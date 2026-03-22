# SmartVoice: A Developer Setup Guide

## Overview
SmartVoice is an Android app that records audio and sends it to a local Python server running a voice/audio analysis algorithm. This guide covers getting both the server and the app running on your local machine.

---

## 1. Server Setup

The server is a Python FastAPI app run with Uvicorn.

### Prerequisites
- Python 3.10
- The `SERVER` project folder (unzipped)

### Steps

```bash
# 1. Navigate to the server directory
cd path/to/SERVER

# 2. (Windows only) Allow script execution for this session
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass

# 3. Activate the virtual environment
# Windows:
.\.venv310\Scripts\activate
# macOS/Linux:
source .venv310/bin/activate

# 4. Install dependencies
pip install -r requirements.txt

# 5. Start the server
python -m uvicorn server:app --host 0.0.0.0 --port 8000
```

### Verify it's running
- Swagger docs: http://localhost:8000/docs
- Health check: http://localhost:8000/health

> **Note:** The server binds to `0.0.0.0` so Android devices/emulators on the same network can reach it. Make sure your firewall allows port 8000.

---

## 2. Android App Setup (Android Studio)

### Prerequisites
- Android Studio (see version note below)
- JDK 17 / JetBrains Runtime 17

### Android Studio Version
Use the version documented in the original setup guide, check the project repo's `docs/` folder for the screenshot reference.

### Build Configuration

In `build.gradle` (app level), ensure the following:

```groovy
compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlinOptions {
    jvmTarget = "17"
}
```

### Gradle JDK
Go to **Settings → Build, Execution, Deployment → Build Tools → Gradle** and set:
- **Gradle JDK:** `jbr-17 JetBrains Runtime`
- Location should point to your local JDK 17 installation

### Connecting to the Server
- If running on an **emulator**, the server address is `http://10.0.2.2:8000`
- If running on a **physical device**, use your machine's local IP (e.g. `http://192.168.x.x:8000`)
- Make sure both devices are on the same Wi-Fi network

---

## 3. Notes for Future Developers

- **Microphone permission** must be granted on the device for recording to work. Check `AndroidManifest.xml` for the `RECORD_AUDIO` permission.
- **Python version matters:** The virtual environment is built for Python 3.10 specifically. Using a different version may break dependencies.
- If the server fails to start, check that port 8000 is not already in use on Windows.
