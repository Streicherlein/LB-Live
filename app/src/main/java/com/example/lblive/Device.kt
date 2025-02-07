// Datei: Device.kt
package com.example.lblive

data class Device(
    var name: String,
    val ip: String,
    var reachable: Boolean = false,
    var status: String = "red" // Standard: rot (nicht erreichbar)
)
