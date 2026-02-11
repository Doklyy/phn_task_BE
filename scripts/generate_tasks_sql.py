import csv
from datetime import datetime
from pathlib import Path

# Đường dẫn tới thư mục backend
ROOT = Path(__file__).resolve().parents[1]
DATA_DIR = ROOT / "src" / "main" / "resources" / "data"
CSV_PATH = DATA_DIR / "Quan ly cong viec_PHN.csv"
OUT_PATH = DATA_DIR / "import_tasks_from_csv.sql"


def parse_deadline(value: str) -> str:
    """Convert DD/MM/YYYY (hoặc dạng gần giống) sang 'YYYY-MM-DD 00:00:00'."""
    value = (value or "").strip()
    if not value:
        return "NULL"
    for fmt in ("%d/%m/%Y", "%d-%m-%Y", "%Y-%m-%d"):
        try:
            d = datetime.strptime(value, fmt)
            return "'" + d.strftime("%Y-%m-%d 00:00:00") + "'"
        except ValueError:
            continue
    # Nếu parse không được thì giữ nguyên dạng text (đã escape)
    safe = value.replace("'", "''")
    return "'" + safe + "'"


def esc(text: str) -> str:
    if text is None:
        return ""
    return text.replace("'", "''")


def main() -> None:
    if not CSV_PATH.exists():
        raise SystemExit(f"CSV not found: {CSV_PATH}")

    lines: list[str] = []
    lines.append("-- SQL sinh tự động từ Quan ly cong viec_PHN.csv\n")
    lines.append("USE phn;\n\n")

    with CSV_PATH.open("r", encoding="utf-8-sig", newline="") as f:
        reader = csv.reader(f, delimiter=";")
        header = next(reader, None)
        for row in reader:
            if not row or all(not (c or "").strip() for c in row):
                continue

            ngay_giao = (row[0] or "").strip()
            ten_cv = (row[2] or "").strip()
            noi_dung = (row[3] or "").strip()
            deadline_raw = (row[4] or "").strip()
            chu_tri = (row[6] or "").strip()
            tien_do = (row[7] or "").strip().lower()
            ket_qua_muc_tieu = (row[9] or "").strip()
            trong_so = (row[11] or "").strip()

            if not ten_cv or not chu_tri:
                # Bỏ dòng không có tên CV hoặc chủ trì
                continue

            # Trạng thái: Đang thực hiện / Hoàn thành / ...
            if tien_do.startswith("hoàn thành"):
                status = "COMPLETED"
            else:
                status = "ACCEPTED"

            # Trọng số: cố gắng parse số, nếu không được thì mặc định 0.5
            try:
                weight = float(trong_so.replace(",", ".")) if trong_so else 0.5
            except ValueError:
                weight = 0.5

            # Deadline: nếu cột Deadline rỗng thì dùng tạm "Ngày giao" làm deadline
            base_deadline_str = deadline_raw or ngay_giao
            deadline_sql = parse_deadline(base_deadline_str)

            title_sql = esc(ten_cv)
            objective_sql = esc(ket_qua_muc_tieu or f"Mục tiêu từ file CSV ngày {ngay_giao}")
            content_sql = esc(noi_dung)
            chu_tri_sql = esc(chu_tri)

            # Gán: assigner = admin, leader & assignee = người chủ trì
            sql = (
                "\nINSERT INTO tasks\n"
                "  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)\n"
                "VALUES\n"
                f"  ('{title_sql}', '{content_sql}', '{objective_sql}', {deadline_sql}, {weight}, '{status}',\n"
                "   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),\n"
                f"   (SELECT id FROM users WHERE name = '{chu_tri_sql}' LIMIT 1),\n"
                f"   (SELECT id FROM users WHERE name = '{chu_tri_sql}' LIMIT 1),\n"
                "   NOW(6));\n"
            )

            lines.append(sql)

    OUT_PATH.write_text("".join(lines), encoding="utf-8")
    print(f"Wrote SQL to {OUT_PATH}")


if __name__ == "__main__":
    main()

