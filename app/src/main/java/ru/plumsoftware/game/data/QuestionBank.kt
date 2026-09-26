package ru.plumsoftware.game.data

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlin.random.Random

/**
 * Вопрос викторины в формате ТЗ §2.3:
 * `{id, topic, difficulty, level, text, image, options, answer, explanation}`.
 */
data class QuizQuestion(
    val id: String,
    val topic: String,
    val difficulty: Int,
    val level: Int,
    val text: String,
    val image: String?,
    val options: List<String>,
    val answer: Int,
    val explanation: String?
) {
    val correctText: String get() = options.getOrElse(answer) { "" }

    /** Перемешивает варианты ответа (§2.3: порядок меняется при каждом показе). */
    fun shuffledOptions(random: Random = Random.Default): QuizQuestion {
        val order = options.indices.shuffled(random)
        return copy(options = order.map { options[it] }, answer = order.indexOf(answer))
    }
}

object QuestionParser {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /** Разбирает JSON-массив вопросов. Некорректные элементы пропускаются. */
    fun parse(raw: String, fallbackTopic: String = "", fallbackDifficulty: Int = 0): List<QuizQuestion> {
        val root = try {
            json.parseToJsonElement(raw)
        } catch (e: Exception) {
            return emptyList()
        }
        val array = when (root) {
            is JsonArray -> root
            is JsonObject -> root["questions"] as? JsonArray ?: return emptyList()
            else -> return emptyList()
        }
        return array.mapIndexedNotNull { index, el ->
            val o = el as? JsonObject ?: return@mapIndexedNotNull null
            try {
                val options = (o["options"] as? JsonArray)?.mapNotNull { it.jsonPrimitive.contentOrNull }
                    ?: return@mapIndexedNotNull null
                val answer = o["answer"]?.jsonPrimitive?.intOrNull ?: return@mapIndexedNotNull null
                val text = o["text"]?.jsonPrimitive?.contentOrNull ?: return@mapIndexedNotNull null
                if (options.size < 2 || answer !in options.indices || text.isBlank()) return@mapIndexedNotNull null
                val topic = o["topic"]?.jsonPrimitive?.contentOrNull ?: fallbackTopic
                val difficulty = o["difficulty"]?.jsonPrimitive?.intOrNull ?: fallbackDifficulty
                QuizQuestion(
                    id = o["id"]?.jsonPrimitive?.contentOrNull ?: "${topic}_${difficulty}_$index",
                    topic = topic,
                    difficulty = difficulty,
                    level = o["level"]?.jsonPrimitive?.intOrNull ?: 0,
                    text = text,
                    image = o["image"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() },
                    options = options,
                    answer = answer,
                    explanation = o["explanation"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}

/** Подбор вопросов на уровень карты. */
object LevelSelector {
    fun questionsPerLevel(level: Int): Int = if (LevelMap.isBossLevel(level)) 10 else 5

    /** Сколько вопросов «съели» уровни до [level]. */
    fun offsetFor(level: Int): Int = (1 until level).sumOf { questionsPerLevel(it) }

    /**
     * Детерминированно выбирает вопросы уровня из пула:
     * 1) если в JSON есть вопросы, помеченные этим level, и их хватает — берём их;
     * 2) иначе идём по «ленте» из перемешанных копий пула, чтобы соседние уровни
     *    не повторялись и весь пул использовался равномерно.
     */
    fun select(pool: List<QuizQuestion>, topic: String, difficulty: Int, level: Int): List<QuizQuestion> {
        if (pool.isEmpty()) return emptyList()
        val count = questionsPerLevel(level)
        val exact = pool.filter { it.level == level }
        if (exact.size >= count) return exact.take(count)

        val sorted = pool.sortedBy { it.id }
        val n = sorted.size
        val seed = (topic.hashCode() * 31 + difficulty) * 1_000_003
        val cycles = HashMap<Int, List<QuizQuestion>>()
        fun at(globalIndex: Int): QuizQuestion {
            val cycle = globalIndex / n
            val order = cycles.getOrPut(cycle) { sorted.shuffled(Random(seed + cycle)) }
            return order[globalIndex % n]
        }

        val result = ArrayList<QuizQuestion>(count)
        val used = HashSet<String>()
        var i = offsetFor(level)
        var guard = 0
        while (result.size < minOf(count, n) && guard < n * 3) {
            val q = at(i)
            if (used.add(q.id)) result += q
            i++
            guard++
        }
        return result
    }
}
