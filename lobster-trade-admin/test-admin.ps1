$response = Invoke-WebRequest -Uri 'http://localhost:8080/admin/' -UseBasicParsing
Write-Host "Status:" $response.StatusCode
if ($response.Content -match '<title>(.*?)</title>') {
    Write-Host "Title:" $Matches[1]
}
Write-Host "Content preview:" $response.Content.Substring(0, 300)