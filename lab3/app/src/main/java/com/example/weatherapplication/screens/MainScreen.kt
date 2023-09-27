package com.example.weatherapplication.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.weatherapplication.Screen
import com.example.weatherapplication.data.BaseParams
import com.example.weatherapplication.data.WeatherModel
import com.example.weatherapplication.ui.theme.DarkBlue
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class)
@Composable
fun MainScreen(
    hoursList: MutableState<List<WeatherModel>>,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>,
    navController: NavController,
    context: Context
) {
    Image(
        painter = painterResource(
            id = BaseParams.mainBg
        ),
        contentDescription = "im1",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillHeight
    )

    Column {
        MainCard(currentDay, navController)
        HorizontalPager(
            count = 2
        ) {currentPage ->
            when(currentPage) {
                0 -> PlacesLayout(context, navController)
                1 -> TabLayout(hoursList, daysList)
            }
        }
    }
}

@Composable
fun PlacesLayout(context: Context, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(BaseParams.interestingPlacesList.value){
            item ->  ListInterestingPlaceItem(context, item, navController)
        }
    }
}

@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, navController: NavController) {
    Column(
        modifier = Modifier
            .padding(5.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().alpha(0.8f),
            shape = RoundedCornerShape(10.dp)
        ){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBlue)
                    .padding(top = 20.dp),
                horizontalAlignment = CenterHorizontally
            ) {
                TextButton( onClick = {
                    navController.navigate(Screen.InputScreen.route)
                },
                ) {
                    Text(
                        text = currentDay.value.city,
                        style = TextStyle(
                            fontSize = 24.sp,
                            color = Color.White
                        )
                    )
                }

                Text(
                    text = currentDay.value.currentTemp + "°C",
                    style = TextStyle(
                        fontSize = 65.sp,
                        color = Color.White
                    )
                )
                
                Text(
                    text = currentDay.value.condition,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )


                Text(
                    modifier = Modifier.padding(bottom = 10.dp),
                    text = "${currentDay.value.minTemp}°C/${currentDay.value.maxTemp}°C",
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}


@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(hoursList: MutableState<List<WeatherModel>>, daysList: MutableState<List<WeatherModel>>) {
    val pagerState = rememberPagerState()
    val tabList = listOf("HOURS", "DAYS")
    val tabIndex= pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    Column (
        modifier = Modifier
            .alpha(0.8f)
            .padding(
                start = 3.dp,
                end = 3.dp
            ).clip(RoundedCornerShape(5.dp)),
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            contentColor = DarkBlue
        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(text = text)
                    },
                )
            }
        }

        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { currentPage->
            val list = if(currentPage == 0) hoursList.value else daysList.value.drop(1)

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(list) {
                        item -> ListWeatherItem(item)
                }
            }
        }
    }
}
