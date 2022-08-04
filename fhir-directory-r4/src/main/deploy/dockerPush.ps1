#*===================================================================================
#* Powershell v2 Scripts to push docker image from one registry to another registry
#* 
#*
#*====================================================================================
Param([string]$sourceRegistrybaseUrl, $sourceRegistryUsername, $sourceRegistryPassword, [string]$targetRegistrybaseUrl, $targetRegistryUsername, $targetRegistryPassword)

$imagePath = "@project.application.image.name@"
$version="@project.version@"
$imagePath = $imagePath + ":" + $version
$sourceImageFullPath = $sourceRegistrybaseUrl + "/" + $imagePath
$targetImageFullPath = $targetRegistrybaseUrl + "/" + $imagePath

#*========================================
#*  Source Registry Login
#*========================================
"$sourceRegistryPassword" | docker login $sourceRegistrybaseUrl --username $sourceRegistryUsername --password-stdin
$Error.Clear()

#*========================================
#*  Docker Pull image 
#*========================================
$msg = "Pulling docker image from $sourceImageFullPath..."
write-host $msg
docker pull $sourceImageFullPath
$msg = "... done"
write-host $msg

#*========================================
#*  Tag Docker image 
#*========================================
$msg = "Tagging docker image $sourceImageFullPath to $targetImageFullPath  ..."
write-host $msg
docker tag $sourceImageFullPath $targetImageFullPath
$msg = "... done"
write-host $msg

#*========================================
#*  Target Registry Login
#*========================================
"$targetRegistryPassword" | docker login $targetRegistrybaseUrl --username $targetRegistryUsername --password-stdin
$Error.Clear()

#*========================================
#*  Push Docker image to registry
#*========================================
$msg = "Pushing docker image $targetImageFullPath  ..."
write-host $msg

docker push $targetImageFullPath
if ($error.count -gt 0) {
    $msg = "encoutered errors while executing docker push $targetImageFullPath"
}
else {
	$msg = "... done"
}
write-host $msg

docker logout $sourceRegistrybaseUrl
docker logout $targetRegistrybaseUrl
if ($msg -contains 'encoutered errors') {
  throw $msg
}
