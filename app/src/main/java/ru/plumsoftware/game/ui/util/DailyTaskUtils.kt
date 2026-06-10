package ru.plumsoftware.game.ui.util

import ru.plumsoftware.game.data.GameData

fun isDailyTaskCompleted(taskId: Int, progress: Map<String, Int>): Boolean = when (taskId) {
    1 -> (progress["quizzes_completed"] ?: 0) >= 3
    2 -> (progress["correct_answers"] ?: 0) >= 5
    3 -> (progress["play_time_minutes"] ?: 0) >= 10
    4 -> (progress["categories_played"] ?: 0) >= GameData.getCategories().size
    5 -> false
    6 -> (progress["quiz_levels_completed"] ?: 0) >= 1
    7 -> (progress["coins_earned"] ?: 0) >= 100
    else -> false
}

fun countCompletedDailyTasks(progress: Map<String, Int>): Int {
    return GameData.getDailyTasks().count { isDailyTaskCompleted(it.id, progress) }
}
