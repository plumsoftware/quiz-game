package ru.plumsoftware.game.data

import java.time.LocalDate

data class Question(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String,
    val difficulty: Int = 1
)

data class DailyTask(
    val id: Int,
    val title: String,
    val description: String,
    val reward: Int,
    val isCompleted: Boolean = false,
    val date: LocalDate = LocalDate.now()
)

data class GameState(
    val coins: Int = 0,
    val level: Int = 1,
    val experience: Int = 0,
    val lastPlayDate: LocalDate? = null,
    val dailyTasksCompleted: Int = 0,
    val streakDays: Int = 0
)

object GameData {
    val questions = listOf(
        Question(
            id = 1,
            question = "What color is the sky on a sunny day?",
            options = listOf("Blue", "Green", "Red", "Yellow"),
            correctAnswer = 0,
            category = "Colors"
        ),
        Question(
            id = 2,
            question = "How many fingers do you have on one hand?",
            options = listOf("3", "4", "5", "6"),
            correctAnswer = 2,
            category = "Numbers"
        ),
        Question(
            id = 3,
            question = "Which animal says 'meow'?",
            options = listOf("Dog", "Cat", "Bird", "Fish"),
            correctAnswer = 1,
            category = "Animals"
        ),
        Question(
            id = 4,
            question = "What do plants need to grow?",
            options = listOf("Water", "Candy", "Toys", "Books"),
            correctAnswer = 0,
            category = "Nature"
        ),
        Question(
            id = 5,
            question = "What shape is a circle?",
            options = listOf("Square", "Triangle", "Circle", "Rectangle"),
            correctAnswer = 2,
            category = "Shapes"
        ),
        Question(
            id = 6,
            question = "How many days are in a week?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "Time"
        ),
        Question(
            id = 7,
            question = "Which fruit is yellow and grows on trees?",
            options = listOf("Apple", "Banana", "Orange", "Grape"),
            correctAnswer = 1,
            category = "Food"
        ),
        Question(
            id = 8,
            question = "What do you use to write on paper?",
            options = listOf("Fork", "Pencil", "Shoe", "Hat"),
            correctAnswer = 1,
            category = "School"
        ),
        Question(
            id = 9,
            question = "What sound does a dog make?",
            options = listOf("Meow", "Woof", "Moo", "Oink"),
            correctAnswer = 1,
            category = "Animals"
        ),
        Question(
            id = 10,
            question = "What color are most leaves?",
            options = listOf("Blue", "Green", "Red", "Purple"),
            correctAnswer = 1,
            category = "Colors"
        )
    )

    fun getDailyTasks(): List<DailyTask> {
        return listOf(
            DailyTask(
                id = 1,
                title = "Complete 3 Quizzes",
                description = "Answer questions to earn coins!",
                reward = 50
            ),
            DailyTask(
                id = 2,
                title = "Get 5 Correct Answers",
                description = "Show off your knowledge!",
                reward = 30
            ),
            DailyTask(
                id = 3,
                title = "Play for 10 Minutes",
                description = "Have fun learning!",
                reward = 25
            ),
            DailyTask(
                id = 4,
                title = "Answer All Categories",
                description = "Try questions from different topics!",
                reward = 40
            ),
            DailyTask(
                id = 5,
                title = "Perfect Score",
                description = "Get all answers correct in one quiz!",
                reward = 100
            )
        )
    }

    fun getCategories(): List<String> {
        return questions.map { it.category }.distinct()
    }
} 