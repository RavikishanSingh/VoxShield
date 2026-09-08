import json
import os
import time
import hashlib
from typing import List, Dict

class SecurityRepository:
    def __init__(self, db_path="security_db.json"):
        self.db_path = db_path
        if not os.path.exists(db_path):
            self.data = {
                "calls": [],
                "incidents": [],
                "stats": {
                    "total_analyzed": 0,
                    "threats_detected": 0,
                    "attacks_blocked": 0
                }
            }
            self._save()
        else:
            with open(db_path, 'r') as f:
                try:
                    self.data = json.load(f)
                except:
                    self.data = {"calls": [], "incidents": [], "stats": {"total_analyzed": 0, "threats_detected": 0, "attacks_blocked": 0}}

    def _generate_audit_hash(self, record: Dict) -> str:
        """Generates a tamper-proof SHA-256 hash for cyber resilience."""
        record_str = json.dumps(record, sort_keys=True)
        return hashlib.sha256(record_str.encode()).hexdigest()

    def _save(self):
        with open(self.db_path, 'w') as f:
            json.dump(self.data, f, indent=4)

    def record_call(self, call_data: Dict):
        call_data["timestamp"] = time.time()
        call_data["audit_hash"] = self._generate_audit_hash(call_data)
        self.data["calls"].append(call_data)
        self.data["stats"]["total_analyzed"] += 1
        self._save()

    def record_incident(self, incident: Dict):
        incident["timestamp"] = time.time()
        # Add audit integrity layer
        incident["audit_hash"] = self._generate_audit_hash(incident)

        self.data["incidents"].append(incident)
        self.data["stats"]["threats_detected"] += 1
        if incident.get("decision") == "BLOCK":
            self.data["stats"]["attacks_blocked"] += 1
        self._save()

    def get_analytics(self) -> Dict:
        return self.data["stats"]

    def get_incidents(self) -> List[Dict]:
        return self.data["incidents"]

repo = SecurityRepository()
