package com.example.weatherapplication

sealed class Screen(val route: String) {
    object MainScreen : Screen("main_screen")
    object InputScreen: Screen("input_screen")
    object ChoseScreen: Screen("chose_screen")
    object InfoScreen: Screen("info_screen")
}