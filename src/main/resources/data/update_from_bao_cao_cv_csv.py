#!/usr/bin/env python3
"""
Đọc CSV từ sheet "Báo cáo CV cuối ngày" (Quan ly cong viec_PHN.csv),
sinh SQL PostgreSQL để:
1. UPDATE tasks: status, weight, quality, objective, completed_at
2. INSERT daily_reports: báo cáo cuối ngày cho dashboard theo dõi

Chạy: python update_from_bao_cao_cv_csv.py [đường_dẫn_csv]
Mặc định CSV: backend/target/classes/data/Quan ly cong viec_PHN.csv
Output: update_dashboard_from_bao_cao_cv.sql
"""

import csv
import sys
import os
from datetime import datetime

# Cột CSV (semicolon, header có cột "Trọng số CV (tự đánh giá)")
COL_NGAY_GIAO = 0
COL_NGUOI_GIAO = 1
COL_TEN_CONG_VIEC = 2
COL_NOI_DUNG = 3
COL_DEADLINE = 4
COL_NGAY_HOAN_THANH = 5
COL_CHU_TRI = 6
COL_TIEN_DO = 7
# 8 = Trọng số CV (tự đánh giá)
COL_KET_QUA_THUC_HIEN = 9
COL_DANH_GIA_CHI_HUY = 10
COL_TRONG_SO_CV = 11
COL_CHAT_LUONG_CV = 12
COL_TRANG_THAI_CV = 13

def parse_date_dd_mm_yyyy(s):
    if not s or not str(s).strip():
        return None
    s = str(s).strip()
    try:
        d = datetime.strptime(s, "%d/%m/%Y")
        return d.strftime("%Y-%m-%d")
    except Exception:
        return None

def map_tien_do(tien_do):
    if not tien_do:
        return None
    t = str(tien_do).strip().lower()
    if "hoàn thành" in t:
        return "COMPLETED"
    if "đang thực hiện" in t or "thực hiện" in t:
        return "ACCEPTED"
    return None

def map_trong_so(ts):
    if not ts:
        return None
    t = str(ts).strip().lower()
    if "rất cao" in t:
        return 0.8
    if "cao" in t:
        return 0.6
    if "bình thường" in t:
        return 0.4
    if "rất thấp" in t or "thấp" in t:
        return 0.2
    return None

def map_chat_luong(cl):
    if not cl:
        return None
    c = str(cl).strip().lower()
    if "đạt chuẩn" in c or "đạt" in c:
        return 0.9
    if "không đạt" in c:
        return 0.3
    return None

def escape_sql(s, max_len=3900):
    if s is None:
        return "NULL"
    t = str(s).strip()
    if not t:
        return "NULL"
    t = t.replace("\\", "\\\\").replace("'", "''")[:max_len]
    return "'" + t + "'"

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_csv = os.path.join(script_dir, "..", "..", "..", "..", "target", "classes", "data", "Quan ly cong viec_PHN.csv")
    csv_path = sys.argv[1] if len(sys.argv) > 1 else default_csv
    if not os.path.isfile(csv_path):
        csv_path = os.path.join(script_dir, "Quan ly cong viec_PHN.csv")
    if not os.path.isfile(csv_path):
        print("Không tìm thấy file CSV. Chỉ định đường dẫn: python update_from_bao_cao_cv_csv.py <path_to_csv>", file=sys.stderr)
        sys.exit(1)

    out_sql = os.path.join(script_dir, "update_dashboard_from_bao_cao_cv.sql")
    rows = []
    with open(csv_path, "r", encoding="utf-8-sig", newline="") as f:
        reader = csv.reader(f, delimiter=";", quotechar='"')
        header = next(reader, None)
        for row in reader:
            if len(row) <= COL_CHU_TRI:
                continue
            title = (row[COL_TEN_CONG_VIEC] or "").strip()
            chu_tri = (row[COL_CHU_TRI] or "").strip()
            if not title or not chu_tri:
                continue
            ngay_hoan_thanh = parse_date_dd_mm_yyyy(row[COL_NGAY_HOAN_THANH] if len(row) > COL_NGAY_HOAN_THANH else "")
            ngay_giao = parse_date_dd_mm_yyyy(row[COL_NGAY_GIAO] if len(row) > COL_NGAY_GIAO else "")
            ket_qua = (row[COL_KET_QUA_THUC_HIEN] or "").strip() if len(row) > COL_KET_QUA_THUC_HIEN else ""
            trong_so = map_trong_so(row[COL_TRONG_SO_CV] if len(row) > COL_TRONG_SO_CV else "")
            chat_luong = map_chat_luong(row[COL_CHAT_LUONG_CV] if len(row) > COL_CHAT_LUONG_CV else "")
            tien_do = map_tien_do(row[COL_TIEN_DO] if len(row) > COL_TIEN_DO else "")
            rows.append({
                "title": title,
                "chu_tri": chu_tri,
                "ngay_giao": ngay_giao,
                "ngay_hoan_thanh": ngay_hoan_thanh,
                "ket_qua": ket_qua,
                "trong_so": trong_so,
                "chat_luong": chat_luong,
                "tien_do": tien_do,
            })

    lines = []
    lines.append("-- Cập nhật tasks và daily_reports từ sheet Báo cáo CV cuối ngày (CSV)")
    lines.append("-- Sinh bởi update_from_bao_cao_cv_csv.py. Chạy trên PostgreSQL (Render).")
    lines.append("SET client_encoding = 'UTF8';")
    lines.append("")

    # 1) UPDATE tasks
    for r in rows:
        if not r["tien_do"] and r["trong_so"] is None and r["chat_luong"] is None:
            continue
        title_esc = escape_sql(r["title"], 500)
        chu_tri_esc = escape_sql(r["chu_tri"])
        set_parts = []
        if r["tien_do"]:
            set_parts.append(f"status = '{r['tien_do']}'")
        if r["trong_so"] is not None:
            set_parts.append(f"weight = {r['trong_so']}")
        if r["chat_luong"] is not None:
            set_parts.append(f"quality = {r['chat_luong']}")
        if r["ket_qua"]:
            set_parts.append(f"objective = {escape_sql(r['ket_qua'], 1000)}")
        if r["ngay_hoan_thanh"] and r["tien_do"] == "COMPLETED":
            set_parts.append(f"completed_at = '{r['ngay_hoan_thanh']} 12:00:00'")
        if not set_parts:
            continue
        sql = f"""UPDATE tasks SET {", ".join(set_parts)}
WHERE TRIM(title) = TRIM({title_esc})
  AND assignee_id = (SELECT id FROM users WHERE name = {chu_tri_esc} LIMIT 1);"""
        lines.append(sql)
        lines.append("")

    lines.append("-- 2) INSERT daily_reports (báo cáo cuối ngày cho dashboard theo dõi)")
    lines.append("-- Chỉ thêm bản ghi có nội dung Kết quả thực hiện; report_date = Ngày hoàn thành hoặc Ngày giao")
    for r in rows:
        if not r["ket_qua"]:
            continue
        report_date = r["ngay_hoan_thanh"] or r["ngay_giao"]
        if not report_date:
            report_date = datetime.now().strftime("%Y-%m-%d")
        result_esc = escape_sql(r["ket_qua"], 4000)
        weight_val = r["trong_so"] if r["trong_so"] is not None else 0.5
        title_esc = escape_sql(r["title"], 500)
        chu_tri_esc = escape_sql(r["chu_tri"])
        sql = f"""INSERT INTO daily_reports (user_id, task_id, report_date, result, weight, submitted_at)
SELECT u.id, t.id, '{report_date}'::date, {result_esc}, {weight_val}, NOW()
FROM users u
JOIN tasks t ON t.assignee_id = u.id AND TRIM(t.title) = TRIM({title_esc})
WHERE u.name = {chu_tri_esc}
  AND NOT EXISTS (
    SELECT 1 FROM daily_reports dr
    WHERE dr.user_id = u.id AND dr.task_id = t.id AND dr.report_date = '{report_date}'::date
  );"""
        lines.append(sql)
        lines.append("")

    with open(out_sql, "w", encoding="utf-8") as f:
        f.write("\n".join(lines))
    print("Generated: " + out_sql + " (rows: " + str(len(rows)) + ")")

if __name__ == "__main__":
    main()
