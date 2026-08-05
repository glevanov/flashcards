package io.levanov.flashcards.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.levanov.flashcards.ui.home.HomeScreen
import io.levanov.flashcards.ui.settings.SettingsScreen
import io.levanov.flashcards.ui.stats.StatsScreen
import io.levanov.flashcards.ui.study.StudyScreen
import kotlinx.serialization.Serializable

/** Session direction; fixed for the duration of a study session. */
@Serializable
enum class StudyDirection { SV_EN, EN_SV }

@Serializable
object Home

@Serializable
data class Study(
    val deck: String? = null,
    val direction: StudyDirection = StudyDirection.SV_EN,
) {
    companion object {
        fun forDeck(deck: String?, reversed: Boolean) = Study(
            deck = deck,
            direction = if (reversed) StudyDirection.EN_SV else StudyDirection.SV_EN,
        )
    }
}

@Serializable
object Stats

@Serializable
object Settings

@Composable
fun FlashcardsNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onStudy = { deck, reversed ->
                    navController.navigate(Study.forDeck(deck, reversed))
                },
                onOpenStats = { navController.navigate(Stats) },
                onOpenSettings = { navController.navigate(Settings) },
            )
        }
        composable<Study> { entry ->
            val study = entry.toRoute<Study>()
            StudyScreen(
                deckName = study.deck,
                reversed = study.direction == StudyDirection.EN_SV,
                onExit = { navController.popBackStack() },
            )
        }
        composable<Stats> {
            StatsScreen(onExit = { navController.popBackStack() })
        }
        composable<Settings> {
            SettingsScreen(onExit = { navController.popBackStack() })
        }
    }
}
