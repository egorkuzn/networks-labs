package com.example.weatherapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapplication.data.BaseParams

@Composable
fun InfoScreen() {
    Column(
        modifier = Modifier.background(Color.Black).fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = BaseParams.placeInfo.value.imgSource,
            contentDescription = "im20",
            modifier = Modifier.size(
                width = BaseParams.placeInfo.value.imgWidth.dp,
                height = BaseParams.placeInfo.value.imgHeight.dp),
        )

        Text(
            modifier = Modifier.fillMaxSize(),
            text = BaseParams.placeInfo.value.address,
            color = Color.Gray,
            style = TextStyle(fontSize = 12.sp),
            fontStyle = FontStyle.Italic
        )
        Text(
            modifier = Modifier.fillMaxSize(),
            text = BaseParams.placeInfo.value.descr.ifEmpty { "No info"},
            color = Color.White
        )
    }
}