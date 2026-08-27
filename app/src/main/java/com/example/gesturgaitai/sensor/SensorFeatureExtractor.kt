package com.example.gesturgaitai.sensor

import kotlin.math.abs
import kotlin.math.sqrt

data class SensorFeatures(
    val stepCount: Int = 0,
    val avgStepTime: Double = 0.0,
    val peakFrequency: Double = 0.0,
    val tremorFrequency: Double = 0.0,
    val movementStability: Double = 0.0
)

object SensorFeatureExtractor {

    fun extract(samples: List<SensorSample>): SensorFeatures {
        if (samples.size < 10) return SensorFeatures()

        val magnitudes = samples.map { s ->
            sqrt((s.ax * s.ax + s.ay * s.ay + s.az * s.az).toDouble())
        }

        val gravities = samples.map { s ->
            sqrt((s.gx * s.gx + s.gy * s.gy + s.gz * s.gz).toDouble())
        }

        val steps = detectSteps(magnitudes)
        val avgStepTime = computeAvgStepTime(samples, steps)
        val peakFreq = computePeakFrequency(magnitudes)
        val tremorFreq = computeTremorFrequency(gravities)
        val stability = computeStability(magnitudes)

        return SensorFeatures(
            stepCount = steps,
            avgStepTime = avgStepTime,
            peakFrequency = peakFreq,
            tremorFrequency = tremorFreq,
            movementStability = stability
        )
    }

    private fun detectSteps(magnitudes: List<Double>): Int {
        val mean = magnitudes.average()
        val threshold = mean * 1.15
        var count = 0
        var crossed = false
        for (m in magnitudes) {
            if (m > threshold && !crossed) {
                count++
                crossed = true
            } else if (m <= mean) {
                crossed = false
            }
        }
        return count
    }

    private fun computeAvgStepTime(samples: List<SensorSample>, steps: Int): Double {
        if (steps < 2) return 0.0
        val duration = samples.last().timestamp - samples.first().timestamp
        return duration.toDouble() / (steps * 1000.0)
    }

    private fun computePeakFrequency(magnitudes: List<Double>): Double {
        val n = magnitudes.size
        if (n < 4) return 0.0
        val fft = realFft(magnitudes)
        val half = fft.size / 2
        if (half < 2) return 0.0
        var maxIdx = 1
        for (i in 2 until half) {
            if (fft[i].magnitude() > fft[maxIdx].magnitude()) maxIdx = i
        }
        val sampleRate = 50.0
        return maxIdx * sampleRate / n
    }

    private fun computeTremorFrequency(gyroMagnitudes: List<Double>): Double {
        val n = gyroMagnitudes.size
        if (n < 8) return 0.0
        val fft = realFft(gyroMagnitudes)
        val half = fft.size / 2
        val sampleRate = 50.0
        var bandPower = 0.0
        for (i in 1 until half) {
            val freq = i * sampleRate / n
            if (freq in 4.0..6.0) {
                bandPower += fft[i].magnitude()
            }
        }
        return bandPower / half.toDouble()
    }

    private fun computeStability(magnitudes: List<Double>): Double {
        if (magnitudes.size < 2) return 0.0
        val mean = magnitudes.average()
        val variance = magnitudes.map { (it - mean) * (it - mean) }.average()
        return sqrt(variance)
    }

    private data class Complex(val re: Double, val im: Double) {
        fun magnitude() = sqrt(re * re + im * im)
    }

    private fun realFft(values: List<Double>): List<Complex> {
        val n = nextPowerOf2(values.size)
        val a = values.toMutableList()
        while (a.size < n) a.add(0.0)
        return fft(a)
    }

    private fun nextPowerOf2(n: Int): Int {
        var p = 1
        while (p < n) p *= 2
        return p
    }

    private fun fft(a: List<Double>): List<Complex> {
        val n = a.size
        if (n == 1) return listOf(Complex(a[0], 0.0))

        val even = a.filterIndexed { i, _ -> i % 2 == 0 }
        val odd = a.filterIndexed { i, _ -> i % 2 == 1 }

        val fftEven = fft(even)
        val fftOdd = fft(odd)

        val result = MutableList(n) { Complex(0.0, 0.0) }
        for (k in 0 until n / 2) {
            val t = Complex(
                cos(2 * Math.PI * k / n),
                -sin(2 * Math.PI * k / n)
            )
            val oddK = Complex(
                fftOdd[k].re * t.re - fftOdd[k].im * t.im,
                fftOdd[k].re * t.im + fftOdd[k].im * t.re
            )
            result[k] = Complex(fftEven[k].re + oddK.re, fftEven[k].im + oddK.im)
            result[k + n / 2] = Complex(fftEven[k].re - oddK.re, fftEven[k].im - oddK.im)
        }
        return result
    }

    private fun cos(x: Double) = kotlin.math.cos(x)
    private fun sin(x: Double) = kotlin.math.sin(x)
}
