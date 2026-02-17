@echo off
REM Chay seed + import voi UTF-8 de tranh loi tieng Viet tren Windows
REM Sua dong CONN below thanh connection string cua ban, roi double-click file nay hoac chay tu CMD.

set CONN=postgresql://phn_user:uu2c8OFoZEk0HA3i6I3YflTmkix6B2Tp@dpg-d66j0gvpm1nc73datvtg-a.oregon-postgres.render.com/phn?sslmode=require

chcp 65001 >nul
set PGCLIENTENCODING=UTF8

cd /d "%~dp0"

echo Chay seed_production_postgres.sql ...
psql "%CONN%" -f seed_production_postgres.sql
if errorlevel 1 (
  echo LOI khi chay seed. Kiem tra connection string va file.
  pause
  exit /b 1
)

echo Chay import_tasks_from_csv_postgres.sql ...
psql "%CONN%" -f import_tasks_from_csv_postgres.sql
if errorlevel 1 (
  echo Co loi khi import. Neu seed chay thanh cong, co the do encoding - thu chay lai file nay.
  pause
  exit /b 1
)

echo Xong.
pause
