@echo off
REM Windows CMD entrypoint: runs check-compose-image-updates.ps1 in the same folder.
REM Same report as the .sh script (Compose image pins vs latest stable tags).
REM Prints per-image progress while querying registries (15s timeout per request).
REM Pass --dry-run or --apply to preview or apply OUTDATED upgrades.
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0check-compose-image-updates.ps1" %*
exit /b %ERRORLEVEL%
