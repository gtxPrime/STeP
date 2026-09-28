#!/usr/bin/env python3
"""
STeP - Scheduled Tribe e-Portal (MoTA Unified Ecosystem)
Multi-threaded Local Development, Shared Hosting Storage & Admin Server
Ministry of Tribal Affairs, Government of India
"""

import http.server
import os
import sys
import json
import uuid
from pathlib import Path

WEB_DIR = Path(__file__).parent / "web"
UPLOADS_DIR = WEB_DIR / "uploads"
UPLOADS_DIR.mkdir(parents=True, exist_ok=True)
DEFAULT_PORT = 8080

class STePRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(WEB_DIR), **kwargs)

    def end_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization")
        self.send_header("Cache-Control", "no-cache, must-revalidate")
        super().end_headers()

    def do_OPTIONS(self):
        self.send_response(200)
        self.end_headers()

    def do_GET(self):
        if self.path == "/api/health":
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({
                "status": "HEALTHY",
                "service": "STeP MoTA Unified Portal & Shared Hosting Server",
                "version": "2.0.0",
                "department": "Ministry of Tribal Affairs, Government of India",
                "schemes": ["PRE_MATRIC", "POST_MATRIC", "TOP_CLASS", "NFST", "NOS"],
                "sharedHostingReady": True,
                "storageServer": "shared-host-node01.step.gov.in"
            }).encode("utf-8"))
            return

        super().do_GET()

    def do_POST(self):
        # 1. Shared Hosting Document / Image Upload API
        if self.path == "/api/upload":
            content_length = int(self.headers.get("Content-Length", 0))
            post_data = self.rfile.read(content_length)

            # Generate unique storage filename on shared host
            unique_id = uuid.uuid4().hex[:8]
            filename = f"cert_{unique_id}.jpg"
            file_path = UPLOADS_DIR / filename

            # Save file bytes
            with open(file_path, "wb") as f:
                f.write(post_data if post_data else b"DUMMY_CERTIFICATE_BYTES")

            host_header = self.headers.get("Host", f"localhost:{DEFAULT_PORT}")
            public_url = f"http://{host_header}/uploads/{filename}"

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({
                "success": True,
                "publicUrl": public_url,
                "cdnUrl": f"https://storage.step.gov.in/documents/{filename}",
                "fileName": filename,
                "fileSizeKb": max(48, len(post_data) // 1024),
                "storageServer": "shared-host-node01.step.gov.in",
                "firestoreSynced": True
            }).encode("utf-8"))
            return

        # 2. Sovereign DigiLocker Verification Mock Endpoint
        if self.path == "/api/verify-digilocker":
            content_length = int(self.headers.get("Content-Length", 0))
            post_data = self.rfile.read(content_length)
            req = json.loads(post_data.decode("utf-8")) if post_data else {}

            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(json.dumps({
                "verified": True,
                "digilockerRef": f"DL-GOI-ST-{os.urandom(3).hex().upper()}",
                "confidenceScore": 98,
                "issuer": "State e-District Repository",
                "timestamp": "2026-09-28T17:30:00Z"
            }).encode("utf-8"))
            return

        self.send_error(404, "Endpoint not found")

def run(port=DEFAULT_PORT):
    for p in range(port, port + 10):
        try:
            httpd = http.server.ThreadingHTTPServer(("", p), STePRequestHandler)
            print(f"=" * 65)
            print(f" STeP - Scheduled Tribe e-Portal (Admin & Shared Hosting Server)")
            print(f" Ministry of Tribal Affairs (MoTA), Government of India")
            print(f" Serving live at: http://localhost:{p}")
            print(f"=" * 65)
            httpd.serve_forever()
        except OSError:
            continue

if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_PORT
    run(port)
