"""
Blood Bank Management System (BBMS) - Python Lightweight Server Option
College of Engineering Muttathara | OOPS Project

Usage:
  python server.py
  Then open: http://localhost:8080
"""

import http.server
import socketserver
import json
import urllib.parse
import os
import mimetypes
from datetime import datetime

PORT = 8080
WEB_DIR = os.path.join(os.path.dirname(__file__), 'web')

# Default In-Memory / Seed Database (Matches Slide 13 Schema)
db = {
    "admin": {"username": "admin", "name": "Administrator", "role": "Super Administrator"},
    "inventory": [
        {"bloodGroup": "A+", "quantity": 15},
        {"bloodGroup": "A-", "quantity": 8},
        {"bloodGroup": "B+", "quantity": 20},
        {"bloodGroup": "B-", "quantity": 5},
        {"bloodGroup": "AB+", "quantity": 12},
        {"bloodGroup": "AB-", "quantity": 4},
        {"bloodGroup": "O+", "quantity": 25},
        {"bloodGroup": "O-", "quantity": 6}
    ],
    "donors": [
        {"id": 1, "name": "Rahul Sharma", "bloodGroup": "O+", "contact": "9876543210"},
        {"id": 2, "name": "Anjali Menon", "bloodGroup": "A+", "contact": "9845123456"},
        {"id": 3, "name": "Karthik Nair", "bloodGroup": "B+", "contact": "9744112233"},
        {"id": 4, "name": "Sneha Roy", "bloodGroup": "AB+", "contact": "9966554433"},
        {"id": 5, "name": "Mohammed Arif", "bloodGroup": "O-", "contact": "9447012345"}
    ],
    "donations": [
        {"id": 1, "donorId": 1, "donorName": "Rahul Sharma", "bloodGroup": "O+", "quantity": 2, "date": "2026-09-15"},
        {"id": 2, "donorId": 2, "donorName": "Anjali Menon", "bloodGroup": "A+", "quantity": 1, "date": "2026-09-18"},
        {"id": 3, "donorId": 3, "donorName": "Karthik Nair", "bloodGroup": "B+", "quantity": 2, "date": "2026-09-22"},
        {"id": 4, "donorId": 4, "donorName": "Sneha Roy", "bloodGroup": "AB+", "quantity": 1, "date": "2026-09-28"},
        {"id": 5, "donorId": 5, "donorName": "Mohammed Arif", "bloodGroup": "O-", "quantity": 1, "date": "2026-10-02"}
    ],
    "issues": [
        {"id": 1, "bloodGroup": "O+", "quantity": 1, "date": "2026-10-03", "adminName": "admin", "recipient": "City General Hospital (ICU)"},
        {"id": 2, "bloodGroup": "A+", "quantity": 1, "date": "2026-10-05", "adminName": "admin", "recipient": "Government Medical College (Trauma)"}
    ]
}

class BBMSHandler(http.server.SimpleHTTPRequestHandler):
    def send_cors(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors()
        self.end_headers()

    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path

        if path.startswith("/api/"):
            self.handle_api_get(path)
        else:
            # Serve frontend static assets
            if path == "/" or path == "":
                filepath = os.path.join(WEB_DIR, "index.html")
            else:
                filepath = os.path.join(WEB_DIR, path.lstrip("/"))

            if os.path.exists(filepath) and os.path.isfile(filepath):
                self.send_response(200)
                mime, _ = mimetypes.guess_type(filepath)
                self.send_header("Content-Type", mime or "text/plain")
                self.send_cors()
                self.end_headers()
                with open(filepath, "rb") as f:
                    self.wfile.write(f.read())
            else:
                self.send_error(404, f"File Not Found: {path}")

    def do_POST(self):
        parsed = urllib.parse.urlparse(self.path)
        path = parsed.path
        body = self.read_json_body()

        if path == "/api/login":
            username = body.get("username", "")
            password = body.get("password", "")
            if (username == "admin" and password == "admin123") or (username == "staff_admin" and password == "staff123"):
                self.send_json(200, {
                    "success": True,
                    "admin": {
                        "username": username,
                        "name": "Super Administrator" if username == "admin" else "Blood Bank Officer",
                        "role": "Super Administrator" if username == "admin" else "Staff Administrator"
                    }
                })
            else:
                self.send_json(401, {"success": False, "message": "Invalid admin credentials"})

        elif path == "/api/donors":
            name = body.get("name", "").strip()
            bloodGroup = body.get("bloodGroup", "").strip()
            contact = body.get("contact", "").strip()
            new_id = max([d["id"] for d in db["donors"]], default=0) + 1
            db["donors"].append({"id": new_id, "name": name, "bloodGroup": bloodGroup, "contact": contact})
            self.send_json(200, {"success": True, "id": new_id})

        elif path == "/api/donations":
            donorId = int(body.get("donorId", 0))
            quantity = int(body.get("quantity", 1))
            date = body.get("date", str(datetime.now().date()))
            donor = next((d for d in db["donors"] if d["id"] == donorId), None)
            if not donor or quantity <= 0:
                self.send_json(400, {"success": False, "message": "Invalid donor or quantity"})
                return

            new_id = max([d["id"] for d in db["donations"]], default=0) + 1
            db["donations"].append({
                "id": new_id, "donorId": donorId, "donorName": donor["name"],
                "bloodGroup": donor["bloodGroup"], "quantity": quantity, "date": date
            })
            # Automatic Stock Increment (Slide 8, 16)
            for inv in db["inventory"]:
                if inv["bloodGroup"] == donor["bloodGroup"]:
                    inv["quantity"] += quantity
                    break
            self.send_json(200, {"success": True, "id": new_id})

        elif path == "/api/inventory/update":
            bloodGroup = body.get("bloodGroup", "")
            quantity = int(body.get("quantity", 0))
            if quantity < 0:
                self.send_json(400, {"success": False, "message": "Quantity cannot be negative"})
                return
            for inv in db["inventory"]:
                if inv["bloodGroup"] == bloodGroup:
                    inv["quantity"] = quantity
                    break
            self.send_json(200, {"success": True})

        elif path == "/api/issues":
            bloodGroup = body.get("bloodGroup", "")
            quantity = int(body.get("quantity", 1))
            date = body.get("date", str(datetime.now().date()))
            recipient = body.get("recipient", "City Hospital")

            inv = next((i for i in db["inventory"] if i["bloodGroup"] == bloodGroup), None)
            if not inv or inv["quantity"] < quantity:
                self.send_json(400, {"success": False, "message": f"Insufficient stock for {bloodGroup}"})
                return

            # Automatic Stock Deduction (Slide 9, 13)
            inv["quantity"] -= quantity
            new_id = max([i["id"] for i in db["issues"]], default=0) + 1
            db["issues"].append({
                "id": new_id, "bloodGroup": bloodGroup, "quantity": quantity,
                "date": date, "adminName": "admin", "recipient": recipient
            })
            self.send_json(200, {"success": True, "id": new_id})

        else:
            self.send_json(404, {"error": "Not Found"})

    def do_PUT(self):
        if self.path == "/api/donors":
            body = self.read_json_body()
            donor_id = int(body.get("id", 0))
            for d in db["donors"]:
                if d["id"] == donor_id:
                    d["name"] = body.get("name", d["name"])
                    d["bloodGroup"] = body.get("bloodGroup", d["bloodGroup"])
                    d["contact"] = body.get("contact", d["contact"])
                    self.send_json(200, {"success": True})
                    return
            self.send_json(404, {"success": False, "message": "Donor not found"})
        else:
            self.send_json(404, {"error": "Not Found"})

    def do_DELETE(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/api/donors":
            query = urllib.parse.parse_qs(parsed.query)
            donor_id = int(query.get("id", [0])[0])
            db["donors"] = [d for d in db["donors"] if d["id"] != donor_id]
            self.send_json(200, {"success": True})
        else:
            self.send_json(404, {"error": "Not Found"})

    def handle_api_get(self, path):
        if path == "/api/dashboard":
            self.send_json(200, db)
        elif path == "/api/donors":
            self.send_json(200, db["donors"])
        elif path == "/api/inventory":
            self.send_json(200, db["inventory"])
        elif path == "/api/donations":
            self.send_json(200, db["donations"])
        elif path == "/api/issues":
            self.send_json(200, db["issues"])
        else:
            self.send_json(404, {"error": "Endpoint not found"})

    def read_json_body(self):
        length = int(self.headers.get("Content-Length", 0))
        if length > 0:
            raw = self.rfile.read(length).decode("utf-8")
            try:
                return json.loads(raw)
            except Exception:
                return {}
        return {}

    def send_json(self, status, payload):
        data = json.dumps(payload).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_cors()
        self.end_headers()
        self.wfile.write(data)

if __name__ == "__main__":
    print("=========================================================")
    print("  BLOOD BANK MANAGEMENT SYSTEM - WEB SERVER (Python)     ")
    print("  College of Engineering Muttathara | OOPS Mini Project  ")
    print("=========================================================")
    print(f"Starting server on http://localhost:{PORT}")
    print("Credentials: admin / admin123")
    print("Press Ctrl+C to stop.")
    with socketserver.TCPServer(("", PORT), BBMSHandler) as httpd:
        httpd.serve_forever()
