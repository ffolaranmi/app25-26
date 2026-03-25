from fastapi import FastAPI, UploadFile, File, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
import numpy as np
import librosa
import tensorflow as tf
import soundfile as sf
import io
import traceback
from pathlib import Path

app = FastAPI()

# enable cors so smartvoice can talk to api
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Load model once at startup
print("Loading model...")
model = tf.keras.models.load_model("CNN.keras", compile=False)
print("Model loaded successfully!")

BANDS = 60
FRAMES = 41
WINDOW_SIZE = 512 * (FRAMES - 1)
TARGET_SR = 22050

ALLOWED_EXTENSIONS = {".wav"}
ALLOWED_CONTENT_TYPES = {
    "audio/wav",
    "audio/x-wav",
    "audio/wave",
    "audio/vnd.wave",
}


def windows(audio, window_size):
    start = 0
    while start < len(audio):
        yield int(start), int(start + window_size)
        start += window_size / 2


def extract_features(signal, sr):
    feature = []

    D = np.abs(librosa.stft(signal)) ** 2
    melspec_from_power = librosa.feature.melspectrogram(S=D, n_mels=BANDS)
    feature.append(librosa.amplitude_to_db(melspec_from_power))

    melspec_from_audio = librosa.feature.melspectrogram(y=signal, sr=sr, n_mels=BANDS)
    feature.append(librosa.amplitude_to_db(melspec_from_audio))

    mfcc = librosa.feature.mfcc(y=signal, sr=sr, n_mfcc=BANDS)
    feature.append(librosa.amplitude_to_db(mfcc))

    x = np.array(feature)              # shape: (3, 60, T)
    x = np.transpose(x, (1, 2, 0))    # shape: (60, T, 3)

    if x.shape[1] >= FRAMES:
        x = x[:, :FRAMES, :]
    else:
        pad = FRAMES - x.shape[1]
        x = np.pad(x, ((0, 0), (0, pad), (0, 0)), mode="constant")

    return x.astype(np.float32)


def validate_uploaded_file(file: UploadFile) -> None:
    if not file.filename:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="No filename provided."
        )

    ext = Path(file.filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail="Unsupported file type. Only .wav files are allowed."
        )

    # Content type can sometimes be missing or inaccurate, so only reject when
    # it is explicitly present and clearly not a WAV type.
    if file.content_type and file.content_type not in ALLOWED_CONTENT_TYPES:
        raise HTTPException(
            status_code=status.HTTP_415_UNSUPPORTED_MEDIA_TYPE,
            detail=f"Unsupported media type '{file.content_type}'. Only WAV audio is allowed."
        )


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    print(f"\n{'=' * 60}")
    print(f"Processing: {file.filename}")

    try:
        validate_uploaded_file(file)

        # read audio file
        audio_bytes = await file.read()

        if len(audio_bytes) == 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Uploaded file is empty."
            )

        # load audio file
        try:
            data, sr = sf.read(io.BytesIO(audio_bytes))
        except RuntimeError:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or corrupted WAV file."
            )

        if data is None or np.size(data) == 0:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Uploaded WAV file contains no audio data."
            )

        if np.issubdtype(data.dtype, np.integer):
            max_val = np.iinfo(data.dtype).max
            data = data.astype(np.float32) / max_val
        else:
            data = data.astype(np.float32)

        if data.ndim > 1:
            data = np.mean(data, axis=1)

        if sr != TARGET_SR:
            data = librosa.resample(data, orig_sr=sr, target_sr=TARGET_SR)
            sr = TARGET_SR

        # process windows and get predictions
        probs = []

        for start, end in windows(data, WINDOW_SIZE):
            chunk = data[start:end]

            # pad or trim chunk
            if len(chunk) < WINDOW_SIZE:
                chunk = np.pad(chunk, (0, WINDOW_SIZE - len(chunk)), mode="constant")
            elif len(chunk) > WINDOW_SIZE:
                chunk = chunk[:WINDOW_SIZE]

            # extract features and predict
            x = extract_features(chunk, sr)
            x = np.expand_dims(x, axis=0)
            p = model.predict(x, verbose=0)[0]
            probs.append(p)

            if len(data) < WINDOW_SIZE:
                break

        # average predictions
        if not probs:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="No predictions could be generated from the uploaded audio."
            )

        avg = np.mean(np.array(probs), axis=0)

        result = {
            "pathology": bool(avg[1] > avg[0]),
            "p_healthy": float(avg[0]),
            "p_pathology": float(avg[1])
        }

        print(f"Result: {result}")
        print(f"{'=' * 60}\n")
        return result

    except HTTPException as e:
        print(f"HTTP ERROR {e.status_code}: {e.detail}")
        print(f"{'=' * 60}\n")
        return JSONResponse(
            status_code=e.status_code,
            content={"detail": e.detail}
        )

    except Exception as e:
        print(f"ERROR: {type(e).__name__}: {str(e)}")
        print(traceback.format_exc())
        print(f"{'=' * 60}\n")
        return JSONResponse(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            content={"detail": "An internal server error occurred while processing the audio."}
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000, workers=1)