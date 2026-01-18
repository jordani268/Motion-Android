package com.example.motion.feature.velocity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.motion.SensorService
import kotlinx.coroutines.flow.*

class VelocityViewModel(private val sensorService: SensorService) : ViewModel() {

    private val currentVelocity0 = MutableStateFlow(0f)
    val currentVelocity: StateFlow<Float> = currentVelocity0

    private val velocityHistory0 = MutableStateFlow<List<Float>>(emptyList())
    val velocityHistory: StateFlow<List<Float>> = velocityHistory0

    private var lastTimestamp: Long = 0L

    init { startVelocityCalculation() }

    private fun startVelocityCalculation() {
        sensorService.sensorData
            .onEach { sensorValues ->
                val currentTime = System.currentTimeMillis()

                if (lastTimestamp != 0L) {
                    val timeDelta = (currentTime - lastTimestamp) / 1000f

                    val accelerationMagnitude = calculateMagnitude(sensorValues)
                    val velocityDelta = accelerationMagnitude * timeDelta

                    currentVelocity0.update { it + velocityDelta }

                    velocityHistory0.update { history ->
                        (history + currentVelocity0.value).takeLast(100)
                    }
                }
                lastTimestamp = currentTime
            }
            .launchIn(viewModelScope)
    }

    private fun calculateMagnitude(values: FloatArray): Float {
        return kotlin.math.sqrt(
            values[0] * values[0] + values[1] * values[1] + values[2] * values[2]
        )
    }

    fun startMeasuring() {
        sensorService.startListening(android.hardware.Sensor.TYPE_ACCELEROMETER)
        lastTimestamp = System.currentTimeMillis()
    }

    fun stopMeasuring()
    {
        sensorService.stopListening()
        currentVelocity0.value = 0f
        velocityHistory0.value = emptyList()
        lastTimestamp = 0L
    }
}