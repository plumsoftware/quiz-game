package ru.plumsoftware.game.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class Question(
    @SerialName("id") val id: Int,
    @SerialName("question") val question: String,
    @SerialName("options") val options: List<String>,
    @SerialName("correct_answer") val correctAnswer: Int,
    @SerialName("category") val category: String,
    @SerialName("difficulty") val difficulty: Int = 1,
    @SerialName("level") val level: Int = 1
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
    val playerName: String = "Игрок",
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
    val categoriesPlayed: Set<String> = emptySet(),
    val powerUpInventory: Map<String, Int> = emptyMap()
)

@Serializable
data class Quiz(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("category") val category: String,
    @SerialName("difficulty") val difficulty: Int,
    @SerialName("required_level") val requiredLevel: Int,
    @SerialName("questions") val questions: List<Question>
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

    // Level 1 - Additional questions (IDs 121-130)
    val level1NewQuestions = listOf(
        Question(
            id = 121,
            question = "Сколько глаз у человека?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 1,
            category = "Человек",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 122,
            question = "Какой формы яйцо?",
            options = listOf("Круглое", "Овальное", "Квадратное", "Треугольное"),
            correctAnswer = 1,
            category = "Фигуры",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 123,
            question = "Какая птица не умеет летать, но умеет плавать?",
            options = listOf("Воробей", "Пингвин", "Орел", "Голубь"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 124,
            question = "Что мы надеваем на ноги?",
            options = listOf("Шапку", "Перчатки", "Обувь", "Шарф"),
            correctAnswer = 2,
            category = "Школа",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 125,
            question = "Какого цвета апельсин?",
            options = listOf("Зеленый", "Оранжевый", "Синий", "Фиолетовый"),
            correctAnswer = 1,
            category = "Цвета",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 126,
            question = "Сколько колес у велосипеда?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 1,
            category = "Числа",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 127,
            question = "Что светит на небе днем?",
            options = listOf("Луна", "Звезды", "Солнце", "Кометы"),
            correctAnswer = 2,
            category = "Природа",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 128,
            question = "Какое время суток самое темное?",
            options = listOf("Утро", "День", "Вечер", "Ночь"),
            correctAnswer = 3,
            category = "Время",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 129,
            question = "Какой овощ оранжевый и растет в земле?",
            options = listOf("Морковь", "Капуста", "Огурец", "Лук"),
            correctAnswer = 0,
            category = "Еда",
            difficulty = 1,
            level = 1
        ),
        Question(
            id = 130,
            question = "Сколько букв в слове 'кот'?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
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

    // Level 2 - Additional questions (IDs 131-140)
    val level2NewQuestions = listOf(
        Question(
            id = 131,
            question = "Какая самая большая страна в мире по площади?",
            options = listOf("Китай", "США", "Россия", "Канада"),
            correctAnswer = 2,
            category = "География",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 132,
            question = "Сколько ног у паука?",
            options = listOf("4", "6", "8", "10"),
            correctAnswer = 2,
            category = "Животные",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 133,
            question = "Какая планета известна своими кольцами?",
            options = listOf("Марс", "Сатурн", "Венера", "Меркурий"),
            correctAnswer = 1,
            category = "Космос",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 134,
            question = "Сколько камер в человеческом сердце?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 2,
            category = "Человек",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 135,
            question = "Сколько секунд в минуте?",
            options = listOf("30", "60", "90", "100"),
            correctAnswer = 1,
            category = "Время",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 136,
            question = "Какое животное меняет цвет кожи, чтобы прятаться?",
            options = listOf("Хамелеон", "Кошка", "Кролик", "Лошадь"),
            correctAnswer = 0,
            category = "Животные",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 137,
            question = "Сколько сторон у пятиугольника?",
            options = listOf("4", "5", "6", "7"),
            correctAnswer = 1,
            category = "Фигуры",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 138,
            question = "Какая самая маленькая страна в мире?",
            options = listOf("Монако", "Ватикан", "Лихтенштейн", "Андорра"),
            correctAnswer = 1,
            category = "География",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 139,
            question = "Сколько литров воды нужно выпивать человеку в день примерно?",
            options = listOf("0.5 литра", "1 литр", "2 литра", "5 литров"),
            correctAnswer = 2,
            category = "Человек",
            difficulty = 2,
            level = 2
        ),
        Question(
            id = 140,
            question = "Какая звезда находится в центре нашей Солнечной системы?",
            options = listOf("Полярная звезда", "Солнце", "Сириус", "Венера"),
            correctAnswer = 1,
            category = "Космос",
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

    // Level 3 - Additional questions (IDs 141-150)
    val level3NewQuestions = listOf(
        Question(
            id = 141,
            question = "Какое животное может прожить без воды дольше верблюда?",
            options = listOf("Крыса кенгуровая", "Лошадь", "Корова", "Овца"),
            correctAnswer = 0,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 142,
            question = "Сколько слоев у сетчатки человеческого глаза?",
            options = listOf("3", "5", "10", "12"),
            correctAnswer = 2,
            category = "Человек",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 143,
            question = "Какой газ растения выделяют при фотосинтезе?",
            options = listOf("Углекислый газ", "Кислород", "Азот", "Водород"),
            correctAnswer = 1,
            category = "Наука",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 144,
            question = "Сколько континентов на Земле?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "География",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 145,
            question = "Какое животное является самым крупным млекопитающим в мире?",
            options = listOf("Слон", "Синий кит", "Жираф", "Бегемот"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 146,
            question = "Сколько букв в слове 'путешествие'?",
            options = listOf("10", "11", "12", "13"),
            correctAnswer = 1,
            category = "Буквы",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 147,
            question = "Какая страна самая населенная в мире?",
            options = listOf("Китай", "США", "Индия", "Бразилия"),
            correctAnswer = 2,
            category = "География",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 148,
            question = "Сколько сторон у шестиугольника?",
            options = listOf("4", "5", "6", "7"),
            correctAnswer = 2,
            category = "Фигуры",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 149,
            question = "Какое время года самое короткое в большинстве регионов?",
            options = listOf("Весна", "Лето", "Осень", "Зима"),
            correctAnswer = 0,
            category = "Времена года",
            difficulty = 3,
            level = 3
        ),
        Question(
            id = 150,
            question = "Сколько линз у простого микроскопа?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 0,
            category = "Наука",
            difficulty = 3,
            level = 3
        )
    )

    // Level 4 - Expert questions (IDs 61-80)
    val level4Questions = listOf(
        Question(
            id = 61,
            question = "Какая самая длинная река в Европе?",
            options = listOf("Дунай", "Волга", "Рейн", "Днепр"),
            correctAnswer = 1,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 62,
            question = "Сколько хромосом у шимпанзе?",
            options = listOf("44", "46", "48", "50"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 63,
            question = "Какая самая высокая гора в Африке?",
            options = listOf("Килиманджаро", "Эльбрус", "Монблан", "Эверест"),
            correctAnswer = 0,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 64,
            question = "Сколько планет в Солнечной системе имеют спутники?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "Космос",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 65,
            question = "Какая самая большая пустыня в Азии?",
            options = listOf("Гоби", "Сахара", "Аравийская", "Калахари"),
            correctAnswer = 0,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 66,
            question = "Сколько костей в теле новорожденного ребенка?",
            options = listOf("206", "270", "300", "350"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 67,
            question = "Какая самая быстрая рыба в мире?",
            options = listOf("Тунец", "Марлин", "Парусник", "Акула"),
            correctAnswer = 2,
            category = "Животные",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 68,
            question = "Сколько спутников у Юпитера?",
            options = listOf("50", "60", "70", "80"),
            correctAnswer = 2,
            category = "Космос",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 69,
            question = "Какая самая длинная кость в теле человека?",
            options = listOf("Плечевая", "Бедренная", "Большеберцовая", "Локтевая"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 70,
            question = "Сколько цветов в радуге?",
            options = listOf("5", "6", "7", "8"),
            correctAnswer = 2,
            category = "Физика",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 71,
            question = "Какая самая маленькая планета в Солнечной системе?",
            options = listOf("Меркурий", "Марс", "Венера", "Земля"),
            correctAnswer = 0,
            category = "Космос",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 72,
            question = "Сколько мышц в человеческом теле?",
            options = listOf("400", "600", "800", "1000"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 73,
            question = "Какая самая глубокая точка океана?",
            options = listOf("Марианская впадина", "Пуэрто-Рико", "Яванская впадина", "Филиппинская впадина"),
            correctAnswer = 0,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 74,
            question = "Сколько хромосом у человека?",
            options = listOf("42", "44", "46", "48"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 75,
            question = "Какая самая быстрая птица в мире?",
            options = listOf("Орел", "Сокол", "Стриж", "Ястреб"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 76,
            question = "Сколько спутников у Земли?",
            options = listOf("0", "1", "2", "3"),
            correctAnswer = 1,
            category = "Космос",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 77,
            question = "Какая самая большая пустыня в мире?",
            options = listOf("Сахара", "Гоби", "Аравийская", "Калахари"),
            correctAnswer = 0,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 78,
            question = "Сколько костей в черепе взрослого человека?",
            options = listOf("20", "22", "24", "26"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 79,
            question = "Какая самая длинная змея в мире?",
            options = listOf("Питон", "Анаконда", "Кобра", "Гадюка"),
            correctAnswer = 1,
            category = "Животные",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 80,
            question = "Сколько планет в Солнечной системе имеют кольца?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 3,
            category = "Космос",
            difficulty = 4,
            level = 4
        )
    )

    // Level 4 - Additional questions (IDs 151-160)
    val level4NewQuestions = listOf(
        Question(
            id = 151,
            question = "Какая самая длинная горная цепь в мире?",
            options = listOf("Гималаи", "Анды", "Альпы", "Уральские горы"),
            correctAnswer = 1,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 152,
            question = "Сколько литров крови в организме взрослого человека примерно?",
            options = listOf("2-3 литра", "5-6 литров", "8-9 литров", "10-12 литров"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 153,
            question = "Какая галактика содержит нашу Солнечную систему?",
            options = listOf("Андромеда", "Млечный Путь", "Туманность Ориона", "Большое Магелланово Облако"),
            correctAnswer = 1,
            category = "Космос",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 154,
            question = "Какое животное может регенерировать утраченные конечности?",
            options = listOf("Ящерица", "Кролик", "Лиса", "Белка"),
            correctAnswer = 0,
            category = "Животные",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 155,
            question = "Сколько слоев имеет атмосфера Земли?",
            options = listOf("3", "4", "5", "6"),
            correctAnswer = 2,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 156,
            question = "Какой элемент самый распространенный во Вселенной?",
            options = listOf("Кислород", "Углерод", "Водород", "Гелий"),
            correctAnswer = 2,
            category = "Физика",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 157,
            question = "Сколько камер имеет желудок коровы?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 3,
            category = "Биология",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 158,
            question = "Какой океан самый большой на Земле?",
            options = listOf("Атлантический", "Индийский", "Тихий", "Северный Ледовитый"),
            correctAnswer = 2,
            category = "География",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 159,
            question = "Сколько лет длится один оборот Юпитера вокруг Солнца?",
            options = listOf("около 5 лет", "около 12 лет", "около 20 лет", "около 30 лет"),
            correctAnswer = 1,
            category = "Космос",
            difficulty = 4,
            level = 4
        ),
        Question(
            id = 160,
            question = "Какая самая ядовитая лягушка в мире?",
            options = listOf("Древолаз", "Жаба-ага", "Квакша", "Лягушка-бык"),
            correctAnswer = 0,
            category = "Животные",
            difficulty = 4,
            level = 4
        )
    )

    // Level 5 - Master questions (IDs 81-100)
    val level5Questions = listOf(
        Question(
            id = 81,
            question = "Какая самая большая клетка в человеческом теле?",
            options = listOf("Нейрон", "Яйцеклетка", "Мышечная клетка", "Клетка печени"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 82,
            question = "Сколько атомов в молекуле воды?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Химия",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 83,
            question = "Какая самая быстрая частица в природе?",
            options = listOf("Электрон", "Фотон", "Нейтрино", "Протон"),
            correctAnswer = 1,
            category = "Физика",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 84,
            question = "Сколько костей в человеческой руке?",
            options = listOf("25", "27", "29", "31"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 85,
            question = "Какая самая длинная кость в теле человека?",
            options = listOf("Плечевая", "Бедренная", "Большеберцовая", "Локтевая"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 86,
            question = "Сколько хромосом у собаки?",
            options = listOf("36", "38", "40", "42"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 87,
            question = "Какая самая маленькая кость в человеческом теле?",
            options = listOf("Молоточек", "Наковальня", "Стремечко", "Височная кость"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 88,
            question = "Сколько мышц в человеческом лице?",
            options = listOf("20", "30", "40", "50"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 89,
            question = "Какая самая большая артерия в человеческом теле?",
            options = listOf("Аорта", "Сонная артерия", "Бедренная артерия", "Подключичная артерия"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 90,
            question = "Сколько костей в человеческой ноге?",
            options = listOf("24", "26", "28", "30"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 91,
            question = "Какая самая длинная мышца в человеческом теле?",
            options = listOf("Двуглавая мышца", "Портняжная мышца", "Икроножная мышца", "Четырехглавая мышца"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 92,
            question = "Сколько хромосом у кошки?",
            options = listOf("36", "38", "40", "42"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 93,
            question = "Какая самая большая вена в человеческом теле?",
            options = listOf("Верхняя полая вена", "Нижняя полая вена", "Яремная вена", "Бедренная вена"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 94,
            question = "Сколько костей в человеческом позвоночнике?",
            options = listOf("30", "32", "33", "35"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 95,
            question = "Какая самая маленькая мышца в человеческом теле?",
            options = listOf("Мышца стремени", "Мышца молоточка", "Мышца наковальни", "Мышца барабанной перепонки"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 96,
            question = "Сколько хромосом у лошади?",
            options = listOf("60", "62", "64", "66"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 97,
            question = "Какая самая большая кость в человеческом черепе?",
            options = listOf("Лобная кость", "Теменная кость", "Височная кость", "Затылочная кость"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 98,
            question = "Сколько мышц в человеческом сердце?",
            options = listOf("1", "2", "3", "4"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 99,
            question = "Какая самая длинная кость в человеческом черепе?",
            options = listOf("Височная кость", "Теменная кость", "Лобная кость", "Затылочная кость"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 100,
            question = "Сколько хромосом у коровы?",
            options = listOf("58", "60", "62", "64"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        )
    )

    // Level 5 - Additional questions (IDs 161-170)
    val level5NewQuestions = listOf(
        Question(
            id = 161,
            question = "Сколько литров воздуха вмещают легкие взрослого человека примерно?",
            options = listOf("1-2 литра", "3-4 литра", "5-6 литров", "8-9 литров"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 162,
            question = "Какой химический элемент обозначается символом 'Au'?",
            options = listOf("Серебро", "Золото", "Алюминий", "Аргон"),
            correctAnswer = 1,
            category = "Химия",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 163,
            question = "Какая частица не имеет электрического заряда?",
            options = listOf("Протон", "Электрон", "Нейтрон", "Позитрон"),
            correctAnswer = 2,
            category = "Физика",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 164,
            question = "Сколько хромосом у курицы?",
            options = listOf("70", "78", "82", "90"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 165,
            question = "Какая самая твердая природная ткань в организме человека?",
            options = listOf("Кость", "Зубная эмаль", "Хрящ", "Ноготь"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 166,
            question = "Сколько пар хромосом у человека?",
            options = listOf("21", "22", "23", "24"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 167,
            question = "Какой газ составляет большую часть земной атмосферы?",
            options = listOf("Кислород", "Углекислый газ", "Азот", "Водород"),
            correctAnswer = 2,
            category = "Химия",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 168,
            question = "Какая самая тяжелая внутренняя железа в человеческом теле?",
            options = listOf("Печень", "Селезенка", "Почка", "Поджелудочная железа"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 169,
            question = "Сколько групп крови существует у человека по основной системе AB0?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 5,
            level = 5
        ),
        Question(
            id = 170,
            question = "Какой самый легкий газ в природе?",
            options = listOf("Гелий", "Водород", "Азот", "Кислород"),
            correctAnswer = 1,
            category = "Физика",
            difficulty = 5,
            level = 5
        )
    )

    // Level 6 - Grand Master questions (IDs 101-120)
    val level6Questions = listOf(
        Question(
            id = 101,
            question = "Какая самая большая молекула в природе?",
            options = listOf("ДНК", "Белки", "Углеводы", "Липиды"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 102,
            question = "Сколько нейронов в человеческом мозге?",
            options = listOf("50 миллиардов", "86 миллиардов", "100 миллиардов", "150 миллиардов"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 103,
            question = "Какая самая быстрая химическая реакция?",
            options = listOf("Горение", "Взрыв", "Ядерная реакция", "Фотосинтез"),
            correctAnswer = 2,
            category = "Химия",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 104,
            question = "Сколько костей в человеческом ухе?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 105,
            question = "Какая самая маленькая клетка в человеческом теле?",
            options = listOf("Сперматозоид", "Яйцеклетка", "Клетка крови", "Нейрон"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 106,
            question = "Сколько мышц в человеческом языке?",
            options = listOf("6", "8", "10", "12"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 107,
            question = "Какая самая большая железа в человеческом теле?",
            options = listOf("Печень", "Поджелудочная железа", "Щитовидная железа", "Надпочечники"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 108,
            question = "Сколько хромосом у шимпанзе?",
            options = listOf("44", "46", "48", "50"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 109,
            question = "Какая самая длинная кость в человеческом черепе?",
            options = listOf("Височная кость", "Теменная кость", "Лобная кость", "Затылочная кость"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 110,
            question = "Сколько мышц в человеческом глазе?",
            options = listOf("4", "6", "8", "10"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 111,
            question = "Какая самая маленькая кость в человеческом теле?",
            options = listOf("Молоточек", "Наковальня", "Стремечко", "Височная кость"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 112,
            question = "Сколько хромосом у гориллы?",
            options = listOf("44", "46", "48", "50"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 113,
            question = "Какая самая большая мышца в человеческом теле?",
            options = listOf("Четырехглавая мышца", "Ягодичная мышца", "Икроножная мышца", "Двуглавая мышца"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 114,
            question = "Сколько костей в человеческом пальце?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 115,
            question = "Какая самая длинная мышца в человеческом теле?",
            options = listOf("Двуглавая мышца", "Портняжная мышца", "Икроножная мышца", "Четырехглавая мышца"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 116,
            question = "Сколько хромосом у орангутанга?",
            options = listOf("44", "46", "48", "50"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 117,
            question = "Какая самая маленькая мышца в человеческом теле?",
            options = listOf("Мышца стремени", "Мышца молоточка", "Мышца наковальни", "Мышца барабанной перепонки"),
            correctAnswer = 0,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 118,
            question = "Сколько костей в человеческом запястье?",
            options = listOf("6", "8", "10", "12"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 119,
            question = "Какая самая большая кость в человеческом черепе?",
            options = listOf("Лобная кость", "Теменная кость", "Височная кость", "Затылочная кость"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 120,
            question = "Сколько хромосом у человека?",
            options = listOf("42", "44", "46", "48"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        )
    )

    // Level 6 - Additional questions (IDs 171-180)
    val level6NewQuestions = listOf(
        Question(
            id = 171,
            question = "Сколько пар ребер у взрослого человека?",
            options = listOf("10", "12", "14", "16"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 172,
            question = "Какая частица переносит электромагнитное взаимодействие?",
            options = listOf("Электрон", "Фотон", "Нейтрино", "Глюон"),
            correctAnswer = 1,
            category = "Физика",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 173,
            question = "Сколько аминокислот участвует в строении человеческих белков?",
            options = listOf("10", "15", "20", "25"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 174,
            question = "Какая кость является единственной подвижно соединенной с черепом?",
            options = listOf("Височная", "Нижняя челюсть", "Скуловая", "Затылочная"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 175,
            question = "Сколько хромосом у дрозофилы (плодовой мушки)?",
            options = listOf("4", "6", "8", "10"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 176,
            question = "Какой слой земной коры самый тонкий — океанический или континентальный?",
            options = listOf("Океанический", "Континентальный", "Они одинаковы", "Зависит от региона"),
            correctAnswer = 0,
            category = "География",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 177,
            question = "Сколько типов мышечной ткани выделяют в организме человека?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 178,
            question = "Какая структура клетки отвечает за производство энергии?",
            options = listOf("Ядро", "Митохондрия", "Рибосома", "Лизосома"),
            correctAnswer = 1,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 179,
            question = "Сколько камер сердца у птиц?",
            options = listOf("2", "3", "4", "5"),
            correctAnswer = 2,
            category = "Биология",
            difficulty = 6,
            level = 6
        ),
        Question(
            id = 180,
            question = "Какая элементарная частица отвечает за массу других частиц согласно физике?",
            options = listOf("Фотон", "Бозон Хиггса", "Глюон", "Нейтрино"),
            correctAnswer = 1,
            category = "Физика",
            difficulty = 6,
            level = 6
        )
    )

    val allQuestions = level1Questions + level1NewQuestions +
            level2Questions + level2NewQuestions +
            level3Questions + level3NewQuestions +
            level4Questions + level4NewQuestions +
            level5Questions + level5NewQuestions +
            level6Questions + level6NewQuestions

    private fun quizQuestions(
        primary: List<Question>,
        levelPool: List<Question>,
        count: Int,
        minCount: Int = 6
    ): List<Question> {
        val target = count.coerceAtLeast(minCount)
        val selected = primary.take(target).toMutableList()
        if (selected.size < target) {
            val usedIds = selected.map { it.id }.toSet()
            levelPool
                .filter { it.id !in usedIds }
                .take(target - selected.size)
                .forEach { selected.add(it) }
        }
        return selected
    }

    val quizzes = listOf(
        // Easy Quizzes (Level 1)
        Quiz(
            id = 1,
            title = "🌱 Основы знаний",
            description = "Простые вопросы для начинающих",
            category = "Общие знания",
            difficulty = 1,
            requiredLevel = 1,
            questions = quizQuestions(level1Questions + level1NewQuestions, level1Questions + level1NewQuestions, 10)
        ),
        Quiz(
            id = 2,
            title = "🎨 Цвета и формы",
            description = "Изучаем цвета и геометрические фигуры",
            category = "Цвета и формы",
            difficulty = 1,
            requiredLevel = 1,
            questions = quizQuestions(
                (level1Questions + level1NewQuestions).filter { it.category == "Цвета" || it.category == "Фигуры" },
                level1Questions + level1NewQuestions,
                12
            )
        ),
        Quiz(
            id = 3,
            title = "🐾 Животный мир",
            description = "Узнаем о животных",
            category = "Животные",
            difficulty = 1,
            requiredLevel = 1,
            questions = quizQuestions(
                (level1Questions + level1NewQuestions).filter { it.category == "Животные" },
                level1Questions + level1NewQuestions,
                9
            )
        ),
        Quiz(
            id = 4,
            title = "🔢 Числа и счет",
            description = "Изучаем числа и математику",
            category = "Математика",
            difficulty = 1,
            requiredLevel = 1,
            questions = quizQuestions(
                (level1Questions + level1NewQuestions).filter { it.category == "Числа" || it.category == "Буквы" },
                level1Questions + level1NewQuestions,
                11
            )
        ),
        Quiz(
            id = 5,
            title = "🌍 Природа вокруг нас",
            description = "Изучаем окружающий мир",
            category = "Природа",
            difficulty = 1,
            requiredLevel = 1,
            questions = quizQuestions(
                (level1Questions + level1NewQuestions).filter { it.category == "Природа" || it.category == "Времена года" },
                level1Questions + level1NewQuestions,
                8
            )
        ),

        // Medium Quizzes (Level 2)
        Quiz(
            id = 6,
            title = "🌿 Средний уровень",
            description = "Вопросы средней сложности",
            category = "Общие знания",
            difficulty = 2,
            requiredLevel = 2,
            questions = quizQuestions(level2Questions + level2NewQuestions, level2Questions + level2NewQuestions, 14)
        ),
        Quiz(
            id = 7,
            title = "🏔️ География мира",
            description = "Изучаем географию",
            category = "География",
            difficulty = 2,
            requiredLevel = 2,
            questions = quizQuestions(
                (level2Questions + level2NewQuestions).filter { it.category == "География" },
                level2Questions + level2NewQuestions,
                10
            )
        ),
        Quiz(
            id = 8,
            title = "🚀 Космические знания",
            description = "Изучаем космос и планеты",
            category = "Космос",
            difficulty = 2,
            requiredLevel = 2,
            questions = quizQuestions(
                (level2Questions + level2NewQuestions).filter { it.category == "Космос" },
                level2Questions + level2NewQuestions,
                9
            )
        ),
        Quiz(
            id = 9,
            title = "👤 Человеческое тело",
            description = "Изучаем анатомию",
            category = "Биология",
            difficulty = 2,
            requiredLevel = 2,
            questions = quizQuestions(
                (level2Questions + level2NewQuestions).filter { it.category == "Человек" },
                level2Questions + level2NewQuestions,
                11
            )
        ),
        Quiz(
            id = 10,
            title = "🦁 Дикие животные",
            description = "Узнаем о диких животных",
            category = "Животные",
            difficulty = 2,
            requiredLevel = 2,
            questions = quizQuestions(
                (level2Questions + level2NewQuestions).filter { it.category == "Животные" },
                level2Questions + level2NewQuestions,
                12
            )
        ),

        // Hard Quizzes (Level 3)
        Quiz(
            id = 11,
            title = "🌳 Продвинутый уровень",
            description = "Сложные вопросы для опытных",
            category = "Общие знания",
            difficulty = 3,
            requiredLevel = 3,
            questions = quizQuestions(level3Questions + level3NewQuestions, level3Questions + level3NewQuestions, 17)
        ),
        Quiz(
            id = 12,
            title = "🧬 Биология человека",
            description = "Углубленное изучение биологии",
            category = "Биология",
            difficulty = 3,
            requiredLevel = 3,
            questions = quizQuestions(
                (level3Questions + level3NewQuestions).filter { it.category == "Человек" },
                level3Questions + level3NewQuestions,
                13
            )
        ),
        Quiz(
            id = 13,
            title = "🌊 Океаны и моря",
            description = "Изучаем водный мир",
            category = "География",
            difficulty = 3,
            requiredLevel = 3,
            questions = quizQuestions(
                (level3Questions + level3NewQuestions).filter { it.category == "География" },
                level3Questions + level3NewQuestions,
                10
            )
        ),
        Quiz(
            id = 14,
            title = "🦅 Птицы мира",
            description = "Узнаем о птицах",
            category = "Животные",
            difficulty = 3,
            requiredLevel = 3,
            questions = quizQuestions(
                (level3Questions + level3NewQuestions).filter { it.category == "Животные" },
                level3Questions + level3NewQuestions,
                11
            )
        ),
        Quiz(
            id = 15,
            title = "⚡ Физика и химия",
            description = "Основы физики и химии",
            category = "Наука",
            difficulty = 3,
            requiredLevel = 3,
            questions = quizQuestions(
                (level3Questions + level3NewQuestions).filter { it.category == "Физика" || it.category == "Химия" || it.category == "Наука" },
                level3Questions + level3NewQuestions,
                9
            )
        ),

        // Expert Quizzes (Level 4)
        Quiz(
            id = 16,
            title = "🏆 Эксперт знаний",
            description = "Специализированные вопросы",
            category = "Общие знания",
            difficulty = 4,
            requiredLevel = 4,
            questions = quizQuestions(level4Questions + level4NewQuestions, level4Questions + level4NewQuestions, 15)
        ),
        Quiz(
            id = 17,
            title = "🧠 Нервная система",
            description = "Изучаем мозг и нервы",
            category = "Биология",
            difficulty = 4,
            requiredLevel = 4,
            questions = quizQuestions(
                (level4Questions + level4NewQuestions).filter { it.category == "Биология" },
                level4Questions + level4NewQuestions,
                12
            )
        ),
        Quiz(
            id = 18,
            title = "🌍 Континенты мира",
            description = "Изучаем континенты",
            category = "География",
            difficulty = 4,
            requiredLevel = 4,
            questions = quizQuestions(
                (level4Questions + level4NewQuestions).filter { it.category == "География" },
                level4Questions + level4NewQuestions,
                10
            )
        ),
        Quiz(
            id = 19,
            title = "🐋 Морские обитатели",
            description = "Узнаем о морских животных",
            category = "Животные",
            difficulty = 4,
            requiredLevel = 4,
            questions = quizQuestions(
                (level4Questions + level4NewQuestions).filter { it.category == "Животные" },
                level4Questions + level4NewQuestions,
                11
            )
        ),
        Quiz(
            id = 20,
            title = "🔬 Научные открытия",
            description = "Великие научные открытия",
            category = "Наука",
            difficulty = 4,
            requiredLevel = 4,
            questions = quizQuestions(
                (level4Questions + level4NewQuestions).filter { it.category == "Физика" || it.category == "Химия" },
                level4Questions + level4NewQuestions,
                8
            )
        ),

        // Master Quizzes (Level 5)
        Quiz(
            id = 21,
            title = "👑 Мастер знаний",
            description = "Сложнейшие вопросы",
            category = "Общие знания",
            difficulty = 5,
            requiredLevel = 5,
            questions = quizQuestions(level5Questions + level5NewQuestions, level5Questions + level5NewQuestions, 16)
        ),
        Quiz(
            id = 22,
            title = "💓 Сердечно-сосудистая система",
            description = "Изучаем сердце и сосуды",
            category = "Биология",
            difficulty = 5,
            requiredLevel = 5,
            questions = quizQuestions(
                (level5Questions + level5NewQuestions).filter { it.category == "Биология" },
                level5Questions + level5NewQuestions,
                14
            )
        ),
        Quiz(
            id = 23,
            title = "🗺️ Картография",
            description = "Изучаем карты и навигацию",
            category = "География",
            difficulty = 5,
            requiredLevel = 5,
            questions = quizQuestions(
                (level5Questions + level5NewQuestions).filter { it.category == "География" },
                level5Questions + level5NewQuestions,
                9
            )
        ),
        Quiz(
            id = 24,
            title = "🦁 Хищники мира",
            description = "Узнаем о хищных животных",
            category = "Животные",
            difficulty = 5,
            requiredLevel = 5,
            questions = quizQuestions(
                (level5Questions + level5NewQuestions).filter { it.category == "Животные" },
                level5Questions + level5NewQuestions,
                10
            )
        ),
        Quiz(
            id = 25,
            title = "⚛️ Атомная физика",
            description = "Изучаем атомы и частицы",
            category = "Физика",
            difficulty = 5,
            requiredLevel = 5,
            questions = quizQuestions(
                (level5Questions + level5NewQuestions).filter { it.category == "Физика" || it.category == "Химия" },
                level5Questions + level5NewQuestions,
                11
            )
        ),

        // Grand Master Quizzes (Level 6)
        Quiz(
            id = 26,
            title = "💎 Гроссмейстер знаний",
            description = "Ультимативные вопросы",
            category = "Общие знания",
            difficulty = 6,
            requiredLevel = 6,
            questions = quizQuestions(level6Questions + level6NewQuestions, level6Questions + level6NewQuestions, 17)
        ),
        Quiz(
            id = 27,
            title = "🧬 Генетика и ДНК",
            description = "Изучаем гены и наследственность",
            category = "Биология",
            difficulty = 6,
            requiredLevel = 6,
            questions = quizQuestions(
                (level6Questions + level6NewQuestions).filter { it.category == "Биология" },
                level6Questions + level6NewQuestions,
                15
            )
        ),
        Quiz(
            id = 28,
            title = "🌋 Вулканы и землетрясения",
            description = "Изучаем геологические процессы",
            category = "География",
            difficulty = 6,
            requiredLevel = 6,
            questions = quizQuestions(
                (level6Questions + level6NewQuestions).filter { it.category == "География" },
                level6Questions + level6NewQuestions,
                10
            )
        ),
        Quiz(
            id = 29,
            title = "🦕 Динозавры и древние животные",
            description = "Узнаем о вымерших животных",
            category = "Животные",
            difficulty = 6,
            requiredLevel = 6,
            questions = quizQuestions(
                (level6Questions + level6NewQuestions).filter { it.category == "Животные" },
                level6Questions + level6NewQuestions,
                11
            )
        ),
        Quiz(
            id = 30,
            title = "🔬 Квантовая физика",
            description = "Изучаем квантовую механику",
            category = "Физика",
            difficulty = 6,
            requiredLevel = 6,
            questions = quizQuestions(
                (level6Questions + level6NewQuestions).filter { it.category == "Физика" || it.category == "Химия" },
                level6Questions + level6NewQuestions,
                9
            )
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

    fun getQuestionsForQuiz(quizId: Int): List<Question> {
        return quizzes.find { it.id == quizId }?.questions ?: emptyList()
    }

    fun getQuiz(quizId: Int): Quiz? {
        return quizzes.find { it.id == quizId }
    }

    fun getAllQuizzes(): List<Quiz> {
        return quizzes
    }

    fun getQuizzesForDifficulty(difficulty: Int): List<Quiz> {
        return quizzes.filter { it.difficulty == difficulty }
    }

    fun getQuizzesForCategory(category: String): List<Quiz> {
        return quizzes.filter { it.category == category }
    }

    fun unlockedQuizTiersForPlayerLevel(playerLevel: Int): Int = when {
        playerLevel >= 15 -> 6
        playerLevel >= 10 -> 5
        playerLevel >= 7 -> 4
        playerLevel >= 5 -> 3
        playerLevel >= 3 -> 2
        else -> 1
    }

    fun quizzesUnlockingAtPlayerLevel(playerLevel: Int): Int {
        if (playerLevel <= 1) return 0
        val tiersAtLevel = unlockedQuizTiersForPlayerLevel(playerLevel)
        val tiersBefore = unlockedQuizTiersForPlayerLevel(playerLevel - 1)
        if (tiersAtLevel <= tiersBefore) return 0
        return quizzes.count { it.requiredLevel == tiersAtLevel }
    }

    fun quizzesUnlockingLabel(count: Int): String {
        val word = when {
            count % 10 == 1 && count % 100 != 11 -> "викторина"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "викторины"
            else -> "викторин"
        }
        return "$count $word"
    }
}