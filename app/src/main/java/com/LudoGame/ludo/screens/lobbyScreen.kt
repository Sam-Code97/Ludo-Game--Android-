package com.example.ludo.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ludo.Screen
import com.example.ludo.viewmodels.GameViewModel
import com.example.ludo.viewmodels.LobbyViewModel
import io.garrit.android.multiplayer.Game
import io.garrit.android.multiplayer.Player
import io.garrit.android.multiplayer.ServerState
import io.garrit.android.multiplayer.SupabaseService
import io.garrit.android.multiplayer.SupabaseService.player
import io.garrit.android.multiplayer.SupabaseService.serverState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun lobbyScreen(
    navController: NavController = rememberNavController(),
    lobbyViewModel: LobbyViewModel,
    gameViewModel: GameViewModel
) {

    var invitedPlayers by remember { mutableStateOf(setOf<String>()) }
    var messageDuration by remember { mutableStateOf(2000L) }
    val gradient = listOf(Color.LightGray, Color.DarkGray)
    val serverState = gameViewModel.serverState.collectAsState()

    LaunchedEffect(serverState.value) {
        when (serverState.value) {
            ServerState.LOADING_GAME,
            ServerState.GAME -> {
                navController.navigate(Screen.GameScreen.route)
            }

            else -> {}
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.linearGradient(gradient))
    ) {
        Column {
            Button(
                onClick = { navController.navigateUp() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            )
            {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "")

            }
        }
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center
        )
        {
            Text(
                text = "Connected Players",
                style = TextStyle(Color.Black),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn() {
            items(lobbyViewModel.players) { connectedPlayer ->
                val coroutineScope = rememberCoroutineScope()

                Text(
                    text = connectedPlayer.name,
                    style = TextStyle(Color.Black),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            invitedPlayers = invitedPlayers
                                .toMutableSet()
                                .apply {
                                    add(connectedPlayer.name)
                                }
                            lobbyViewModel.sendInvite(connectedPlayer)

                            coroutineScope.launch {
                                delay(messageDuration)
                                invitedPlayers = emptySet()
                            }

                        }
                        .padding(16.dp)
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .padding(20.dp)

                )
                if (invitedPlayers.contains(connectedPlayer.name)) {
                    Text(
                        text = "Invited ${connectedPlayer.name}",
                        style = TextStyle(Color.Green)
                    )
                }
            }
        }
        LazyColumn {
            items(lobbyViewModel.games)
            { game ->
                Text(
                    text = "Invited by: ${game.player1.name}",
                    style = TextStyle(Color.Black),
                    fontWeight = FontWeight.Bold
                )

                Row {
                    Button(
                        onClick = {
                            lobbyViewModel.AcceptInvite(game)
//                            navController.navigate(Screen.GameScreen.route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                    )
                    {

                        Text(
                            text = "Accept",
                            style = TextStyle(Color.Black),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = {
                            lobbyViewModel.DeclineInvite(game)

                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    )
                    {
                        Text(
                            text = "Decline",
                            style = TextStyle(Color.White),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }
        }
    }

}

