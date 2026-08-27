function fft(samples) {
  const N = samples.length;
  if (N <= 1) return samples.map(x => [x, 0]);

  const half = Math.floor(N / 2);
  const even = fft(samples.filter((_, i) => i % 2 === 0));
  const odd = fft(samples.filter((_, i) => i % 2 === 1));

  const result = new Array(N);
  for (let k = 0; k < half; k++) {
    const angle = (-2 * Math.PI * k) / N;
    const tReal = Math.cos(angle) * odd[k][0] - Math.sin(angle) * odd[k][1];
    const tImag = Math.sin(angle) * odd[k][0] + Math.cos(angle) * odd[k][1];
    result[k] = [even[k][0] + tReal, even[k][1] + tImag];
    result[k + half] = [even[k][0] - tReal, even[k][1] - tImag];
  }
  return result;
}

function magnitude(complex) {
  return Math.sqrt(complex[0] * complex[0] + complex[1] * complex[1]);
}

function powerSpectrum(samples) {
  const spectrum = fft(samples);
  return spectrum.map(magnitude);
}

function dominantFrequency(samples, sampleRate = 50) {
  const N = samples.length;
  const spectrum = powerSpectrum(samples);
  let maxIdx = 0;
  let maxMag = 0;
  for (let i = 0; i < Math.floor(N / 2); i++) {
    if (spectrum[i] > maxMag) {
      maxMag = spectrum[i];
      maxIdx = i;
    }
  }
  return (maxIdx * sampleRate) / N;
}

function bandPower(samples, lowHz, highHz, sampleRate = 50) {
  const N = samples.length;
  const spectrum = powerSpectrum(samples);
  let totalPower = 0;
  const lowBin = Math.floor((lowHz * N) / sampleRate);
  const highBin = Math.ceil((highHz * N) / sampleRate);
  for (let i = lowBin; i < Math.min(highBin, Math.floor(N / 2)); i++) {
    totalPower += spectrum[i];
  }
  return totalPower / (highBin - lowBin + 1);
}

export { fft, magnitude, powerSpectrum, dominantFrequency, bandPower };
