-- Chay file nay TRUOC khi chay lai seed + import (khi da co du lieu sai/du thua)
-- Xoa theo thu tu de tranh loi FK.

SET client_encoding = 'UTF8';

DELETE FROM daily_reports;
DELETE FROM tasks;
DELETE FROM attendance_records;
DELETE FROM leave_requests;
DELETE FROM users WHERE role != 'ADMIN';
