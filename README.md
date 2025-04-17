# Multi threading with image processing using java
The aim of this project is to convert PNG or JPG files from the RGB format into three other output formats using parallelization:

## 1. A grayscale image
## 2. A histogram
## 3. An image with altered brightness

# Running
I make a video for project run if you want to see it [🔗 ](https://drive.google.com/file/d/1pyzyTL-A8Exs3lW6Y7l2NrYagIfaYuLW/view?usp=drive_link)
# Parallelization
User can select mode of parallelism into three tybes:

### 1-Blocking

Typically splits the image into chunks and processes them—often using multiple threads—but then waits (blocks) on each chunk to finish before returning the final image.

During that wait, if you ran it on the UI thread, the interface would freeze until completion.

### 2-Non‑blocking

Kicks off work asynchronously (e.g., with callbacks, futures, or an executor), so the GUI thread isn’t held up.

You could (in a more advanced version) show a progress bar or let the user interact while conversion is in progress.

Once all pieces are done, it assembles them and returns the result, all without locking up the UI.

### 3-Single‑threaded

Processes every pixel in sequence on one thread (usually the calling thread).

It’s the simplest approach—no concurrency at all—but is the slowest for large images because it can’t take advantage of multiple CPU cores.

## Output
Image that converted to what you selected before and time for running process.

