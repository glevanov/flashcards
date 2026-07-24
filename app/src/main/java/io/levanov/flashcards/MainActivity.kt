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
                            onStudyDeck = { deck ->
                                navController.navigate(Routes.study(deck))
                            },
                            onStudyAll = {
                                navController.navigate(Routes.STUDY_ALL)
                            },
                        )
                    }
                    composable(
                        route = "study?deck={deck}",
                        arguments = listOf(
                            navArgument("deck") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                        ),
                    ) { entry ->
                        StudyScreen(
                            deckName = entry.arguments?.getString("deck"),
                            onExit = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}