package com.example.sih_2026

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sih_2026.ui.navigation.VoxShieldDestinations
import com.example.sih_2026.ui.navigation.VoxShieldNavGraph

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        // Permissions handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        setContent {
            val navController = rememberNavController()
            
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF818CF8),
                    background = Color(0xFF1E293B),
                    surface = Color(0xFF334155),
                ),
            ) {
                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        val showBottomBar = currentDestination?.route !in listOf(
                            VoxShieldDestinations.INCOMING_CALL,
                            VoxShieldDestinations.LIVE_CALL
                        )

                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Color(0xFF1E293B),
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Home") },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.HOME } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.HOME) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Science, contentDescription = null) },
                                    label = { Text("Labs") },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.ATTACK_LAB } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.ATTACK_LAB) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                                    label = { Text("Profiles") },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.PROFILES } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.PROFILES) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                                    label = { Text("Incidents") },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.INCIDENTS } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.INCIDENTS) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    label = { Text("Settings") },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.SETTINGS } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.SETTINGS) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        VoxShieldNavGraph(navController = navController)
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
        )
        val missing = permissions.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing) {
            permissionLauncher.launch(permissions)
        }
    }
}
