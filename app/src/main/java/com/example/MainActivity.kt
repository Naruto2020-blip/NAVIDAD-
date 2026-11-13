package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassTop
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.NavidadUiState
import com.example.ui.NavidadViewModel
import com.example.ui.components.MusicMiniPlayer
import com.example.ui.components.SnowfallOverlay
import com.example.ui.screens.CountdownScreen
import com.example.ui.screens.MusicAmbianceScreen
import com.example.ui.theme.NavidadTheme

private object NavidadRoutes {
    const val COUNTDOWN = "countdown"
    const val MUSIC = "music"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: NavidadViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_START -> viewModel.onAppForeground()
                        Lifecycle.Event.ON_STOP -> viewModel.onAppBackground()
                        else -> Unit
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            NavidadTheme(darkTheme = uiState.preferences.darkAmbiance) {
                NavidadAppShell(
                    uiState = uiState,
                    viewModel = viewModel,
                )
            }
        }
    }
}

@Composable
private fun NavidadAppShell(
    uiState: NavidadUiState,
    viewModel: NavidadViewModel,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: NavidadRoutes.COUNTDOWN

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
        ),
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        val isWideScreen = maxWidth >= 600.dp

        SnowfallOverlay(
            enabled = uiState.preferences.snowEnabled,
            darkAmbiance = uiState.preferences.darkAmbiance,
            modifier = Modifier.fillMaxSize(),
        )

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isWideScreen) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (currentRoute == NavidadRoutes.COUNTDOWN) {
                            MusicMiniPlayer(
                                track = uiState.currentTrack,
                                audioState = uiState.audioState,
                                isMusicEnabled = uiState.preferences.musicEnabled,
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onNextTrack = viewModel::nextTrack,
                                onPreviousTrack = viewModel::previousTrack,
                                onRingBells = viewModel::ringBells,
                                onOpenMusicTab = { navigateTo(NavidadRoutes.MUSIC) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        NavidadBottomBar(
                            currentRoute = currentRoute,
                            onNavigate = ::navigateTo,
                        )
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (isWideScreen) {
                    NavidadNavigationRail(
                        currentRoute = currentRoute,
                        onNavigate = ::navigateTo,
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 840.dp),
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = NavidadRoutes.COUNTDOWN,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        ) {
                            composable(NavidadRoutes.COUNTDOWN) {
                                CountdownScreen(
                                    uiState = uiState,
                                    onSelectTarget = viewModel::selectTarget,
                                    onToggleSnow = viewModel::toggleSnow,
                                    onToggleAmbiance = viewModel::toggleAmbiance,
                                    onRingBells = viewModel::ringBells,
                                    onToggleMusic = viewModel::togglePlayPause,
                                )
                            }
                            composable(NavidadRoutes.MUSIC) {
                                MusicAmbianceScreen(
                                    uiState = uiState,
                                    onSelectTrack = viewModel::selectTrack,
                                    onTogglePlayPause = viewModel::togglePlayPause,
                                    onNextTrack = viewModel::nextTrack,
                                    onPreviousTrack = viewModel::previousTrack,
                                    onVolumeChange = viewModel::setVolume,
                                    onToggleSnow = viewModel::toggleSnow,
                                    onToggleAmbiance = viewModel::toggleAmbiance,
                                    onRingBells = viewModel::ringBells,
                                )
                            }
                        }

                        if (isWideScreen && currentRoute == NavidadRoutes.COUNTDOWN) {
                            MusicMiniPlayer(
                                track = uiState.currentTrack,
                                audioState = uiState.audioState,
                                isMusicEnabled = uiState.preferences.musicEnabled,
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onNextTrack = viewModel::nextTrack,
                                onPreviousTrack = viewModel::previousTrack,
                                onRingBells = viewModel::ringBells,
                                onOpenMusicTab = { navigateTo(NavidadRoutes.MUSIC) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavidadBottomBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 6.dp,
    ) {
        NavigationBarItem(
            selected = currentRoute == NavidadRoutes.COUNTDOWN,
            onClick = { onNavigate(NavidadRoutes.COUNTDOWN) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavidadRoutes.COUNTDOWN) {
                        Icons.Rounded.HourglassTop
                    } else {
                        Icons.Outlined.HourglassTop
                    },
                    contentDescription = stringResource(R.string.nav_countdown),
                )
            },
            label = { Text(stringResource(R.string.nav_countdown)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.testTag("nav_countdown_tab"),
        )
        NavigationBarItem(
            selected = currentRoute == NavidadRoutes.MUSIC,
            onClick = { onNavigate(NavidadRoutes.MUSIC) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavidadRoutes.MUSIC) {
                        Icons.Rounded.MusicNote
                    } else {
                        Icons.Outlined.MusicNote
                    },
                    contentDescription = stringResource(R.string.nav_music),
                )
            },
            label = { Text(stringResource(R.string.nav_music)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.testTag("nav_music_tab"),
        )
    }
}

@Composable
private fun NavidadNavigationRail(
    currentRoute: String,
    onNavigate: (String) -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        NavigationRailItem(
            selected = currentRoute == NavidadRoutes.COUNTDOWN,
            onClick = { onNavigate(NavidadRoutes.COUNTDOWN) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavidadRoutes.COUNTDOWN) {
                        Icons.Rounded.HourglassTop
                    } else {
                        Icons.Outlined.HourglassTop
                    },
                    contentDescription = stringResource(R.string.nav_countdown),
                )
            },
            label = { Text(stringResource(R.string.nav_countdown)) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.testTag("rail_countdown_tab"),
        )
        NavigationRailItem(
            selected = currentRoute == NavidadRoutes.MUSIC,
            onClick = { onNavigate(NavidadRoutes.MUSIC) },
            icon = {
                Icon(
                    imageVector = if (currentRoute == NavidadRoutes.MUSIC) {
                        Icons.Rounded.MusicNote
                    } else {
                        Icons.Outlined.MusicNote
                    },
                    contentDescription = stringResource(R.string.nav_music),
                )
            },
            label = { Text(stringResource(R.string.nav_music)) },
            colors = NavigationRailItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.testTag("rail_music_tab"),
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}
