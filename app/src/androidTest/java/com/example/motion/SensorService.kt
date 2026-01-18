package com.example.motion

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SensorService(private val sensorManager: SensorManager) {

    private var Current: Sensor? = null

    private val Data = MutableStateFlow(FloatArray(3))
    val sensorData: StateFlow<FloatArray> = Data

    private val Listener = object : SensorEventListener
    {
        override fun onSensorChanged(event: SensorEvent)
        { // this function is run when something changes within the app.
            Data.tryEmit(event.values)
            Log.d("DEBUG", "Data: ${event.values[0]}")
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int)
        {
        }
    }

    fun startListening(sensorType: Int)
    {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        Current = sensor
        sensor?.let {
            sensorManager.registerListener(
                Listener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d("DEBUG", "Listening: $sensorType")
        }
    }

    fun stopListening()
    {
        sensorManager.unregisterListener(Listener)
        Current = null
        Log.d("DEBUG", "Stopped")
    }
}