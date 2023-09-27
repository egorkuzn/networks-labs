package com.example.weatherapplication.data

data class Place(
    val type: String,
    val name: String,
    val country: String,
    val city: String,
    val lng: Double,
    val lat: Double
)
