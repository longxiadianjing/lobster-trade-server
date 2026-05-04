const http = require('http');
const body = JSON.stringify({
  text: `订单号：260423-482449877411343
订单内容：炫彩足球加巨兽机甲
服务端口：Q
角色名字：向来运气好
几格保险：9
联系方式：13812345678
订单来源：淘宝
段位多少：钻石`
});
const req = http.request({
  hostname: 'localhost', port: 8080, path: '/api/admin/order/recognize',
  method: 'POST',
  headers: { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) }
}, res => {
  let d = ''; res.on('data', c => d += c);
  res.on('end', () => {
    const data = JSON.parse(d);
    const r = data.data?.recognized;
    console.log('contact:', r?.contact);
    console.log('orderSource:', r?.orderSource);
    console.log('All:', JSON.stringify(r, null, 2));
  });
});
req.on('error', e => console.error(e));
req.write(body);
req.end();