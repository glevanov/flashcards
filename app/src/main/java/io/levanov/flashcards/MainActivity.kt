package io.levanov.flashcards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.levanov.flashcards.ui.Routes
import io.levanov.flashcards.ui.home.HomeScreen
import io.levanov.flashcards.ui.settings.SettingsScreen
import io.levanov.flashcards.ui.stats.StatsScreen
import io.levanov.flashcards.ui.study.StudyScreen
import io.levanov.flashcards.ui.theme.FlashcardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashcardsTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = Routes.HOME) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            onStudy = { deck, reversed ->
                                navController.navigate(Routes.study(deck, reversed))
                            },
                            onOpenStats = {
                                navController.navigate(Routes.STATS)
                            },
                            onOpenSettings = {
                                navController.navigate(Routes.SETTINGS)
                            },
                        )
                    }
                    composable(
                        route = "study?deck={deck}&direction={direction}",
                        arguments = listOf(
                            navArgument("deck") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument("direction") {
                                type = NavType.StringType
                                defaultValue = Routes.DIRECTION_SV_EN
                            },
                        ),
                    ) { entry ->
                        StudyScreen(
                            deckName = entry.arguments?.getString("deck"),
                            reversed = entry.arguments?.getString("direction") == Routes.DIRECTION_EN_SV,
                            onExit = { navController.popBackStack() },
                        )
                    }
                    composable(Routes.STATS) {
                        StatsScreen(onExit = { navController.popBackStack() })
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(onExit = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}