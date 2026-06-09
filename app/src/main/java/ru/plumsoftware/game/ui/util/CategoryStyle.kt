package ru.plumsoftware.game.ui.util

import androidx.compose.ui.graphics.Color

data class CategoryStyle(
    val color: Color,
    val emoji: String
)

object CategoryStyles {
    private val styles = mapOf(
        "Общие знания" to CategoryStyle(Color(0xFF4CAF50), "🧠"),
        "Цвета и формы" to CategoryStyle(Color(0xFFE91E63), "🎨"),
        "Цвета" to CategoryStyle(Color(0xFFE91E63), "🎨"),
        "Фигуры" to CategoryStyle(Color(0xFFE91E63), "🔷"),
        "Животные" to CategoryStyle(Color(0xFFFF9800), "🐾"),
        "Числа и счёт" to CategoryStyle(Color(0xFF2196F3), "🔢"),
        "Числа" to CategoryStyle(Color(0xFF2196F3), "🔢"),
        "История" to CategoryStyle(Color(0xFF795548), "📜"),
        "Наука" to CategoryStyle(Color(0xFF9C27B0), "🔬"),
        "Музыка" to CategoryStyle(Color(0xFFFF5722), "🎵"),
        "Природа" to CategoryStyle(Color(0xFF009688), "🌿"),
        "География" to CategoryStyle(Color(0xFF795548), "🌍"),
        "Космос" to CategoryStyle(Color(0xFF3F51B5), "🚀"),
        "Человек" to CategoryStyle(Color(0xFFE91E63), "👤"),
        "Время" to CategoryStyle(Color(0xFF607D8B), "⏰"),
        "Времена года" to CategoryStyle(Color(0xFF8BC34A), "🍂"),
        "Еда" to CategoryStyle(Color(0xFFFF5722), "🍎"),
        "Школа" to CategoryStyle(Color(0xFF2196F3), "📚"),
        "Буквы" to CategoryStyle(Color(0xFF9C27B0), "🔤"),
    )

    fun forCategory(category: String): CategoryStyle {
        return styles[category] ?: CategoryStyle(Color(0xFF4CAF50), "❓")
    }

    fun greetingByTimeOfDay(): String {
        val hour = java.time.LocalTime.now().hour
        return when (hour) {
            in 5..11 -> "Доброе утро!"
            in 12..16 -> "Привет! Готов узнать что-то новое?"
            in 17..21 -> "Вечерняя разминка!"
            else -> "Доброй ночи! Последний вопрос?"
        }
    }
}
