# VoxShield Backend

FastAPI backend for VoxShield real-time voice threat detection.

## Prerequisites

- Python 3.9+
- pip

## Installation

1. Create a virtual environment (optional but recommended):
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```

2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Running the Server

Start the FastAPI server using uvicorn:

```bash
uvicorn main:app --reload
```

The API will be available at `http://127.0.0.1:8000`.
Swagger UI documentation: `http://127.0.0.1:8000/docs`.

## Key Endpoints

- **WebSocket**: `ws://127.0.0.1:8000/ws/calls/{call_id}`
  - Upstream: `{"type": "audio_chunk", "data": "base64_encoded_audio"}`
  - Downlink: `{"type": "risk_update", ...}`
- **Attack Lab**: `POST /api/v1/attack-lab/start`
  - Body: `{"attack_type": "voice_clone" | "replay" | "social_engineering"}`
