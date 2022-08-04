#*===================================================================================
#* Powershell v2 Scripts to run test
#* 
#*
#*====================================================================================
Param([string]$hostVm, [string]$testPath)
$uri = "http://" + $hostVm + ":@project.application.port@/" + $testPath
$msg = "Test $uri..."
Write-Host $msg
$response = Invoke-RestMethod -uri $uri
if ($response -eq "pong") {
	$msg = "Succeeded: $response.status"
	Write-Host $msg
}
else {
	$msg = "Failed: $response"
	Write-Host $msg
	Throw "Test Failed"
}