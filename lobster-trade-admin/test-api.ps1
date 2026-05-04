$bytes = [System.Text.Encoding]::UTF8.GetBytes('{"username":"admin","password":"admin123"}')
$stream = [System.IO.MemoryStream]::new($bytes)
$response = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/login' -Method Post -ContentType 'application/json' -Body $stream
$response | ConvertTo-Json -Depth 5