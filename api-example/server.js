/**
 * Sample API server for the locked VPN app.
 * Run on your server: node server.js
 * Serves GET /vpn/servers from servers.json
 *
 * Use behind HTTPS (e.g. nginx + Let's Encrypt). Do not put credentials in this file.
 */
const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 3000;
const SERVERS_FILE = path.join(__dirname, 'servers.json');

function serveJson(res, obj) {
  res.setHeader('Content-Type', 'application/json; charset=utf-8');
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.end(JSON.stringify(obj));
}

const server = http.createServer((req, res) => {
  const url = new URL(req.url || '', `http://localhost:${PORT}`);
  if (url.pathname === '/vpn/servers' && req.method === 'GET') {
    fs.readFile(SERVERS_FILE, 'utf8', (err, data) => {
      if (err) {
        res.statusCode = 500;
        serveJson(res, []);
        return;
      }
      try {
        const list = JSON.parse(data);
        serveJson(res, Array.isArray(list) ? list : []);
      } catch (e) {
        res.statusCode = 500;
        serveJson(res, []);
      }
    });
  } else {
    res.statusCode = 404;
    res.end('Not found');
  }
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`VPN API example listening on port ${PORT}. GET /vpn/servers`);
});
