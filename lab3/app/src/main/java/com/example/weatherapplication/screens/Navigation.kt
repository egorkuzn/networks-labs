package com.example.weatherapplication

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapplication.data.BaseParams
import com.example.weatherapplication.data.Place
import com.example.weatherapplication.data.WeatherModel
import com.example.weatherapplication.screens.ChoseScreen
import com.example.weatherapplication.screens.InputScreen
import com.example.weatherapplication.screens.MainScreen
import com.example.weatherapplication.screens.InfoScreen
import org.json.JSONObject

@Composable
fun Navigation(context: Context) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route,
    ) {
        composable(route = Screen.MainScreen.route) {
            val daysList = remember{
                mutableStateOf(listOf<WeatherModel>())
            }
            val hoursList = remember{
                mutableStateOf(listOf<WeatherModel>())
            }
            val currentDay = remember {
                mutableStateOf(WeatherModel())
            }
            val placesList = remember {
                mutableStateOf(listOf<Place>())
            }

            Log.d("MyLog", "Starting getting data")
            getData(BaseParams.countOfBoxes, BaseParams.city, context, daysList, hoursList, currentDay)
            MainScreen(hoursList, daysList, currentDay, navController, context)
        }

        composable(route = Screen.InputScreen.route) {
            InputScreen(navController, context)
        }

        composable(route = Screen.ChoseScreen.route) {
            ChoseScreen(context, navController)
        }

        composable(route = Screen.InfoScreen.route) {
            InfoScreen()
        }
    }
}

fun getData(
    count: Int,
    city: String,
    context: Context,
    daysList: MutableState<List<WeatherModel>>,
    hoursList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>
) {
    val url = "https://api.weatherapi.com/v1/forecast.json?" +
            "key=${BaseParams.weatherApiKey}" +
            "&q=$city" +
            "&days=$count" +
            "&aqi=no" +
            "&alerts=no\n"
    val queue = Volley.newRequestQueue(context)
    val sRequest = StringRequest(
        Request.Method.GET,
        url,
        {
                response ->
            Log.d("MyLog", "Response: $response")
            hoursList.value = getWeatherByHours(response)
            daysList.value = getWeatherByDays(response)
            currentDay.value = daysList.value[0]
        },
        {
            Log.d("MyLog", "VolleyError: $it")
        }
    )

    queue.add(sRequest)
}

fun getWeatherByHours(response: String): List<WeatherModel> {
    if(response.isEmpty()) return listOf()
    var countOfCards = 0
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val currentTime = mainObject.getJSONObject("current").getString("last_updated")
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for(i in 0..1) {
        val day = days[i] as JSONObject
        Log.d("MyLog", "Response: $day")
        val hours = day.getJSONArray("hour")

        for(i in 0 until hours.length()){
            val item = hours[i] as JSONObject
            val time = item.getString("time")

            if(currentTime > time)
                continue
            else if(countOfCards++ == 12)
                return list

            list.add(
                WeatherModel(
                    city,
                    time.substring(11),
                    item.getString("temp_c"),
                    item.getJSONObject("condition")
                        .getString("text"),
                    item.getJSONObject("condition")
                        .getString("icon")
                )
            )
        }
    }

    return listOf()
}

fun getWeatherByDays(response: String): List<WeatherModel>{
    if(response.isEmpty()) return listOf()
    val list = ArrayList<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for(i in 0 until days.length()){
        val item = days[i] as JSONObject

        list.add(
            WeatherModel(
                city,
                toDayMonth(item.getString("date")),
                "",
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c")
            )
        )
    }

    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c")
    )
    return list
}

fun toDayMonth(string: String): String {
    val dayDigit = string.substring(8,10)
    val monthDigit = string.substring(5,7)
    return "$dayDigit " + when(monthDigit){
        "01" -> "January"
        "02" -> "February"
        "03" -> "March"
        "04" -> "April"
        "05" -> "May"
        "06" -> "June"
        "07" -> "July"
        "08" -> "August"
        "09" -> "September"
        "10" -> "October"
        "11" -> "November"
        "12" -> "December"
        else -> ""
    }
}
