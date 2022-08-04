#*===================================================================================
#* Powershell v2 Scripts to check if port is listening.
#* 
#*
#*====================================================================================
Param([string]$hostVm, [int]$timeoutSeconds = 180)

$timeout = new-timespan -seconds $timeoutSeconds
$sw = [diagnostics.stopwatch]::StartNew()
$started = $False;
while (($sw.elapsed -lt $timeout) -And !$started){
	$msg = "Checking if @project.application.port@ is running on $hostVm..."
	Write-Host $msg
	start-sleep -seconds 1
	$started = Test-NetConnection $hostVm -Port @project.application.port@ | ? { $_.TcpTestSucceeded }
	Write-Host $started
}
if ($started) {
	$msg = "...@project.application.port@ is running on $hostVm"
	Write-Host $msg
}
else {
	$msg = "...timed out after $timeoutSeconds seconds."
	Write-Host $msg
	Throw "Docker container @project.application.name@ may not start successfully on $hostVm"
}
