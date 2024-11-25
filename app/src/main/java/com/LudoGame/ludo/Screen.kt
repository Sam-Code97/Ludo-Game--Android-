package com.example.ludo

//import com.example.ludo.screens.OpponentType

sealed class Screen (val route: String){
    object MainScreen : Screen("main_screen")
    object lobbyScreen: Screen("lobby_Screen/{playerName}")
    fun createRoute1(playerName : String): String {
        val route = "lobby_screen/$playerName"
        println("L: '$route'")
        return route
    }
    object GameScreen : Screen("game_screen/{player1}/{player2}"){
        fun createRoute(player1 : String, player2 : String): String {
            val route = "game_screen/$player1/$player2"
            println("R: '$route'")
            return route
        }
    }

}
