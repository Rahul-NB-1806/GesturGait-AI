const SAMPLE_RATE = 50;
const WINDOW_DURATION_SEC = 5;
const WINDOW_SIZE = SAMPLE_RATE * WINDOW_DURATION_SEC;

class RingBuffer {
  constructor(size = WINDOW_SIZE) {
    this.size = size;
    this.buffer = new Array(size);
    this.head = 0;
    this.count = 0;
  }

  push(sample) {
    this.buffer[this.head] = sample;
    this.head = (this.head + 1) % this.size;
    if (this.count < this.size) this.count++;
  }

  get length() {
    return this.count;
  }

  get isFull() {
    return this.count === this.size;
  }

  clear() {
    this.head = 0;
    this.count = 0;
  }

  toArray() {
    const result = new Array(this.count);
    const start = this.head - this.count;
    for (let i = 0; i < this.count; i++) {
      result[i] = this.buffer[(start + i + this.size) % this.size];
    }
    return result;
  }
}

export { RingBuffer, WINDOW_SIZE, WINDOW_DURATION_SEC, SAMPLE_RATE };
