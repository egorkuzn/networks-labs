package com.example.weatherapplication.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.weatherapplication.Screen
import com.example.weatherapplication.data.BaseParams
import com.example.weatherapplication.data.Place
import com.example.weatherapplication.data.PlaceBaseInfo
import com.example.weatherapplication.data.WeatherModel
import com.example.weatherapplication.ui.theme.DarkBlue
import kotlin.math.roundToInt

@Composable
fun ListWeatherItem(item: WeatherModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp),
        shape = RoundedCornerShape(5.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(DarkBlue),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 5.dp,
                    bottom = 5.dp
                )
            ) {
                Text(
                    text = item.time,
                    color = Color.White
                )

                Text(
                    text = item.condition,
                    color = Color.White
                )
            }

            Row (
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.currentTemp.ifEmpty { "${item.minTemp}°C/${item.maxTemp}" } + "°C",
                    color = Color.White,
                    style = TextStyle(fontSize = 25.sp)
                )

                AsyncImage(
                    model = "https:${item.icon}",
                    contentDescription = "im5",
                    modifier = Modifier.size(60.dp)
                        .padding(end = 8.dp)
                )
            }

        }
    }
}

@Composable
fun ListPlaceButtonItem(context: Context, item: Place, navController: NavController){
    TextButton(
        onClick = {
            BaseParams.updatePlace(context, item)
            navController.navigate(Screen.MainScreen.route)
        }
    ) {
        Text(text = "${item.country}, ${item.city}, ${item.type}")
    }
}

@Composable
fun ListInterestingPlaceItem(context: Context, item: PlaceBaseInfo, navController: NavController) {
    TextButton(
        shape = RoundedCornerShape(0.dp),
        onClick = {
        BaseParams.updateInterestingPlace(context, item.xid)
        navController.navigate(Screen.InfoScreen.route)
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 10.dp,
                    start = 5.dp,
                    end = 5.dp
                ),
            shape = RoundedCornerShape(30.dp)
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 10.dp,
                    bottom = 10.dp,
                    end = 20.dp
                ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${item.dist.roundToInt()} meters from you"
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "★",
                            style = TextStyle(fontSize = 30.sp)
                        )

                        Text(
                            text = "${item.rate}",
                            style = TextStyle(fontSize = 50.sp)
                        )
                    }

                    Text(
                        modifier = Modifier.padding(start = 10.dp),
                        text = item.name,
                        style = TextStyle(fontSize = 20.sp)
                    )
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = kindsFormat(item.kinds)
                )
            }
        }
    }
}

fun kindsFormat(kinds: String): String {
    var cleanString = kinds.replace("_", " ")
    cleanString = cleanString.replace(",", ", ")
    cleanString = cleanString.replaceRange(0, 1, cleanString[0].uppercase())
    return cleanString
}

