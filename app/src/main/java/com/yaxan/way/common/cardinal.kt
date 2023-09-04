package com.yaxan.way.common

enum class Cardinal(val letter: String, val degree: Int) {
    NORTH("N", 0),
    NORTHEAST("NE", 45),
    EAST("E", 90),
    SOUTHEAST("SE", 135),
    SOUTH("S", 180),
    SOUTHWEST("SW", 225),
    WEST("W", 270),
    NORTHWEST("NW", 315),
    ;

    companion object {
        fun fromDegree(degree: Int): Cardinal {
            val divisor: Int = 360 / values().size
            val coci = degree / divisor
            val resto = degree % divisor
            return if (resto <= divisor / 2) {
                values()[coci % values().size]
            } else {
                values()[(coci + 1) % values().size]
            }
        }
    }
}