package com.example.ludo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ludo.screens.GameScreen
import com.example.ludo.screens.Mainscreen
//import com.example.ludo.screens.OpponentType
import com.example.ludo.ui.theme.LudoTheme
import com.example.ludo.viewmodels.GameViewModel
import com.example.ludo.viewmodels.LobbyViewModel
import com.example.ludo.screens.lobbyScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LudoTheme {
                val navController = rememberNavController()

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                }
                NavHost(
                    navController = navController,
                    startDestination = Screen.MainScreen.route,
                ) {
                    composable(Screen.MainScreen.route) {
                        Mainscreen(navController, lobbyViewModel = LobbyViewModel())
                    }
                    composable(
                        Screen.GameScreen.route,
                        arguments = listOf(
                            navArgument("player1") {
                                type = NavType.StringType
                            },
                            navArgument("player2") {
                                type = androidx.navigation.NavType.StringType
                            }
                        )
                    ) {  navBackStackEntry ->
                        // Retrieve the playerName from the arguments
                        val player1: String? =
                            navBackStackEntry.arguments?.getString("player1")
                        val player2: String? =
                            navBackStackEntry.arguments?.getString("player2")
                        val gameViewModel = GameViewModel()

                        if (player1 != null && player2 != null)
                            GameScreen(
                                navController = navController,
                                gameViewModel = GameViewModel(),

                                player1 = player1,
                                player2 = player2,
                                lobbyViewModel = LobbyViewModel()

                            )

                    }
                    composable(
                        Screen.lobbyScreen.route,
                        arguments = listOf(
                            navArgument("playerName") {
                                type = NavType.StringType
                            }
                        )
                    ) {  navBackStackEntry ->
                        // Retrieve the playerName from the arguments
                        val playerName: String? =
                            navBackStackEntry.arguments?.getString("playerName")

                        // Create or obtain the necessary ViewModel for LobbyScreen
                        val lobbyViewModel = LobbyViewModel()

                        // You can pass the necessary dependencies to your LobbyScreen
                        lobbyScreen(
                            navController,
                            lobbyViewModel,
                            gameViewModel = GameViewModel(),
                        )
                    }
                }
            }
        }
    }
}


