package com.example.weatherapplication.data

data class WeatherModel(
    val city: String = "",
    val time: String = "",
    val currentTemp: String = "",
    val condition: String = "",
    val icon: String = "",
    val maxTemp: String = "",
    val minTemp: String = "",
)
