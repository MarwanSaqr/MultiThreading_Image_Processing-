# Multi-threading with Image Processing using Java

This project demonstrates the use of parallelization in Java to process PNG or JPG files by converting them from RGB format into three different output types:

1. **Grayscale image**  
2. **Histogram**  
3. **Image with altered brightness**  

---

## 🎬 Demo

I’ve recorded a video showing how the project runs. You can watch it here:  
[🔗 Project Demo](https://drive.google.com/file/d/1pyzyTL-A8Exs3lW6Y7l2NrYagIfaYuLW/view?usp=drive_link)

---

## ⚙️ Parallelization Modes

You can choose from three modes of parallelism:

### 1. **Blocking**

- Splits the image into chunks and processes them (often using multiple threads).  
- Waits (blocks) until all chunks are processed before assembling the final image.  
- If run on the UI thread, it may freeze the interface until completion.

### 2. **Non-blocking**

- Executes processing asynchronously (using callbacks, futures, or an executor).  
- Keeps the UI responsive — ideal for real-time progress bars or interactive interfaces.  
- Once all chunks are processed, the image is assembled and returned without freezing the UI.

### 3. **Single-threaded**

- Processes each pixel sequentially on a single thread (usually the calling thread).  
- The simplest method — no concurrency involved.  
- Slowest option for large images as it doesn't utilize multiple CPU cores.

---

## 🖼️ Output

The output includes:

- The image converted into the selected format  
- The time taken to complete the processing  

---
