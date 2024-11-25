package com.example.ludo.viewmodels

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.garrit.android.multiplayer.Game
import io.garrit.android.multiplayer.Player
import io.garrit.android.multiplayer.SupabaseService
import kotlinx.coroutines.launch


class LobbyViewModel : ViewModel() {
    val player1 = SupabaseService.currentGame?.player1?.name.toString()
    val player2 = SupabaseService.currentGame?.player2?.name.toString()
    val games = SupabaseService.games
    val players = SupabaseService.users.filter { it != SupabaseService.player }
    fun joinLobby(player: Player) {
        println("joining lobby")
        viewModelScope.launch {
            println("Joining lobby")
            SupabaseService.joinLobby(player)
        }
    }
    fun sendInvite(player: Player) {
        viewModelScope.launch {
            SupabaseService.invite(player)
        }
    }

    fun PlayerReady() {
        viewModelScope.launch {
            SupabaseService.playerReady()
        }
    }

    fun AcceptInvite(game: Game) {
        viewModelScope.launch {
            SupabaseService.acceptInvite(game)
        }
    }

    fun DeclineInvite(game: Game) {
        viewModelScope.launch {
            SupabaseService.declineInvite(game)
        }
    }
}
