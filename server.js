/**
 * Blood Bank Management System (BBMS) - Node.js Lightweight Server Option
 * College of Engineering Muttathara | OOPS Project
 * 
 * Usage:
 *   node server.js
 *   Then open: http://localhost:8080
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 8080;
const WEB_DIR = path.join(__dirname, 'web');

// Seed Database (Matches Slide 13 Schema)
let db = {
  admin: { username: "admin", name: "Administrator", role: "Super Administrator" },
  inventory: [
    { bloodGroup: "A+", quantity: 15 },
    { bloodGroup: "A-", quantity: 8 },
    { bloodGroup: "B+", quantity: 20 },
    { bloodGroup: "B-", quantity: 5 },
    { bloodGroup: "AB+", quantity: 12 },
    { bloodGroup: "AB-", quantity: 4 },
    { bloodGroup: "O+", quantity: 25 },
    { bloodGroup: "O-", quantity: 6 }
  ],
  donors: [
    { id: 1, name: "Rahul Sharma", bloodGroup: "O+", contact: "9876543210" },
    { id: 2, name: "Anjali Menon", bloodGroup: "A+", contact: "9845123456" },
    { id: 3, name: "Karthik Nair", bloodGroup: "B+", contact: "9744112233" },
    { id: 4, name: "Sneha Roy", bloodGroup: "AB+", contact: "9966554433" },
    { id: 5, name: "Mohammed Arif", bloodGroup: "O-", contact: "9447012345" }
  ],
  donations: [
    { id: 1, donorId: 1, donorName: "Rahul Sharma", bloodGroup: "O+", quantity: 2, date: "2026-09-15" },
    { id: 2, donorId: 2, donorName: "Anjali Menon", bloodGroup: "A+", quantity: 1, date: "2026-09-18" },
    { id: 3, donorId: 3, donorName: "Karthik Nair", bloodGroup: "B+", quantity: 2, date: "2026-09-22" },
    { id: 4, donorId: 4, donorName: "Sneha Roy", bloodGroup: "AB+", quantity: 1, date: "2026-09-28" },
    { id: 5, donorId: 5, donorName: "Mohammed Arif", bloodGroup: "O-", quantity: 1, date: "2026-10-02" }
  ],
  issues: [
    { id: 1, bloodGroup: "O+", quantity: 1, date: "2026-10-03", adminName: "admin", recipient: "City General Hospital (ICU)" },
    { id: 2, bloodGroup: "A+", quantity: 1, date: "2026-10-05", adminName: "admin", recipient: "Government Medical College (Trauma)" }
  ]
};

const mimeTypes = {
  '.html': 'text/html',
  '.css': 'text/css',
  '.js': 'application/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};

const server = http.createServer((req, res) => {
  // CORS
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;

  // Helper to read JSON request body
  const readJson = (callback) => {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try {
        callback(JSON.parse(body || '{}'));
      } catch (e) {
        callback({});
      }
    });
  };

  // Helper to send JSON response
  const sendJson = (status, data) => {
    res.writeHead(status, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify(data));
  };

  // REST API Routes
  if (pathname.startsWith('/api/')) {
    if (pathname === '/api/login' && req.method === 'POST') {
      readJson(data => {
        if ((data.username === 'admin' && data.password === 'admin123') ||
            (data.username === 'staff_admin' && data.password === 'staff123')) {
          sendJson(200, {
            success: true,
            admin: {
              username: data.username,
              name: data.username === 'admin' ? 'Super Administrator' : 'Blood Bank Officer',
              role: data.username === 'admin' ? 'Super Administrator' : 'Staff Administrator'
            }
          });
        } else {
          sendJson(401, { success: false, message: 'Invalid admin credentials' });
        }
      });
      return;
    }

    if (pathname === '/api/dashboard' && req.method === 'GET') {
      sendJson(200, db);
      return;
    }

    if (pathname === '/api/donors') {
      if (req.method === 'GET') {
        sendJson(200, db.donors);
      } else if (req.method === 'POST') {
        readJson(data => {
          const newId = (db.donors.length > 0 ? Math.max(...db.donors.map(d => d.id)) : 0) + 1;
          db.donors.push({ id: newId, name: data.name, bloodGroup: data.bloodGroup, contact: data.contact });
          sendJson(200, { success: true, id: newId });
        });
      } else if (req.method === 'PUT') {
        readJson(data => {
          const id = parseInt(data.id);
          const donor = db.donors.find(d => d.id === id);
          if (donor) {
            donor.name = data.name || donor.name;
            donor.bloodGroup = data.bloodGroup || donor.bloodGroup;
            donor.contact = data.contact || donor.contact;
            sendJson(200, { success: true });
          } else {
            sendJson(404, { success: false, message: 'Donor not found' });
          }
        });
      } else if (req.method === 'DELETE') {
        const id = parseInt(parsedUrl.query.id);
        db.donors = db.donors.filter(d => d.id !== id);
        sendJson(200, { success: true });
      }
      return;
    }

    if (pathname === '/api/donations' && req.method === 'POST') {
      readJson(data => {
        const donorId = parseInt(data.donorId);
        const quantity = parseInt(data.quantity);
        const donor = db.donors.find(d => d.id === donorId);
        if (!donor || quantity <= 0) {
          sendJson(400, { success: false, message: 'Invalid donor or quantity' });
          return;
        }
        const newId = (db.donations.length > 0 ? Math.max(...db.donations.map(d => d.id)) : 0) + 1;
        db.donations.push({
          id: newId,
          donorId: donor.id,
          donorName: donor.name,
          bloodGroup: donor.bloodGroup,
          quantity: quantity,
          date: data.date || new Date().toISOString().split('T')[0]
        });
        // Automatic stock increment
        const inv = db.inventory.find(i => i.bloodGroup === donor.bloodGroup);
        if (inv) inv.quantity += quantity;
        sendJson(200, { success: true, id: newId });
      });
      return;
    }

    if (pathname === '/api/inventory/update' && req.method === 'POST') {
      readJson(data => {
        const inv = db.inventory.find(i => i.bloodGroup === data.bloodGroup);
        if (inv && data.quantity >= 0) {
          inv.quantity = parseInt(data.quantity);
          sendJson(200, { success: true });
        } else {
          sendJson(400, { success: false, message: 'Invalid quantity' });
        }
      });
      return;
    }

    if (pathname === '/api/issues' && req.method === 'POST') {
      readJson(data => {
        const quantity = parseInt(data.quantity);
        const inv = db.inventory.find(i => i.bloodGroup === data.bloodGroup);
        if (!inv || inv.quantity < quantity) {
          sendJson(400, { success: false, message: 'Insufficient stock' });
          return;
        }
        // Automatic stock deduction
        inv.quantity -= quantity;
        const newId = (db.issues.length > 0 ? Math.max(...db.issues.map(i => i.id)) : 0) + 1;
        db.issues.push({
          id: newId,
          bloodGroup: data.bloodGroup,
          quantity: quantity,
          date: data.date || new Date().toISOString().split('T')[0],
          adminName: 'admin',
          recipient: data.recipient || 'Hospital'
        });
        sendJson(200, { success: true, id: newId });
      });
      return;
    }

    sendJson(404, { error: 'API endpoint not found' });
    return;
  }

  // Static File Serving
  let filePath = path.join(WEB_DIR, pathname === '/' ? 'index.html' : pathname);
  const ext = path.extname(filePath).toLowerCase();
  const contentType = mimeTypes[ext] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        res.writeHead(404, { 'Content-Type': 'text/plain' });
        res.end('404 Not Found');
      } else {
        res.writeHead(500, { 'Content-Type': 'text/plain' });
        res.end('500 Server Error: ' + err.code);
      }
    } else {
      res.writeHead(200, { 'Content-Type': contentType });
      res.end(content, 'utf-8');
    }
  });
});

server.listen(PORT, () => {
  console.log("=========================================================");
  console.log("  BLOOD BANK MANAGEMENT SYSTEM - WEB SERVER (Node.js)    ");
  console.log("  College of Engineering Muttathara | OOPS Mini Project  ");
  console.log("=========================================================");
  console.log(`Server running at http://localhost:${PORT}/`);
  console.log("Admin Default Credentials: admin / admin123");
  console.log("Press Ctrl+C to stop.");
});
