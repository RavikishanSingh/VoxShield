package com.example.sih_2026.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sih_2026.ui.home.HomeScreen
import com.example.sih_2026.ui.home.HomeViewModel
import com.example.sih_2026.ui.calls.IncomingCallScreen
import com.example.sih_2026.ui.calls.IncomingCallViewModel
import com.example.sih_2026.ui.live_call.LiveCallScreen
import com.example.sih_2026.ui.live_call.LiveCallViewModel
import com.example.sih_2026.ui.attack_lab.AttackLabScreen
import com.example.sih_2026.ui.attack_lab.AttackLabViewModel
import com.example.sih_2026.ui.incidents.IncidentScreen
import com.example.sih_2026.ui.incidents.IncidentViewModel
import com.example.sih_2026.ui.analytics.AnalyticsScreen
import com.example.sih_2026.ui.analytics.AnalyticsViewModel
import com.example.sih_2026.ui.settings.SettingsScreen
import com.example.sih_2026.ui.settings.SettingsViewModel
import com.example.sih_2026.ui.profiles.VoiceProfileScreen
import com.example.sih_2026.ui.profiles.VoiceProfileViewModel

object VoxShieldDestinations {
    const val HOME = "home"
    const val INCOMING_CALL = "incoming_call"
    const val LIVE_CALL = "live_call"
    const val ATTACK_LAB = "attack_lab"
    const val INCIDENTS = "incidents"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val PROFILES = "profiles"
}

@Composable
fun VoxShieldNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = VoxShieldDestinations.HOME
    ) {
        composable(VoxShieldDestinations.HOME) {
            val viewModel: HomeViewModel = viewModel()
            HomeScreen(viewModel, onNavigate = { destination -> 
                navController.navigate(destination)
            })
        }
        composable(VoxShieldDestinations.INCOMING_CALL) {
            val viewModel: IncomingCallViewModel = viewModel()
            IncomingCallScreen(viewModel, onAnswer = {
                navController.navigate(VoxShieldDestinations.LIVE_CALL)
            })
        }
        composable(VoxShieldDestinations.LIVE_CALL) {
            val viewModel: LiveCallViewModel = viewModel()
            LiveCallScreen(viewModel)
        }
        composable(VoxShieldDestinations.ATTACK_LAB) {
            val viewModel: AttackLabViewModel = viewModel()
            AttackLabScreen(viewModel)
        }
        composable(VoxShieldDestinations.INCIDENTS) {
            val viewModel: IncidentViewModel = viewModel()
            IncidentScreen(viewModel)
        }
        composable(VoxShieldDestinations.ANALYTICS) {
            val viewModel: AnalyticsViewModel = viewModel()
            AnalyticsScreen(viewModel)
        }
        composable(VoxShieldDestinations.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(viewModel, onNavigateToProfiles = {
                navController.navigate(VoxShieldDestinations.PROFILES)
            })
        }
        composable(VoxShieldDestinations.PROFILES) {
            val viewModel: VoiceProfileViewModel = viewModel()
            VoiceProfileScreen(viewModel)
        }
    }
}
