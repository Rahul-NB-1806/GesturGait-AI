package com.example.gesturgaitai.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

data class SensorSample(
    val timestamp: Long,
    val ax: Float, val ay: Float, val az: Float,
    val gx: Float, val gy: Float, val gz: Float
)

class MotionSensorManager(
    private val sensorManager: SensorManager,
    private val onWindowReady: (List<SensorSample>) -> Unit
) : SensorEventListener {

    companion object {
        const val SAMPLE_RATE_US = 20000 // 50 Hz
        const val WINDOW_MS = 5000L
    }

    private val buffer = mutableListOf<SensorSample>()
    private var lastWindowTime = System.currentTimeMillis()
    private var accel: FloatArray? = null
    private var gyro: FloatArray? = null

    private val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    fun start() {
        accelSensor?.let {
            sensorManager.registerListener(this, it, SAMPLE_RATE_US)
        }
        gyroSensor?.let {
            sensorManager.registerListener(this, it, SAMPLE_RATE_US)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = event.timestamp / 1000000L // nanos to ms

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                accel = event.values.clone()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyro = event.values.clone()
            }
        }

        if (accel != null && gyro != null) {
            buffer.add(SensorSample(now, accel!![0], accel!![1], accel!![2], gyro!![0], gyro!![1], gyro!![2]))

            val elapsed = now - lastWindowTime
            if (elapsed >= WINDOW_MS && buffer.size >= 50) {
                val window = buffer.toList()
                buffer.clear()
                lastWindowTime = now
                onWindowReady(window)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun reset() {
        buffer.clear()
        lastWindowTime = System.currentTimeMillis()
        accel = null
        gyro = null
    }
}
