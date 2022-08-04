#*===================================================================================
#* Powershell v2 Scripts to update application.
#*
#* It does the following:
#*
#* Create a new application version folder to c:\healthlink\form-view-api\version
#* requires deployment.properties file in the same folder to read docker file properties sucha as docker image name, isntance name, port number and version.
#* 
#*
#*====================================================================================
Param([string]$registrybaseUrl, [bool] $keepPreviousReleases, $registryUsername, $registryPassword)

Set-ExecutionPolicy RemoteSigned

##### set docker --env options, variable dockerEnvOptions can be replaced in bamboo deployment project  #####
$dockerEnvOptions=""
write-host "Environment variables for docker: $dockerEnvOptions"

##### Set folder variables   ############
$containerName = "@project.application.name@"
$imagePath = "@project.application.image.name@"
$version="@project.version@"
$port=@project.application.port@
$dockerPortOption="-p ${port}:${port} "
$elasticPortOption="-p 6200:6200 "
$jmxPortOption="-p 9000:9000"

$healthlinkHome = "C:\healthlink"
$containerApplicationHome =  $healthlinkHome + "\app";
$containerResourceHome = $containerApplicationHome + "\data"
$containerConfHome = $containerApplicationHome + "\conf"
$containerLogsHome = $containerApplicationHome + "\logs"

$applicationRoot = "C:\" + $imagePath
$applicationRoot = $applicationRoot.replace("/", "\")
$applicationHome = $applicationRoot + "\" + $version
$applicationConf = $applicationRoot + "\conf"
$applicationConf = $applicationConf.replace("/", "\")

$appVersionFilePath = $applicationRoot + "\currentVersion.txt"

$logsFolder = $applicationRoot + "\logs"

$imageFullPath = $registrybaseUrl + "/" + $imagePath + ":" + $version

#### Create applicationRoot and conf folder ####
If (!(test-path $applicationRoot)) {
	New-Item -ItemType directory -Path $applicationRoot
	Write-Host "create directory $applicationRoot"
}
If (!(test-path $applicationConf)) {
	New-Item -ItemType directory -Path $applicationConf
}
#####  Create installation log file   ######
$todayTime = get-date -format "yyyy-MM-ddTHHmm"
$installationLog = $applicationRoot + "\"+ "installationLog-"+ $todayTime + ".txt"
New-Item $installationLog -type file
$msg = "Installation of $containerName with version $version"
Add-Content $installationLog $msg


#*======================================
#* Stop container if running
#*======================================
$started = $False;
$output = docker ps -f name=$containerName
$started = $output -match $containerName
if ($started) {
	$msg = "Stopping container $containerName..."
	write-host $msg
	Add-Content $installationLog $msg
	docker stop $containerName
	$msg = "...done"
	write-host $msg
	Add-Content $installationLog $msg
}

#*======================================
#* Remove container 
#*======================================
$containerExists=$False
$output = docker ps -a -f name=$containerName
$containerExists = $output -match $containerName
if ($containerExists) {
	$msg = "Removing container $containerName..."
	write-host $msg
	Add-Content $installationLog $msg
	Try {
		docker rm -v $containerName
		$msg = "...done"
		write-host $msg
		Add-Content $installationLog $msg
	}
	Catch
	{
		$errMessage = $_.Exception.Message
		write-host $errMessage
		Add-Content $installationLog $errMessage
	}
	finally {
		$error.Clear()
	}
}

#*======================================
#* Remove all existing releases
#*======================================
if (!$keepPreviousReleases) {
	$msg = "Removing previous releases from $applicationRoot ..."
	write-host $msg
	Add-Content $installationLog $msg
	
	$previousVersions = Get-ChildItem $applicationRoot -Name -attributes D
	foreach ($previousVersion in $previousVersions) {
		if (($previousVersions -contains "conf") -or ($previousVersions -contains "log")) {
		   continue
		}
		$msg = "Removing release $previousVersion ..."
		write-host $msg
		Add-Content $installationLog $msg 
		
		$previousImageFullPath = $registrybaseUrl + "/" + $imagePath + ":" + $previousVersion
		docker rmi $previousImageFullPath
		$msg = "... previous docker image $previousImageFullPath removed"
		write-host $msg
		Add-Content $installationLog $msg
		
		$previouseReleaseHome = $applicationRoot + "\" + $previousVersion
		Remove-Item $previouseReleaseHome -Recurse
		$msg = "... previous release folder $previouseReleaseHome removed"
		write-host $msg
		Add-Content $installationLog $msg 	
	}
	if (($lastExitCode -gt 0) -or ($error.count -gt 0)) {
		throw "Remove previous versions (docker image) encoutered errors"
	} 
}

#*======================================
#* Create new version folder 
#*======================================
if (!(Test-Path -path $applicationHome)) {
	$msg = "Creating new application version folder $applicationHome ..."
	write-host $msg
	Add-Content $installationLog $msg
	New-Item $applicationHome -Type Directory
	$msg = "...done"
	Write-Host $msg
	Add-Content $installationLog $msg
}
else {
	$msg = "application version folder $applicationHome already exists"
	write-host $msg
	Add-Content $installationLog $msg
}
if (!(Test-Path -path $logsFolder)) {
	$msg = "Creating new application logs folder $logsFolder ..."
	write-host $msg
	Add-Content $installationLog $msg
	New-Item $logsFolder -Type Directory
	$msg = "...done"
	Write-Host $msg
	Add-Content $installationLog $msg
}
else {
	$msg = "application logs folder $logsFolder already exists"
	write-host $msg
	Add-Content $installationLog $msg
}

#*========================================
#*  Registry Login
#*========================================
"$registryPassword" | docker login $registrybaseUrl --username $registryUsername --password-stdin
$Error.Clear()	

#*========================================
#*  Pull Docker Container from registry
#*========================================
$msg = "Pulling docker image $imageFullPath  ..."
write-host $msg
Add-Content $installationLog $msg
docker pull $imageFullPath
if ($error.count -gt 0) {
    $msg = "encoutered errors while executing docker pull $imageFullPath"
}
else {
	$msg = "... done"
}
write-host $msg
Add-Content $installationLog $msg
docker logout $registrybaseUrl
if ($msg -contains 'encoutered errors') {
  throw $msg
}

#*========================================
#*  Invoke docker run command
#*========================================
$cmd = "docker run --restart=always $dockerPortOption -d --name $containerName $dockerEnvOptions $elasticPortOption $jmxPortOption -v $applicationConf\:$containerConfHome -v $logsFolder\:$containerLogsHome $imageFullPath"
#$cmd = "docker run --restart=always $dockerPortOption -d --name $containerName $dockerEnvOptions -v $logsFolder\:$containerLogsHome $imageFullPath"
write-host "cmd to run: $cmd"
Invoke-Expression $cmd

$msg = "Executed docker run with detached mode for container $containerName"
write-host $msg
Add-Content $installationLog $msg

#*========================================
#*  Check if docker is running
#*========================================
$timeout = new-timespan -seconds 60
$sw = [diagnostics.stopwatch]::StartNew()
$started = $False;
while (($sw.elapsed -lt $timeout) -And !$started){
	$msg = "Checking if container $containerName is running ..."
	Write-Host $msg
	Add-Content $installationLog $msg
	start-sleep -seconds 1
	$output = docker ps -f name=$containerName
	$started = $output -match $imageFullPath
	Write-Host $started
}
if ($started) {
	$msg = "...container $containerName is running"
	Write-Host $msg
	Add-Content $installationLog $msg
}
else {
	$msg = "...timed out after 60s. Check log files under $logsFolder for any failure"
	Write-Host $msg
	Add-Content $installationLog $msg
}

#*========================================
#*  save the current version into file
#*========================================
New-Item $appVersionFilePath -type file -force
Set-Content $appVersionFilePath $version
$msg = "Update $appVersionFilePath with version $version"
write-host $msg
Add-Content $installationLog $msg

#*========================================
#*  Move log file to its own version folder
#*========================================
Move-Item $installationLog $applicationHome

if (!$started) {
	Throw "Docker container $containerName may not start successfully"
}
