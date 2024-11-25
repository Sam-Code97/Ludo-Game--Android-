package com.example.ludo.screens

import android.graphics.Color.rgb
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth


import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.ludo.viewmodels.GameViewModel
import com.example.ludo.viewmodels.LobbyViewModel
import com.example.ludo.viewmodels.Token
import io.garrit.android.multiplayer.GameResult
import io.garrit.android.multiplayer.SupabaseService

@Composable
fun GameScreen(
    gameViewModel: GameViewModel,
    navController: NavController,
    player1: String,
    player2: String,
    lobbyViewModel: LobbyViewModel
) {


    Column(

    ) {
        Box (Modifier.padding(5.dp)){
            Column(horizontalAlignment =Alignment.Start){
                Button(onClick = {
                    GameResult.SURRENDER
                    navController.popBackStack()
                }) {
                    Text(text = "Back")
                }
            }

            Row (Modifier.fillMaxWidth().padding(10.dp),horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top){
                if (!gameViewModel.winner.isNullOrEmpty()) {
                    Text(
                        text = "🎉 ${gameViewModel.findPlayerNameByColor(gameViewModel.winner)} winns! 🎉",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Serif,
                        color = when(gameViewModel.winner){
                            "blue" -> Color.Blue
                            "red" -> Color.Red
                            else -> return
                        },
                    )
                }
//                Text(
//                    text = "🎉 Sam winns! 🎉",
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 20.sp,
//                    fontFamily = FontFamily.Serif,
//                    color = Color.Red
//                    )
            }
        }



        Spacer(modifier = Modifier.fillMaxHeight(0.05f))

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.End,
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle, // Predefined user icon
                contentDescription = "User Icon",
                tint = Color.Black, // Icon color
                modifier = Modifier.size(30.dp) // Icon size
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = lobbyViewModel.player2,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.size(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.End,
        ) {
            var winningTokens = gameViewModel.winningTokens("red")
            //Token(color = Color.Red, onClick = {})
            //Token(color = Color.Red, onClick = {})
            for (tokens in winningTokens) {
                Token(color = getTokenColor(tokens), onClick = {})
                if (winningTokens.size in 2..3)
                    Spacer(modifier = Modifier.fillMaxWidth(0.05f))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxHeight(0.5f)

        ) {
            // PlayerColumn
            val boardLength = 11
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(15.dp)
                    .aspectRatio(0.9f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(boardLength),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    var rightOffBoardIndex = 0
                    var leftOffBoardIndex = 7

                    items(boardLength * boardLength) { index ->
                        // Check if it's the end of the 4th row  (update off-board indices)
                        if (index == (boardLength * 4) - 1) {
                            rightOffBoardIndex = boardLength * 7
                            leftOffBoardIndex = rightOffBoardIndex + 7
                        }

                        // Handle the right off-board indices (skip rendering)
                        else if (index >= rightOffBoardIndex && index <= (rightOffBoardIndex + 3)) {
                            if (index == rightOffBoardIndex + 3)
                                rightOffBoardIndex += boardLength
                        }

                        // Handle the left off-board indices (skip rendering)
                        else if (index >= leftOffBoardIndex && index <= (leftOffBoardIndex + 3)) {
                            if (index == leftOffBoardIndex + 3)
                                leftOffBoardIndex += boardLength // Skip rendering for left off-board indices
                        }

                        // The center column
                        else if (index == (boardLength * 5) + 5) {
                            center()
                        } else {
                            standardColumn(gameViewModel, index)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(15.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {


                    CornerRectangle(
                        color = getTokenColor(gameViewModel.YellowTeamTokens[0]),
                        tokens = gameViewModel.YellowTeamTokens,
                        onTokenClick = gameViewModel::handleTokenClick,
                        onRectClick = gameViewModel::handleRectClick
                    )
                    CornerRectangle(
                        color = getTokenColor(gameViewModel.RedTeamTokens[0]),
                        tokens = gameViewModel.RedTeamTokens,
                        onTokenClick = gameViewModel::handleTokenClick,
                        onRectClick = gameViewModel::handleRectClick
                    )

                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom row for bottom-left and bottom-right corners
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CornerRectangle(
                        color = getTokenColor(gameViewModel.BlueTeamTokens[0]),
                        tokens = gameViewModel.BlueTeamTokens,
                        onTokenClick = gameViewModel::handleTokenClick,
                        onRectClick = gameViewModel::handleRectClick
                    )
                    CornerRectangle(
                        color = getTokenColor(gameViewModel.GreenTeamTokens[0]),
                        tokens = gameViewModel.GreenTeamTokens,
                        onTokenClick = gameViewModel::handleTokenClick,
                        onRectClick = gameViewModel::handleRectClick
                    )
                }
            }


//            Text(text = "Column " + clickedColumn.toString() + " is clicked", fontSize = 15.sp)

        }

        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.Start
        ) {
            var winningTokens = gameViewModel.winningTokens("blue")
            for (tokens in winningTokens) {
                Token(color = getTokenColor(tokens), onClick = {})
                if (winningTokens.size in 2..3)
                    Spacer(modifier = Modifier.fillMaxWidth(0.05f))
            }
        }
        Spacer(modifier = Modifier.size(5.dp))
        Row(
            modifier = Modifier.fillMaxWidth(0.9f),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle, // Predefined user icon
                contentDescription = "User Icon",
                tint = Color.Black, // Icon color
                modifier = Modifier.size(30.dp) // Icon size
            )
            Spacer(modifier = Modifier.size(5.dp))
            Text(
                text = lobbyViewModel.player1,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.fillMaxHeight(0.05f))

        Dice(
            result = gameViewModel.diceResult.value,
            onClick = {
                if (gameViewModel.isActiveDice)
                    gameViewModel.rollDice()
            },
            gameViewModel
        )

    }
}

@Composable
fun standardColumn(gameViewModel: GameViewModel, index: Int) {
    val tokenAtThisPosition = gameViewModel.tokenAtThisPosition(index)
    val color = Color(rgb(174, 174, 174))
    val gradientColors = listOf(color, color.copy(alpha = 0.8f))
    gameViewModel.board.add(index)
    Column(
        modifier = Modifier
            .aspectRatio(1f)
            //.clip(RoundedCornerShape(5.dp))
            .clip(shape = CircleShape)
            .background(
                brush = Brush.radialGradient(gradientColors), // Linear gradient for 3D effect
                shape = CircleShape
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (tokenAtThisPosition != null) {
            val tokenColor = getTokenColor(tokenAtThisPosition)
            val onClickHandler = { gameViewModel.handleTokenClick(tokenAtThisPosition) }
            Token(color = tokenColor, onClick = onClickHandler)
        }
    }
}

fun getTokenColor(token: Token): Color {
    return when (token.teamColor()) {
        "yellow" -> Color(rgb(215, 215, 0))
        "red" -> Color(rgb(204, 0, 0))
        "blue" -> Color(rgb(0, 0, 204))
        "green" -> Color(rgb(0, 204, 0))
        else -> Color.Gray
    }
}

@Composable
fun CornerRectangle(
    color: Color,
    tokens: List<Token>,
    onTokenClick: (Token) -> Unit,
    onRectClick: (tokens: List<Token>) -> Unit
) {
    val unusedTokens = tokens.filter { it.place.value == -1 }
    Column(
        modifier = Modifier
            .width(100.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(15.dp, shape = RoundedCornerShape(10.dp), color = color)
            .clickable(onClick = { onRectClick(tokens) }),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var tokenCount = 0
        for (i in 0 until 2) { // Two rows
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (j in 0 until 2) { // Two tokens per row
                    if (tokenCount < unusedTokens.size) {
                        val token = unusedTokens[tokenCount]
                        Token(color, onClick = { onTokenClick(token) })
                        tokenCount++
                    }
                    if (j == 0 && tokenCount < unusedTokens.size) { // Add spacer after the first token in a row
                        Spacer(modifier = Modifier.fillMaxWidth(0.05f))
                    }
                }
            }
            if (i == 0 && unusedTokens.size > 2) { // Add spacer after the first row if more than two tokens
                Spacer(modifier = Modifier.fillMaxHeight(0.05f))
            }
        }
    }
}

@Composable
fun Dice(result: Int, onClick: () -> Unit, gameViewModel: GameViewModel) {
    var rotation by remember { mutableStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing)
    )
    var color = Color.Black
    val currentPlayer = (gameViewModel.currentTurn.value)
    val player1Id = gameViewModel.currentGame?.player1?.id
    if (currentPlayer == player1Id){
        color = Color.Red
    }
    else{
        color = Color.Blue
    }

    fun shouldShowPoint(row: Int, col: Int): Boolean {
        return when (result) {
            1 -> row == 1 && col == 1
            2 -> (row == 0 && col == 0) || (row == 2 && col == 2)
            3 -> (row == 0 && col == 0) || (row == 1 && col == 1) || (row == 2 && col == 2)
            4 -> ((row == 0 || row == 2) && (col == 0 || col == 2))
            5 -> ((row == 0 || row == 2) && (col == 0 || col == 2)) || (row == 1 && col == 1)
            6 -> (col == 0 || col == 2)
            else -> false
        }
    }

    Column(
        modifier = Modifier
            .size(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .graphicsLayer {
                rotationZ = animatedRotation
            }
            .clickable {
                    rotation += 360 * (4..6).random() // Rotate the dice 4-6 times
                onClick()
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Text(text = result.toString(), fontSize = 16.sp)
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 0 until 3) { // Three rows
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (j in 0 until 3) { // Three columns
                        if (shouldShowPoint(i, j)) {
                            dicePoint()
                        } else {
                            Spacer(modifier = Modifier.size(8.dp))
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun dicePoint() {
    val color = Color.White
    val gradientColors = listOf(color, color.copy(alpha = 0.8f))
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(shape = CircleShape)
            .background(
                brush = Brush.radialGradient(gradientColors), // Linear gradient for 3D effect
                shape = CircleShape
            ),
    )
}

@Composable
fun center() {
    val gradientColors = listOf(Color.White, Color.Green,Color.White, Color.Blue,Color.White, Color.Yellow, Color.White, Color.Red, Color.White)
    Row(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(shape = CircleShape)
            .background(
                brush = Brush.sweepGradient(gradientColors),
                shape = CircleShape
            )

    ) {

    }
}

@Composable

fun Token(color: Color, onClick: () -> Unit) {
    val gradientColors = listOf(color, color.copy(alpha = 0.7f))
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(shape = CircleShape)
            .shadow(4.dp, CircleShape)
            .background(
                brush = Brush.radialGradient(gradientColors),
                shape = CircleShape
            )
            .padding(4.dp)

            .clickable(onClick = onClick)
    )
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    val navController = rememberNavController()
    val gameViewModel = GameViewModel() // Assuming GameViewModel can be instantiated like this
    val player1 = "Sam"
    val player2 = "Ahmed"

    GameScreen(gameViewModel, navController, player1, player2, lobbyViewModel = LobbyViewModel())
}






