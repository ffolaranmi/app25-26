import tensorflow as tf

print("Loading model...")
model = tf.keras.models.load_model("CNN_updated.h5", compile=False)

print("Model loaded successfully!")
print("Resaving model in new format...")
model.save("CNN_updated.h5")

print("SUCCESS! Your model has been fixed and saved.")
