package com.yaxan.way.domain.utils

import android.hardware.GeomagneticField
import android.hardware.SensorManager
import android.location.Location
import androidx.annotation.StringRes
import com.yaxan.way.R
import kotlin.math.roundToInt

private const val AZIMUTH = 0
private const val AXIS_SIZE = 3
private const val ROTATION_MATRIX_SIZE = 9

object MathUtils {

    @JvmStatic
    fun calculateAzimuth(rotationVector: RotationVector, displayRotation: DisplayRotation): Azimuth {
        val rotationMatrix = getRotationMatrix(rotationVector)
        val remappedRotationMatrix = remapRotationMatrix(rotationMatrix, displayRotation)
        val orientationInRadians = SensorManager.getOrientation(remappedRotationMatrix, FloatArray(AXIS_SIZE))
        val azimuthInRadians = orientationInRadians[AZIMUTH]
        val azimuthInDegrees = Math.toDegrees(azimuthInRadians.toDouble()).toFloat()
        return Azimuth(azimuthInDegrees)
    }

    private fun getRotationMatrix(rotationVector: RotationVector): FloatArray {
        val rotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector.toArray())
        return rotationMatrix
    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, displayRotation: DisplayRotation): FloatArray {
        return when (displayRotation) {
            DisplayRotation.ROTATION_0 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Y)
            DisplayRotation.ROTATION_90 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X)
            DisplayRotation.ROTATION_180 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Y)
            DisplayRotation.ROTATION_270 -> remapRotationMatrix(rotationMatrix, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_X)
        }
    }

    private fun remapRotationMatrix(rotationMatrix: FloatArray, newX: Int, newY: Int): FloatArray {
        val remappedRotationMatrix = FloatArray(ROTATION_MATRIX_SIZE)
        SensorManager.remapCoordinateSystem(rotationMatrix, newX, newY, remappedRotationMatrix)
        return remappedRotationMatrix
    }

    @JvmStatic
    fun getMagneticDeclination(location: Location): Float {
        val latitude = location.latitude.toFloat()
        val longitude = location.longitude.toFloat()
        val altitude = location.altitude.toFloat()
        val time = location.time
        val geomagneticField = GeomagneticField(latitude, longitude, altitude, time)
        return geomagneticField.declination
    }

    fun getClosestNumberFromInterval(number: Float, interval: Float): Float =
        (number / interval).roundToInt() * interval

    fun isAzimuthBetweenTwoPoints(azimuth: Azimuth, pointA: Azimuth, pointB: Azimuth): Boolean {
        val aToB = (pointB.degrees - pointA.degrees + 360f) % 360f
        val aToAzimuth = (azimuth.degrees - pointA.degrees + 360f) % 360f
        return aToB <= 180f != aToAzimuth > aToB
    }
}

data class RotationVector(val x: Float, val y: Float, val z: Float) {
    fun toArray(): FloatArray = floatArrayOf(x, y, z)
}

enum class DisplayRotation {
    ROTATION_0,
    ROTATION_90,
    ROTATION_180,
    ROTATION_270
}

class Azimuth(_degrees: Float) {

    init {
        if (!_degrees.isFinite()) {
            throw IllegalArgumentException("Degrees must be finite but was '$_degrees'")
        }
    }

    val degrees = normalizeAngle(_degrees)

    val roundedDegrees = normalizeAngle(_degrees.roundToInt().toFloat()).toInt()

    val cardinalDirection: CardinalDirection = when (degrees) {
        in 22.5f until 67.5f -> CardinalDirection.NORTHEAST
        in 67.5f until 112.5f -> CardinalDirection.EAST
        in 112.5f until 157.5f -> CardinalDirection.SOUTHEAST
        in 157.5f until 202.5f -> CardinalDirection.SOUTH
        in 202.5f until 247.5f -> CardinalDirection.SOUTHWEST
        in 247.5f until 292.5f -> CardinalDirection.WEST
        in 292.5f until 337.5f -> CardinalDirection.NORTHWEST
        else -> CardinalDirection.NORTH
    }

    private fun normalizeAngle(angleInDegrees: Float): Float {
        return (angleInDegrees + 360f) % 360f
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Azimuth

        if (degrees != other.degrees) return false

        return true
    }

    override fun hashCode(): Int {
        return degrees.hashCode()
    }

    override fun toString(): String {
        return "Azimuth(degrees=$degrees)"
    }

    operator fun plus(degrees: Float) = Azimuth(this.degrees + degrees)

    operator fun minus(degrees: Float) = Azimuth(this.degrees - degrees)

    operator fun compareTo(azimuth: Azimuth) = this.degrees.compareTo(azimuth.degrees)
}

private data class SemiClosedFloatRange(val fromInclusive: Float, val toExclusive: Float)

private operator fun SemiClosedFloatRange.contains(value: Float) = fromInclusive <= value && value < toExclusive
private infix fun Float.until(to: Float) = SemiClosedFloatRange(this, to)


enum class CardinalDirection(@StringRes val labelResourceId: Int) {
    NORTH(R.string.cardinal_direction_north),
    NORTHEAST(R.string.cardinal_direction_northeast),
    EAST(R.string.cardinal_direction_east),
    SOUTHEAST(R.string.cardinal_direction_southeast),
    SOUTH(R.string.cardinal_direction_south),
    SOUTHWEST(R.string.cardinal_direction_southwest),
    WEST(R.string.cardinal_direction_west),
    NORTHWEST(R.string.cardinal_direction_northwest)
}