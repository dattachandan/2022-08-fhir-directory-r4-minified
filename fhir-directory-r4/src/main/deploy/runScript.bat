invoke-command -computername CI-Docker-Form -FilePath "C:\temp\upgradeApp.ps1" -ArgumentList 10.2.3.51:5000, $False

invoke-command -computername CI-Docker-Form -FilePath "C:\temp\upgradeApp.ps1" -ArgumentList 10.2.3.51:5000, $TRUE
