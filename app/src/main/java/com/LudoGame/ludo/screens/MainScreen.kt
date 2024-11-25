package com.example.ludo.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ludo.R
import com.example.ludo.Screen
import com.example.ludo.viewmodels.LobbyViewModel
import io.garrit.android.multiplayer.Player


@Composable
fun Mainscreen(navController: NavController, lobbyViewModel: LobbyViewModel) {
    var showName by remember { mutableStateOf(false) }
    var playerName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Gray,
                        Color.DarkGray

                    )
                )
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
        Modifier.fillMaxHeight()
        ) {
                Image(
                    painter = painterResource(id = R.drawable.ludo2),
                    contentDescription = "LUDO Logo",
                    modifier = Modifier
                        .blur(10.dp)
                        .fillMaxSize(),
                        contentScale = ContentScale.Crop
//                .border(width = 1.dp, color = Color.Red, shape = RoundedCornerShape(0.dp))
                )
            Column(
                modifier = Modifier
                    .align(Alignment.Center) // Align the column to the center of the box
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = "LUDO",
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .animateContentSize(),
                    style = TextStyle(
                        fontSize = 65.sp,
                        fontWeight = FontWeight.W900,
                        color = Color.Black,
                        shadow = Shadow(
                            color = Color.Black, // Shadow color
                            offset = Offset(-2f, 2f), // Shadow position offset
                            blurRadius = 5f // Blur radius for the shadow
                        )
                    )
                )

                Button(
                    onClick = {
                        showName = true
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Enter your name",
                        fontSize = 18.sp
                    )
                }
                if (playerName.isNotBlank()) {
                    Text(
                        text = playerName,
                        color = Color.Red,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                if (showName) {
                    NameInput(
                        onNameEntered = {
                            playerName = it.trim()
                            showName = false
                            val player = Player(name = playerName)
                            lobbyViewModel.joinLobby(player)
                            navController.navigate(Screen.lobbyScreen.route)
                        })
                }
            }
        }

    }
}

@Composable
fun NameInput(onNameEntered: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        TextField(
            value = name,
            onValueChange = {
                name = it.replace("\n", "")
                errorMessage = null
            },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
        )
        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
        Button(
            onClick = {
                if (isValidName(name)) {
                    onNameEntered(name)
                } else {
                    errorMessage = errorFromName(name)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)

        ) {
            Text("Join lobby")
        }
    }
}


fun isValidName(name: String): Boolean {
    return name.trim().length in 3..10
}

fun errorFromName(name: String): String {
    if(name.trim().isEmpty())
        return "Please enter your name"
    if(name.trim().length < 3)
        return "Name must be longer"
    if(name.trim().length > 10)
        return "Name must be shorter than 10 characters long"
    return "Name"
}

@Preview(showBackground = true)
@Composable
fun MainscreenPreview() {
    val navController = rememberNavController()
    val lobbyViewModel = LobbyViewModel()
    Mainscreen(navController = navController, lobbyViewModel = lobbyViewModel)
}








