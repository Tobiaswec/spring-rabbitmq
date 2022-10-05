param(
    [Parameter()]
    [int]$runs = 10,
    [int]$sleep = 1000,
    [int]$mode = 1
)

$endpoint = "publish"
if($mode -eq 2 -and $mode -ne 1){
    $endpoint = "broadcast"
}elseif($mode -ge 2){
     throw "Invalid mode, 1 == publish (default), 2 == broadcast"
}

for ($i = 0; $i -lt $runs; $i++){
    $random = Get-Random -Maximum 1000
    Write-Host Sent $endpoint request  with number $random
    Invoke-WebRequest -Uri http://127.0.0.1:8080/api/$endpoint -Body @{number=$random} -Method Post -UseBasicParsing | out-null
    Start-Sleep -Milliseconds $sleep
}