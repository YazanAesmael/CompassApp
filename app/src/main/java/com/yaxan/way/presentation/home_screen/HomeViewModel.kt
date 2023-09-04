package com.yaxan.way.presentation.home_screen

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val compass: Compass,
    private val cardinal: SensorDataManager
): ViewModel() {

    val gravity = compass.gravityValue
    val geomagnetic = compass.geomagneticValue
    val heading = compass.headingValue

    val north = cardinal.north

    fun startSensors() { compass.startListening() }

    fun startCardinal() { cardinal.init() }

}

const val alpha = 0.1f // Smoothing factor (0 < alpha < 1)
var filteredDegree = 0.0f

class SensorDataManager (context: Context): SensorEventListener {
    private val sensorManager by lazy {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private var _north = MutableStateFlow(0.0f)
    val north: StateFlow<Float>
        get() = _north.asStateFlow()

    fun init() {
        sensorManager.registerListener(
            this, sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    val data: Channel<Float> = Channel(Channel.UNLIMITED)

    override fun onSensorChanged(event: SensorEvent) {
        val degree = event.values[0].roundToInt().toFloat()

        filteredDegree = alpha * degree + (1 - alpha) * filteredDegree
        data.trySend(filteredDegree.roundToInt().toFloat())
        Log.d("app_tag", "$filteredDegree & $degree")

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun cancel() {
        sensorManager.unregisterListener(this);
    }
}

class Compass(context: Context) {
    private val sensorManager = ContextCompat.getSystemService(context, SensorManager::class.java)
    private val heading: MutableState<Float> = mutableStateOf(0f)
    private var gravity = FloatArray(3)
    private var geomagnetic = FloatArray(3)

    private var _gravityValue = MutableStateFlow("")
    val gravityValue: StateFlow<String>
        get() = _gravityValue.asStateFlow()

    private var _geomagneticValue = MutableStateFlow("")
    val geomagneticValue: StateFlow<String>
        get() = _geomagneticValue.asStateFlow()

    private var _headingValue = MutableStateFlow(0.0f)
    val headingValue: StateFlow<Float>
        get() = _headingValue.asStateFlow()

    private val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && event.values.size >= 3) {
                gravity = event.values
                _gravityValue.value = event.values.contentToString()

            } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD && event.values.size >= 3) {
                geomagnetic = event.values
                _geomagneticValue.value = event.values.contentToString()

            }

            // Calculate heading based on processed sensor data
            val rotationMatrix = FloatArray(9)
            val inclinationMatrix = FloatArray(9)
            val remappedRotationMatrix = FloatArray(9)
            val orientationAngles = FloatArray(3)

            SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, gravity, geomagnetic)
            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            val azimuthRadians = orientationAngles[0]
            val azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()

            // Update the heading state
            heading.value = azimuthDegrees
            _headingValue.value = azimuthDegrees
        }
    }

    fun startListening() {
        val accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        sensorManager?.registerListener(
            sensorEventListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager?.registerListener(
            sensorEventListener,
            magnetometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    fun stopListening() {
        sensorManager?.unregisterListener(sensorEventListener)
    }
}