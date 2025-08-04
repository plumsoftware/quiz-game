package ru.plumsoftware.game.data

import java.time.LocalDate

data class Question(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val category: String,
    val difficulty: Int = 1,
    val level: Int = 1
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
    val streakDays: Int = 0,
    val unlockedQuizLevels: Int = 1,
    val quizzesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val streak: Int = 0,
    val playTimeMinutes: Int = 0,
    val categoriesPlayed: Set<String> = emptySet()
)

data class QuizLevel(
    val level: Int,
    val title: String,
    val description: String,
    val requiredLevel: Int,
    val questions: List<Question>
)

object GameData {
    // Level 1 - Easy questions (IDs 1-20)
    val level1Questions = listOf(
        Question(
            id = 1,
            question = "Какого цвета небо в солнечный день?",
            options = listOf("Синее", "Зеленое", "Красное", "Желтое"),
            correctAnswer = 0,
            category = "Цвета",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 2,
            question = "Сколько пальцев на одной руке?",
            options = listOf("3", "4", "5", "6"),
            correctAnswer = 2,
            category = "Числа",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 3,
            question = "Какое животное говорит 'мяу'?",
            options = listOf("Собака", "Кошка", "Птица", "Рыба"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 4,
            question = "Что нужно растениям для роста?",
            options = listOf("Вода", "Конфеты", "Игрушки", "Книги"),
            correctAnswer = 0,
            category = "Природа",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 5,
            question = "Какая форма у круга?",
            options = listOf("Квадрат", "Треугольник", "Круг", "Прямоугольник"),
            correctAnswer = 2,
            category = "Фигуры",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 6,
            question = "Сколько дней в неделе?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "Время",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 7,
            question = "Какой фрукт желтый и растет на деревьях?",
            options = listOf("Яблоко", "Банан", "Апельсин", "Виноград"),
            correctAnswer = 1,
            category = "Еда",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 8,
            question = "Чем пишут на бумаге?",
            options = listOf("Вилка", "Карандаш", "Обувь", "Шляпа"),
            correctAnswer = 1,
            category = "Школа",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 9,
            question = "Какой звук издает собака?",
            options = listOf("Мяу", "Гав", "Му", "Хрю"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 10,
            question = "Какого цвета большинство листьев?",
            options = listOf("Синие", "Зеленые", "Красные", "Фиолетовые"),
            correctAnswer = 1,
            category = "Цвета",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 11,
            question = "Сколько букв в слове 'мама'?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 2,
            category = "Буквы",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 12,
            question = "Какое время года после зимы?",
            options = listOf("Лето", "Осень", "Весна", "Зима"),
            correctAnswer = 2,
            category = "Времена года",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 13,
            question = "Сколько ног у кошки?",
            options = listOf("2", "3", "4", "6"),
            correctAnswer = 2,
            category = "Животные",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 14,
            question = "Какой цвет у снега?",
            options = listOf("Черный", "Белый", "Красный", "Синий"),
            correctAnswer = 1,
            category = "Цвета",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 15,
            question = "Сколько месяцев в году?",
            options = listOf("10", "11", "12", "13"),
            correctAnswer = 2,
            category = "Время",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 16,
            question = "Какое животное живет в воде?",
            options = listOf("Кошка", "Собака", "Рыба", "Птица"),
            correctAnswer = 2,
            category = "Животные",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 17,
            question = "Какой фрукт красный и круглый?",
            options = listOf("Банан", "Яблоко", "Апельсин", "Груша"),
            correctAnswer = 1,
            category = "Еда",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 18,
            question = "Сколько углов у треугольника?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Фигуры",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 19,
            question = "Какое время суток после утра?",
            options = listOf("Ночь", "Вечер", "День", "Утро"),
            correctAnswer = 2,
            category = "Время",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 20,
            question = "Сколько букв в русском алфавите?",
            options = listOf("30", "31", "32", "33"),
            correctAnswer = 2,
            category = "Буквы",
            difficulty = 1,
            level = 1
        )
    )

    // Level 2 - Medium questions (IDs 21-40)
    val level2Questions = listOf(
        Question(
            id = 21,
            question = "Какое животное самое большое на суше?",
            options = listOf("Лев", "Слон", "Жираф", "Тигр"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 22,
            question = "Сколько цветов в радуге?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "Цвета",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 23,
            question = "Какая планета ближе всего к Солнцу?",
            options = listOf("Земля", "Марс", "Меркурий", "Венера"),
            correctAnswer = 2,
            category = "Космос",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 24,
            question = "Сколько костей в теле взрослого человека?",
            options = listOf("156", "206", "256", "306"),
            correctAnswer = 1,
            category = "Человек",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 25,
            question = "Какая самая длинная река в мире?",
            options = listOf("Амазонка", "Нил", "Янцзы", "Миссисипи"),
            correctAnswer = 1,
            category = "География",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 26,
            question = "Сколько часов в сутках?",
            options = listOf("20", "22", "24", "26"),
            correctAnswer = 2,
            category = "Время",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 27,
            question = "Какое животное откладывает яйца?",
            options = listOf("Кошка", "Собака", "Курица", "Корова"),
            correctAnswer = 2,
            category = "Животные",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 28,
            question = "Сколько сторон у квадрата?",
            options = listOf("3", "4", "5", "6"),
            correctAnswer = 1,
            category = "Фигуры",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 29,
            question = "Какая столица России?",
            options = listOf("Санкт-Петербург", "Москва", "Новосибирск", "Екатеринбург"),
            correctAnswer = 1,
            category = "География",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 30,
            question = "Сколько зубов у взрослого человека?",
            options = listOf("28", "32", "36", "40"),
            correctAnswer = 1,
            category = "Человек",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 31,
            question = "Какое время года самое холодное?",
            options = listOf("Весна", "Лето", "Осень", "Зима"),
            correctAnswer = 3,
            category = "Времена года",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 32,
            question = "Сколько букв в слове 'привет'?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "Буквы",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 33,
            question = "Какое животное живет в пустыне?",
            options = listOf("Пингвин", "Верблюд", "Белый медведь", "Тюлень"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 34,
            question = "Сколько минут в часе?",
            options = listOf("30", "45", "60", "90"),
            correctAnswer = 2,
            category = "Время",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 35,
            question = "Какая самая высокая гора в мире?",
            options = listOf("К2", "Эверест", "Килиманджаро", "Монблан"),
            correctAnswer = 1,
            category = "География",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 36,
            question = "Сколько углов у прямоугольника?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 2,
            category = "Фигуры",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 37,
            question = "Какое животное самое быстрое на суше?",
            options = listOf("Лев", "Гепард", "Тигр", "Леопард"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 38,
            question = "Сколько планет в Солнечной системе?",
            options = listOf("7", "8", "9", "10"),
            correctAnswer = 1,
            category = "Космос",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 39,
            question = "Какая самая большая планета?",
            options = listOf("Земля", "Марс", "Юпитер", "Сатурн"),
            correctAnswer = 2,
            category = "Космос",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 40,
            question = "Сколько букв в слове 'спасибо'?",
            options = listOf("6", "7", "8", "9"),
            correctAnswer = 2,
            category = "Буквы",
            difficulty = 2,
            level = 2
        )
    )

    // Level 3 - Hard questions (IDs 41-60)
    val level3Questions = listOf(
        Question(
            id = 41,
            question = "Какое животное спит зимой?",
            options = listOf("Лиса", "Медведь", "Волк", "Заяц"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 42,
            question = "Сколько цветов у светофора?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Цвета",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 43,
            question = "Какая самая длинная кость в теле человека?",
            options = listOf("Плечевая", "Бедренная", "Локтевая", "Большеберцовая"),
            correctAnswer = 1,
            category = "Человек",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 44,
            question = "Сколько дней в феврале в високосный год?",
            options = listOf("28", "29", "30", "31"),
            correctAnswer = 1,
            category = "Время",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 45,
            question = "Какое животное самое умное?",
            options = listOf("Собака", "Дельфин", "Обезьяна", "Слон"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 46,
            question = "Сколько букв в слове 'математика'?",
            options = listOf("8", "9", "10", "11"),
            correctAnswer = 2,
            category = "Буквы",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 47,
            question = "Какая самая маленькая птица в мире?",
            options = listOf("Воробей", "Колибри", "Синица", "Чиж"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 48,
            question = "Сколько сторон у треугольника?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Фигуры",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 49,
            question = "Какое время года самое жаркое?",
            options = listOf("Весна", "Лето", "Осень", "Зима"),
            correctAnswer = 1,
            category = "Времена года",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 50,
            question = "Сколько букв в русском алфавите?",
            options = listOf("30", "31", "32", "33"),
            correctAnswer = 2,
            category = "Буквы",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 51,
            question = "Какая самая глубокая точка океана?",
            options = listOf("Марианская впадина", "Пуэрто-Рико", "Яванская впадина", "Филиппинская впадина"),
            correctAnswer = 0,
            category = "География",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 52,
            question = "Сколько хромосом у человека?",
            options = listOf("42", "44", "46", "48"),
            correctAnswer = 2,
            category = "Человек",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 53,
            question = "Какая самая быстрая птица?",
            options = listOf("Орел", "Сокол", "Стриж", "Ястреб"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 54,
            question = "Сколько спутников у Земли?",
            options = listOf("0", "1", "2", "3"),
            correctAnswer = 1,
            category = "Космос",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 55,
            question = "Какая самая большая пустыня в мире?",
            options = listOf("Сахара", "Гоби", "Аравийская", "Калахари"),
            correctAnswer = 0,
            category = "География",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 56,
            question = "Сколько мышц в теле человека?",
            options = listOf("400", "600", "800", "1000"),
            correctAnswer = 1,
            category = "Человек",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 57,
            question = "Какая самая длинная змея в мире?",
            options = listOf("Питон", "Анаконда", "Кобра", "Гадюка"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 58,
            question = "Сколько планет в Солнечной системе имеют кольца?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 3,
            category = "Космос",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 59,
            question = "Какая самая маленькая планета?",
            options = listOf("Меркурий", "Марс", "Венера", "Земля"),
            correctAnswer = 0,
            category = "Космос",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 60,
            question = "Сколько костей в черепе взрослого человека?",
            options = listOf("20", "22", "24", "26"),
            correctAnswer = 1,
            category = "Человек",
            difficulty = 3,
            level = 3
        )
    )

    val allQuestions = level1Questions + level2Questions + level3Questions

    val quizLevels = listOf(
        QuizLevel(
            level = 1,
            title = "Начинающий",
            description = "Простые вопросы для самых маленьких",
            requiredLevel = 1,
            questions = level1Questions
        ),
        QuizLevel(
            level = 2,
            title = "Средний",
            description = "Вопросы средней сложности",
            requiredLevel = 3,
            questions = level2Questions
        ),
        QuizLevel(
            level = 3,
            title = "Продвинутый",
            description = "Сложные вопросы для опытных игроков",
            requiredLevel = 5,
            questions = level3Questions
        )
    )

    fun getDailyTasks(): List<DailyTask> {
        return listOf(
            DailyTask(
                id = 1,
                title = "Завершить 3 викторины",
                description = "Отвечай на вопросы и зарабатывай монеты!",
                reward = 50
            ),
            DailyTask(
                id = 2,
                title = "Получить 5 правильных ответов",
                description = "Покажи свои знания!",
                reward = 30
            ),
            DailyTask(
                id = 3,
                title = "Играть 10 минут",
                description = "Весело учись!",
                reward = 25
            ),
            DailyTask(
                id = 4,
                title = "Ответить на все категории",
                description = "Попробуй вопросы из разных тем!",
                reward = 40
            ),
            DailyTask(
                id = 5,
                title = "Отличный результат",
                description = "Получи все ответы правильно в одной викторине!",
                reward = 100
            ),
            DailyTask(
                id = 6,
                title = "Пройди новый уровень",
                description = "Разблокируй и пройди следующий уровень викторины!",
                reward = 75
            ),
            DailyTask(
                id = 7,
                title = "Собери 100 монет",
                description = "Накопи монеты для покупок в магазине!",
                reward = 50
            )
        )
    }

    fun getCategories(): List<String> {
        return allQuestions.map { it.category }.distinct()
    }

    fun getQuestionsForLevel(level: Int): List<Question> {
        return when (level) {
            1 -> level1Questions
            2 -> level2Questions
            3 -> level3Questions
            else -> level1Questions
        }
    }

    fun getQuizLevel(level: Int): QuizLevel? {
        return quizLevels.find { it.level == level }
    }
} 