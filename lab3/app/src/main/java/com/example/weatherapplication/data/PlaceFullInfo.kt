package com.example.weatherapplication.data

import androidx.compose.ui.text.AnnotatedString

data class PlaceFullInfo(
    val wikiLink: String = "",
    val descr: String = "",
    val imgSource: String = "",
    val imgHeight: Int = 0,
    val imgWidth: Int = 0,
    val address: String = ""
)
