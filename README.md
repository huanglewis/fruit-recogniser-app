# Fruit Recogniser App

This is an Android application that uses a TensorFlow Lite model to classify fruits (Apple, Banana, Orange, Mixed) from images captured or selected by the user.

Built with Kotlin and XML layouts, optimized for on-device inference using a custom-trained `.tflite` model.

---

## Features

- Capture or choose an image  
- Offline classification using a 157 MB TFLite model  
- Recognizes: Apple, Banana, Orange, or Mixed  
- Clean UI with result display and confidence score  
- Works fully offline — no internet needed  

---

## Model Info

- **Format**: `fruit_classifier_2_float32.tflite`  
- **Size**: ~157 MB (stored via Git LFS)  
- **Input Size**: 100x100 normalized image  
- **Framework**: Trained in PyTorch, exported to TensorFlow Lite  
- **Classes**: 4 (`apple`, `banana`, `orange`, `mixed`)  

---

## Tech Stack

- Android SDK (Kotlin)  
- TensorFlow Lite Interpreter  
- ByteBuffer-based preprocessing  
- Manual image normalization:  
  `(pixel / 255 - 0.5) / 0.5`

---

## How to Run

1. Clone this repo (**requires Git LFS**):
   ```bash
   git lfs install
   git clone https://github.com/huanglewis/fruit-recogniser-app.git
