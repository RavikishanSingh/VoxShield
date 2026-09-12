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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                colorScheme = lightColorScheme(
                    primary = Color(0xFF4F46E5), // Indigo 600
                    onPrimary = Color.White,
                    background = Color(0xFFF1F5F9), // Slate 100
                    onBackground = Color(0xFF0F172A),
                    surface = Color(0xFFFFFFFF), // Crisp White
                    onSurface = Color(0xFF0F172A),
                    surfaceVariant = Color(0xFFE2E8F0), // Slate 200
                    onSurfaceVariant = Color(0xFF64748B),
                    error = Color(0xFFDC2626)
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
                                containerColor = Color(0xFFFFFFFF),
                                contentColor = Color(0xFF0F172A),
                                tonalElevation = 12.dp
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    label = { Text("Home", fontWeight = FontWeight.Medium) },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.HOME } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.HOME) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF818CF8),
                                        selectedTextColor = Color(0xFF818CF8),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Science, contentDescription = null) },
                                    label = { Text("Labs", fontWeight = FontWeight.Medium) },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.ATTACK_LAB } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.ATTACK_LAB) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF818CF8),
                                        selectedTextColor = Color(0xFF818CF8),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                                    label = { Text("Profiles", fontWeight = FontWeight.Medium) },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.PROFILES } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.PROFILES) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF818CF8),
                                        selectedTextColor = Color(0xFF818CF8),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                                    label = { Text("Incidents", fontWeight = FontWeight.Medium) },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.INCIDENTS } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.INCIDENTS) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF818CF8),
                                        selectedTextColor = Color(0xFF818CF8),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    label = { Text("Settings", fontWeight = FontWeight.Medium) },
                                    selected = currentDestination?.hierarchy?.any { it.route == VoxShieldDestinations.SETTINGS } == true,
                                    onClick = {
                                        navController.navigate(VoxShieldDestinations.SETTINGS) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF818CF8),
                                        selectedTextColor = Color(0xFF818CF8),
                                        unselectedIconColor = Color(0xFF94A3B8),
                                        unselectedTextColor = Color(0xFF94A3B8),
                                        indicatorColor = Color(0xFF6366F1).copy(alpha = 0.25f)
                                    )
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
