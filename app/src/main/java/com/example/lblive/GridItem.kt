package com.example.lblive

data class GridItem(
    var type: FieldType,
    var id: Int // Eindeutige ID für Slider-Gruppen
)

enum class FieldType {
    EMPTY, MUTE, SLIDER1, SLIDER2, SLIDER3, SLIDER4
}
