$body = @{
    text = @"
订单号：260423-482449877411343
订单内容：炫彩足球加巨兽机甲
服务端口：Q
角色名字：向来运气好
几格保险：9
段位多少：钻石
"@
} | ConvertTo-Json -Depth 10

$response = Invoke-RestMethod -Uri 'http://localhost:8080/api/admin/order/recognize' `
    -Method Post `
    -ContentType 'application/json' `
    -Body ([System.Text.Encoding]::UTF8.GetBytes($body))

$response | ConvertTo-Json -Depth 10