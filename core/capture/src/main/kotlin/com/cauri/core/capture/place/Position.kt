package com.cauri.core.capture.place

/**
 * Where the phone is, as Android located it.
 *
 * @property latitude Latitude, in degrees.
 * @property longitude Longitude, in degrees.
 * @property accuracyMeters Radius around it within which the phone most likely is, in metres.
 */
data class Position(val latitude: Double, val longitude: Double, val accuracyMeters: Int)
