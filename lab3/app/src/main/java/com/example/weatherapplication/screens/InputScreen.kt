package com.example.weatherapplication.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavController
import com.example.weatherapplication.Screen
import com.example.weatherapplication.data.BaseParams

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun InputScreen(navController: NavController, context: Context) {
    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val text = remember {
            mutableStateOf("")
        }

        TextField(
            label = {
                Text("Place name")
            },
            value = text.value,
            onValueChange = { newText: String ->
                text.value = newText
            },
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                autoCorrect = true,
            ),
            trailingIcon = {
                IconButton(onClick = {
                    Log.d("Mylog", "Go on getting place info")
                    BaseParams.getListOfPlaces(context, text.value)
                    Log.d("Mylog", "Out of getting place info")
                    navController.navigate(Screen.ChoseScreen.route)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "Done Icon"
                    )
                }
            }
        )
    }
}
