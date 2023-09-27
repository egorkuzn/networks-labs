package com.example.weatherapplication.data

data class PlaceBaseInfo(
    val xid: String,
    val name: String,
    val dist: Double,
    val rate: String,
    val wikidata: String,
    val kinds: String,
    val lon: Double,
    val lat: Double
)
