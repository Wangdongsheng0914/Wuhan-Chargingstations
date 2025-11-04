@echo off
REM 创建数据库索引的批处理脚本
REM 使用方法：双击运行此脚本，或在命令行中执行

echo 正在为stations表创建索引...
echo.

REM 设置数据库连接参数
set PGHOST=localhost
set PGPORT=5432
set PGDATABASE=charging-stations
set PGUSER=postgres
set PGPASSWORD=Wds050914

REM 执行SQL脚本
psql -h %PGHOST% -p %PGPORT% -U %PGUSER% -d %PGDATABASE% -f "src\main\resources\create_indexes.sql"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo 索引创建成功！
) else (
    echo.
    echo 索引创建失败，请检查错误信息。
    echo 如果psql命令不可用，请手动执行SQL脚本。
)

pause

