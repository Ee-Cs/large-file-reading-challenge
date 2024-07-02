@echo on
@set SITE=http://localhost:8080
@set CURL=c:\tools\curl-7.58.0\bin\curl.exe
@set CURL=%CURL% -g -i -H "Accept: application/json"
@set HR_YELLOW=@powershell -Command Write-Host "----------------------------------------------------------------------" -foreground "Yellow"
@set HR_RED=@powershell    -Command Write-Host "----------------------------------------------------------------------" -foreground "Red"

:large
%HR_YELLOW%
@powershell -Command Write-Host "GET large" -foreground "Green"
%CURL% -H "Content-Type: application/json" -X GET "%SITE%/large"
@echo.

:finish
%HR_RED%
@powershell -Command Write-Host "FINISH" -foreground "Red"
pause