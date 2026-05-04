var fs = require('fs');
var path = require('path');

function search(dir, pattern, depth) {
  if (depth > 4) return;
  var files;
  try { files = fs.readdirSync(dir); }
  catch(e) { return; }
  files.forEach(function(f) {
    var fp = path.join(dir, f);
    var stat;
    try { stat = fs.statSync(fp); } catch(e) { return; }
    if (stat.isDirectory()) {
      search(fp, pattern, depth + 1);
    } else if (f.endsWith('.vue') || f.endsWith('.js')) {
      try {
        var c = fs.readFileSync(fp, 'utf8');
        var lines = c.split('\n');
        lines.forEach(function(line, i) {
          if (pattern.test(line)) {
            console.log(fp + ':' + (i+1) + ': ' + line.trim().substring(0, 200));
          }
        });
      } catch(e) {}
    }
  });
}

search('C:\\Users\\Administrator\\.openclaw\\workspace\\projects\\龙虾道具交易平台\\code\\lobster-trade-admin\\src', /stats[\/\.]users/);