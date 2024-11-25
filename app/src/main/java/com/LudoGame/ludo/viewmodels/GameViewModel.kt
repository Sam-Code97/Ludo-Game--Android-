package com.example.ludo.viewmodels

import android.annotation.SuppressLint
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.garrit.android.multiplayer.ActionResult
import io.garrit.android.multiplayer.Game
import io.garrit.android.multiplayer.GameResult
import io.garrit.android.multiplayer.Player
import io.garrit.android.multiplayer.SupabaseCallback
import io.garrit.android.multiplayer.SupabaseService
import kotlinx.coroutines.launch


data class Token(
    val id: Int,
    val team: Int,
    //var place: MutableState<Int> = mutableStateOf(-1),
    var place: MutableState<Int> = mutableStateOf(-1),
    val isActive: MutableState<Boolean> = mutableStateOf(true),
    val isClicked: MutableState<Boolean> = mutableStateOf(false),
) {
    fun deactivate() {
        isActive.value = false
    }

    fun cliked() {
        isClicked.value = true
    }

    fun unCliked() {
        isClicked.value = false
    }

    fun teamColor(): String {
        return when (team) {
            1 -> "yellow"
            2 -> "red"
            3 -> "blue"
            4 -> "green"
            else -> "gray"
        }
    }

}

enum class ActionType {
    MoveToken,
    DiceRoll
}

class GameViewModel : ViewModel(), SupabaseCallback {


    var diceResult = mutableStateOf(0)
    val receivedDiceResult = mutableStateOf<Int?>(null)


    val BlueTeamTokens = mutableStateListOf(
        Token(1, 3),
        Token(2, 3),
        Token(3, 3),
        Token(4, 3)
    )
    val RedTeamTokens = mutableStateListOf(
        Token(5, 2),
        Token(6, 2),
        Token(7, 2),
        Token(8, 2)
    )
    val YellowTeamTokens = mutableStateListOf(
        Token(9, 1),
        Token(10, 1),
        Token(11, 1),
        Token(12, 1)
    )
    val GreenTeamTokens = mutableStateListOf(
        Token(13, 4),
        Token(14, 4),
        Token(15, 4),
        Token(16, 4)
    )

    var selectedToken = mutableStateOf<Token?>(null)

    val paths = mapOf(
        "yellow" to listOf(
            44,
            45,
            46,
            47,
            48,
            37,
            26,
            15,
            4,
            5,
            6,
            17,
            28,
            39,
            50,
            51,
            52,
            53,
            54,
            65,
            76,
            75,
            74,
            73,
            72,
            83,
            94,
            105,
            116,
            115,
            114,
            103,
            92,
            81,
            70,
            69,
            68,
            67,
            66,
            55,
            56,
            57,
            58,
            59,
            60
        ),
        "red" to listOf(
            6,
            17,
            28,
            39,
            50,
            51,
            52,
            53,
            54,
            65,
            76,
            75,
            74,
            73,
            72,
            83,
            94,
            105,
            116,
            115,
            114,
            103,
            92,
            81,
            70,
            69,
            68,
            67,
            66,
            55,
            44,
            45,
            46,
            47,
            48,
            37,
            26,
            15,
            4,
            5,
            16,
            27,
            38,
            49,
            60
        ),
        "blue" to listOf(
            114,
            103,
            92,
            81,
            70,
            69,
            68,
            67,
            66,
            55,
            44,
            45,
            46,
            47,
            48,
            37,
            26,
            15,
            4,
            5,
            6,
            17,
            28,
            39,
            50,
            51,
            52,
            53,
            54,
            65,
            76,
            75,
            74,
            73,
            72,
            83,
            94,
            105,
            116,
            115,
            104,
            93,
            82,
            71,
            60
        ),
        "green" to listOf(
            76,
            75,
            74,
            73,
            72,
            83,
            94,
            105,
            116,
            115,
            114,
            103,
            92,
            81,
            70,
            69,
            68,
            67,
            66,
            55,
            44,
            45,
            46,
            47,
            48,
            37,
            26,
            15,
            4,
            5,
            6,
            17,
            28,
            39,
            50,
            51,
            52,
            53,
            54,
            65,
            64,
            63,
            62,
            61,
            60
        )
    )

    var board = mutableListOf<Int>()

    // Function to map grid index to path index
    fun getPathIndex(team: String, gridIndex: Int): Int {
        val path = paths[team] ?: return -1
        return path.indexOf(gridIndex)
    }

    fun getBoardIndexFromPathIndex(team: String, gridIndex: Int): Int {
        val path = paths[team] ?: return -1
        return path[gridIndex]
    }

    fun handleTokenClick(token: Token) {
        println("handleTokenClick")
        val currentPlayerTokens = getCurrentPlayerTokens()
        if (currentPlayerTokens != null && currentPlayerTokens.contains(token)) {
            if (token.place.value == -1) {
                if (diceResult.value == 6) {
                    moveToken(token, mutableIntStateOf(1))
                }
            } else {
                selectedToken.value = token
                moveToken(token, diceResult)
            }
        } else {
            println("Token does not belong to the current player")
        }
        // Additional logic for moving the token
    }

    fun handleRectClick(tokens: List<Token>) {
        println("handleRectClick 1")
        val unusedTokens = tokens.filter { it.place.value == -1 }
        val currentPlayerTokens = getCurrentPlayerTokens()
        if (unusedTokens.isNotEmpty() && currentPlayerTokens != null && currentPlayerTokens[0].team == unusedTokens[0].team) {
            println("handleRectClick 2")
            if (diceResult.value == 6) {
                moveToken(unusedTokens[0], mutableIntStateOf(1))
            }
        }
    }

    fun tokenAtThisPosition(position: Int): Token?
        = BlueTeamTokens.find { it.place.value == position }
            ?: RedTeamTokens.find { it.place.value == position }
            ?: YellowTeamTokens.find { it.place.value == position }
            ?: GreenTeamTokens.find { it.place.value == position }

    private fun findTokenById(tokenId: Int): Token?
        = BlueTeamTokens.find { it.id == tokenId }
            ?: RedTeamTokens.find { it.id == tokenId }
            ?: YellowTeamTokens.find { it.id == tokenId }
            ?: GreenTeamTokens.find { it.id == tokenId }

    fun findTeamById(tokenId: Int): SnapshotStateList<Token>? {
        return when {
            BlueTeamTokens.any { it.id == tokenId } -> BlueTeamTokens
            RedTeamTokens.any { it.id == tokenId } -> RedTeamTokens
            YellowTeamTokens.any { it.id == tokenId } -> YellowTeamTokens
            GreenTeamTokens.any { it.id == tokenId } -> GreenTeamTokens
            else -> null
        }
    }

    val serverState get() = SupabaseService.serverState
    var playerReady by mutableStateOf(false)
    var isActiveDice by mutableStateOf(true)
    var currentTurn = mutableStateOf("")


    val player get() = SupabaseService.player
    val currentGame get() = SupabaseService.currentGame
    fun invite(opponent: Player) {
        viewModelScope.launch {
            SupabaseService.invite(opponent)
        }
    }

    fun currentGame(): Game? {
        return SupabaseService.currentGame
    }

    fun sendTurn(steps: Int, tokeId: Int) {
        viewModelScope.launch {
            SupabaseService.sendTurn(steps, tokeId)
        }
    }

    fun sendTurn(dice: Int) {
        viewModelScope.launch {
            SupabaseService.sendTurn(dice)
        }
    }

    private var currentPlayerIndex by mutableIntStateOf(0)
    private val playerPositions = mutableMapOf<Player, List<Int>>()

    init {
        SupabaseService.callbackHandler = this

        currentPlayerIndex = 0

        initializeTurnState()
        val currentPlayer: MutableLiveData<Player> = MutableLiveData()
    }

    private fun initializeTurnState() {
        val game = SupabaseService.currentGame
        currentTurn.value = game?.player1?.id ?: ""
    }

    var hasMoved = mutableStateOf(false)

    @SuppressLint("SuspiciousIndentation")
    fun rollDice() {
        println("Attempting to roll dice... ${isMyTurn()} ${winner.isEmpty()}")
        if (isMyTurn() && winner.isEmpty()) {//&& !hasMoved.value) {
            println("If-statement successful!")
            diceResult.value = (1..6).random()

            if (diceResult.value != 6)
                diceResult.value = (1..6).random()

            println("Rolled dice: ${diceResult.value}")
            sendTurn(diceResult.value)

            if (!isPossibleMovesForCurrentPlayer(diceResult.value)) {
                println("No possible moves for current player.")
                //println("!!!!!release1!!!!!!")
                releaseTurn()
            } else {
                println("Possible moves exist for the dice roll: ${diceResult.value}")
            }
            hasMoved.value = false
        } else {
            println("Not my turn.")
        }
    }

    private fun isPossibleMovesForCurrentPlayer(diceRoll: Int): Boolean {
        println("Checking for possible moves with dice roll: $diceRoll")
        val currentPlayerTokens = getCurrentPlayerTokens() ?: run {
            println("No current player tokens found.")
            return true
        }
        println("Current team found: ${currentPlayerTokens[0].teamColor()}")
        return isNoPossibleToMove(currentPlayerTokens, diceRoll)
    }

    private fun getCurrentPlayerTokens(): List<Token>? {
        val currentPlayer = SupabaseService.player
        // Match the current player with their tokens
        println("Getting tokens for current player: ${currentPlayer?.name}")
        return when (currentPlayer?.id) {
            SupabaseService.currentGame?.player1?.id -> BlueTeamTokens
            SupabaseService.currentGame?.player2?.id -> RedTeamTokens
            else -> null.also { println("No matching team found for current player") }
        }
    }

    private fun isNoPossibleToMove(tokens: List<Token>, steps: Int): Boolean {
        println("Checking if no possible move with steps: $steps")
        for (token in tokens) {
            if (token.place.value == -1 && steps == 6) {
                println("Token ${token.id} can enter the board with a roll of 6.")
                return false
            }
            if (token.place.value > -1) {
                println("Token ${token.id} can move forward with steps: $steps")
                return false
            }
        }
        println("No possible moves for any token.")
        return false
    }

    private fun isMyTurn(): Boolean {
        println("isMyTurn: ${SupabaseService.player?.id} | ${currentTurn.value}")
        return SupabaseService.player?.id == currentTurn.value
    }
    fun moveToken(token: Token, steps: MutableState<Int>) {
        val currentPlayerTokens = getCurrentPlayerTokens()
        val originalPosition = token.place
        val teamColor = token.teamColor()
        if (currentPlayerTokens.isNullOrEmpty()) {
            println("Returning: currentPlayerTokens.isNullOrEmpty()")
            return
        }
        if (currentPlayerTokens[0].team == token.team) {
            println("if (currentPlayerTokens[0].team == token.team)")
            val currentPathIndex = getPathIndex(teamColor, token.place.value)
            val newPathIndex = currentPathIndex + steps.value
            if (newPathIndex > 44) // last available column
                return
            val newPosition = getBoardIndexFromPathIndex(teamColor, newPathIndex)

            val tokenAtThisPosition = tokenAtThisPosition(newPosition)
//        val isPossibleToMove = isPossibleToMove(token, steps)
            if (newPathIndex == 44) { // winning position
                token.place.value = 100
                val stepsToHundred = newPathIndex - currentPathIndex
                sendTurn(stepsToHundred, token.id) // Send update to the server
                println("Winning move sent: Token ID: ${token.id}, Steps: $stepsToHundred")
                checkWinCondition()
                isActiveDice = false
                releaseTurn() // Release turn if applicable
            } else {
                if (!isMyTurn()) {
                    if (tokenAtThisPosition != null) {
                        if (tokenAtThisPosition.teamColor() != token.teamColor()) {
                            tokenAtThisPosition.place.value = -1
                            token.place.value = getBoardIndexFromPathIndex(teamColor, newPathIndex)
                            hasMoved.value = true
                            val diceResultTemp = diceResult.value
                            diceResult.value = 0
                            sendTurn(diceResultTemp, token.id)
                            println("Sending actions: x=$steps, y=$token")
                            isActiveDice = false
                            println("!!!!!release2!!!!!!")
                            releaseTurn()
                        }
                    }

                    if (tokenAtThisPosition == null) {
                        token.place.value = getBoardIndexFromPathIndex(teamColor, newPathIndex)
                        val diceResultTemp = diceResult.value
                        diceResult.value = 0
                        sendTurn(diceResultTemp, token.id)
                        println("Sending actions: x=$steps, y=$token")
                        isActiveDice = false
                        println("!!!!!release3!!!!!!")
                        releaseTurn()
                    }

                    if (token.place.value != originalPosition.value) {
                        hasMoved.value = true
                        println("Token moved, setting hasMoved to true")
                    }
                }
            }
        } else {
            println("Returning: currentPlayerTokens[0].team == token.team")
            return
        }
    }


    fun winningTokens(teamColor: String): List<Token> {
        var winningTokens = mutableListOf<Token>()
        if (teamColor == "blue") {
            winningTokens += BlueTeamTokens.filter { it.place.value == 100 }
        } else if (teamColor == "red") {
            winningTokens += RedTeamTokens.filter { it.place.value == 100 }
        } else if (teamColor == "yellow") {
            winningTokens += YellowTeamTokens.filter { it.place.value == 100 }
        } else if (teamColor == "green") {
            winningTokens += GreenTeamTokens.filter { it.place.value == 100 }
        }
        return winningTokens
    }

    fun releaseTurn() {
        viewModelScope.launch {
            println("rT Current turn: ${currentTurn.value}, Player1: ${SupabaseService.currentGame?.player1}, Player2: ${SupabaseService.currentGame?.player2}")
            SupabaseService.releaseTurn()
            val playerTurn = if (currentTurn.value == SupabaseService.currentGame?.player1?.id)
                SupabaseService.currentGame?.player2?.name.toString()
            else
                SupabaseService.currentGame?.player1?.name.toString()
            currentTurn.value = if (currentTurn.value == SupabaseService.currentGame?.player1?.id)
                SupabaseService.currentGame?.player2?.id.toString()
            else
                SupabaseService.currentGame?.player1?.id.toString()

            println("Releasing turn to $playerTurn (${currentTurn.value})")
        }
    }


    override suspend fun playerReadyHandler() {
    }

    override suspend fun releaseTurnHandler() {
        isActiveDice = true
        println("rTH Current turn: ${currentTurn.value}, Player1: ${SupabaseService.currentGame?.player1}, Player2: ${SupabaseService.currentGame?.player2}")

        val playerTurn = if (currentTurn.value == SupabaseService.currentGame?.player1?.id)
            SupabaseService.currentGame?.player2?.name.toString()
        else
            SupabaseService.currentGame?.player1?.name.toString()
        currentTurn.value = if (currentTurn.value == SupabaseService.currentGame?.player1?.id)
            SupabaseService.currentGame?.player2?.id.toString()
        else
            SupabaseService.currentGame?.player1?.id.toString()

        hasMoved.value = false
        println("$playerTurn's turn now (${currentTurn.value})")
    }

    override suspend fun actionHandler(x: Int, y: Int) {
        //diceResult.value = x
        /*
                when(ActionType.values()[x]){
                    ActionType.MoveToken -> {
                        diceResult.value = 0
                        // Assuming x is the token ID and y is the number of steps to move
                        val token = findTokenById(y)
                        val steps = z
                        if (token != null) {
                            if(token.place.value == -1){
                                if (steps == 6) {//dice is not being updated
                                    moveToken(token, mutableIntStateOf(1))
                                }
                            }
                            else {
                                moveToken(token, mutableIntStateOf(steps))
                            }
                        }
                    }
                    ActionType.DiceRoll -> {
                        diceResult.value = y
                    }
                }
        */

        /*
                when(z){
                    1 -> {
                        println("Inside the Z == 1")
                        diceResult.value = 0
                        // Assuming x is the token ID and y is the number of steps to move
                        val token = findTokenById(x)
                        val steps = y
                        if (token != null) {
                            if(token.place.value == -1){
                                if (steps == 6) {//dice is not being updated
                                    moveToken(token, mutableIntStateOf(1))
                                }
                            }
                            else {
                                moveToken(token, mutableIntStateOf(steps))
                            }
                        }
                    }
                    2 -> {
                        diceResult.value = x
                    }
                }
        */

        println("Received action: x=$x, y=$y")
        if (y == -1) {
            diceResult.value = x
            println("Dice rolled by other player: $x")
        } else {
            println("move actionHandler called")
            diceResult.value = 0
            val token = findTokenById(y)
            val steps = x
            if (token != null) {
                if (token.place.value == -1) {
                    if (steps == 6) {//dice is not being updated
                        updateMoveToken(token, mutableIntStateOf(1))
                    }
                } else {
                    updateMoveToken(token, mutableIntStateOf(steps))
                }
            }
        }
    }

    fun updateMoveToken(token: Token, steps: MutableState<Int>) {
        val originalPosition = token.place
        val currentPlayerTokens = getCurrentPlayerTokens()
        val teamColor = token.teamColor()
        println("Updating move for token ID: ${token.id}, Team: $teamColor, Steps: ${steps.value}")
        if (currentPlayerTokens.isNullOrEmpty()) {
            println("Exiting: No tokens found for the current player.")
            return
        }

        if (currentPlayerTokens[0].team == token.team) {
            println("Exiting: Token does not belong to the current player.")
            return
        }


        val currentPathIndex = getPathIndex(teamColor, token.place.value)
        val newPathIndex = currentPathIndex + steps.value
        if (newPathIndex > 44) // last available column
            return
        val newPosition = getBoardIndexFromPathIndex(teamColor, newPathIndex)

        val tokenAtThisPosition = tokenAtThisPosition(newPosition)
//        val isPossibleToMove = isPossibleToMove(token, steps)
        if (newPathIndex == 44) { // winning position
            token.place.value = 100
            val stepsToHundred = newPathIndex - currentPathIndex
            sendTurn(stepsToHundred, token.id) // Send update to the server
            println("Winning move sent: Token ID: ${token.id}, Steps: $stepsToHundred")
            checkWinCondition()
            isActiveDice = false
            releaseTurn() // Release turn if applicabl
        } else {
//            if (isMyTurn()) {
            if (tokenAtThisPosition != null) {
                if (tokenAtThisPosition.teamColor() != token.teamColor()) {
                    tokenAtThisPosition.place.value = -1
                    token.place.value = newPosition
                    println("Token ${token.id} moved to new position: $newPosition, Kicked out token: ${tokenAtThisPosition.id}")
                    val diceResultTemp = diceResult.value
                    diceResult.value = 0
                    sendTurn(diceResultTemp, token.id)
                    println("Turn sent: Token ID: ${token.id} for team ( ${token.teamColor()} ), Dice Roll: $diceResultTemp")
                    isActiveDice = false
                    println("!!!!!release4!!!!!!")
                    releaseTurn()
                }
            }

            if (tokenAtThisPosition == null) {
                token.place.value = newPosition
                val diceResultTemp = diceResult.value
                diceResult.value = 0
                sendTurn(diceResultTemp, token.id)
                println("Turn sent: Token ID: ${token.id} for team ( ${token.teamColor()} ), Dice Roll: $diceResultTemp")
                isActiveDice = false
                println("isActiveDice set to false")
                println("!!!!!release5!!!!!!")
                releaseTurn()
            }
            checkWinCondition()

            if (token.place.value != originalPosition.value) {
                hasMoved.value = true
                println("Token moved, setting hasMoved to true")
            }

        }
    }

    override suspend fun answerHandler(status: ActionResult) {
        TODO("Not yet implemented")
    }

    fun findPlayerIdByColor(color: String): String? {
        return when (color) {
            "blue" -> SupabaseService.currentGame?.player1?.id
            "red" -> SupabaseService.currentGame?.player2?.id
            else -> null
        }
    }

    fun findPlayerNameByColor(color: String): String? {
        return when (color) {
            "blue" -> SupabaseService.currentGame?.player1?.name
            "red" -> SupabaseService.currentGame?.player2?.name
            else -> null
        }
    }

    fun cheatWin(teamColor: String) {
        val tokens = when (teamColor) {
            "blue" -> BlueTeamTokens
            "red" -> RedTeamTokens
            // Add other teams if necessary
            else -> return
        }

        tokens.forEach { token ->
            token.place.value = 100 // Set to a winning position
        }
        checkWinCondition() // Ensure this method checks for tokens at position 101
    }

    var winner = ""
    fun checkWinCondition() {
        val playerTeams = listOf(BlueTeamTokens, RedTeamTokens, YellowTeamTokens, GreenTeamTokens)
        playerTeams.forEach { team ->
            if (team.all { it.place.value == 100 }) {
                // This team has won
                val winningColor = team.first().teamColor()
                println("$winningColor team wins!")
                winner = winningColor


                viewModelScope.launch {
                    val playerId = findPlayerIdByColor(winningColor)
                    if (SupabaseService.player?.id == playerId) {
                        SupabaseService.gameFinish(GameResult.WIN)
                    } else {
                        SupabaseService.gameFinish(GameResult.LOSE)
                    }
                }
            }
        }
    }

    override suspend fun finishHandler(status: GameResult) {
        when (status) {
            GameResult.WIN -> {
                // Handle winning logic
                println("You won the game!")
            }

            GameResult.LOSE -> {
                // Handle losing logic
                println("You lost the game.")
            }

            GameResult.DRAW -> {
                // Handle draw logic
                println("The game ended in a draw.")
            }

            GameResult.SURRENDER -> {
                // Handle surrender logic
                println("Opponent surrendered. You won!")
            }
        }
    }
}

