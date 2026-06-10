package ru.plumsoftware.game.data

import androidx.compose.ui.graphics.Color

enum class Difficulty(val label: String, val color: Long) {
    EASY("Лёгкий", 0xFF27AE60),
    MEDIUM("Средний", 0xFFFFD700),
    HARD("Сложный", 0xFFE53935),
    EXPERT("Эксперт", 0xFF9C27B0)
}

data class QuizCategory(
    val id: String,
    val name: String,
    val emoji: String,
    val difficulty: Difficulty,
    val questionsCount: Int,
    val bgColor: Color,
    val description: String
)

val ALL_CATEGORIES = listOf(
    QuizCategory("animals", "Животные", "🦁", Difficulty.EASY, 30, Color(0xFF5D4037), "Животный мир планеты"),
    QuizCategory("dinosaurs", "Динозавры", "🦕", Difficulty.EASY, 30, Color(0xFF558B2F), "Юрский период и виды"),
    QuizCategory("colors", "Цвета и формы", "🎨", Difficulty.EASY, 25, Color(0xFFAD1457), "Цвета, фигуры, узоры"),
    QuizCategory("plants", "Растения", "🌿", Difficulty.EASY, 25, Color(0xFF2E7D32), "Деревья, цветы, грибы"),
    QuizCategory("general", "Общие знания", "🧠", Difficulty.MEDIUM, 50, Color(0xFF5C6BC0), "Широкий кругозор"),
    QuizCategory("geography", "География", "🌍", Difficulty.MEDIUM, 40, Color(0xFF0097A7), "Страны, столицы, реки"),
    QuizCategory("history_ru", "История России", "🏰", Difficulty.MEDIUM, 40, Color(0xFF8D1A1A), "От Рюрика до наших дней"),
    QuizCategory("space", "Космос", "🚀", Difficulty.MEDIUM, 35, Color(0xFF1A237E), "Планеты, звёзды, космонавтика"),
    QuizCategory("music", "Музыка", "🎵", Difficulty.MEDIUM, 30, Color(0xFF6A1B9A), "Жанры, инструменты, композиторы"),
    QuizCategory("science", "Наука", "🔬", Difficulty.HARD, 40, Color(0xFF00695C), "Физика, химия, биология"),
    QuizCategory("history_world", "История мира", "🏛", Difficulty.HARD, 45, Color(0xFF4E342E), "Древние цивилизации и события"),
    QuizCategory("math", "Математика", "🔢", Difficulty.HARD, 30, Color(0xFF1565C0), "Логика, числа, геометрия"),
    QuizCategory("paleontology", "Палеонтология", "🦖", Difficulty.EXPERT, 25, Color(0xFF37474F), "Эпохи, виды, раскопки"),
    QuizCategory("chemistry", "Химия", "⚗️", Difficulty.EXPERT, 30, Color(0xFF004D40), "Таблица Менделеева, реакции")
)

fun findQuizIdForCategory(categoryId: String): Int? {
    val category = ALL_CATEGORIES.find { it.id == categoryId } ?: return null
    return GameData.getAllQuizzes().find { quiz ->
        quiz.category.contains(category.name, ignoreCase = true) ||
            quiz.title.contains(category.emoji) ||
            category.name.split(" ").any { word -> quiz.category.contains(word, ignoreCase = true) }
    }?.id
}
