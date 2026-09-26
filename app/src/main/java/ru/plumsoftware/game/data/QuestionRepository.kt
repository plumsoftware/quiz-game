package ru.plumsoftware.game.data

import android.content.Context
import kotlin.random.Random

/**
 * Вопросы из `assets/questions/{topicId}_{difficulty}.json` (ТЗ §2.3).
 * Тема «Математика» генерируется, если для неё нет JSON.
 * Новые вопросы добавляются просто новыми файлами — тема сама станет доступной.
 */
class QuestionRepository(context: Context) {
    private val assets = context.applicationContext.assets
    private val cache = HashMap<String, List<QuizQuestion>>()

    /** Темы, для которых есть контент (файл хотя бы для одной сложности, либо генератор). */
    val playableTopics: Set<String> by lazy {
        val files = try {
            assets.list(DIR)?.toList().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        val fromFiles = files.filter { it.endsWith(".json") }
            .map { it.removeSuffix(".json").substringBeforeLast('_') }
            .toSet()
        fromFiles + MathGenerator.TOPIC
    }

    fun hasContent(topicId: String): Boolean = topicId in playableTopics

    @Synchronized
    fun pool(topicId: String, difficulty: Int): List<QuizQuestion> = cache.getOrPut("${topicId}_$difficulty") {
        val raw = try {
            assets.open("$DIR/${topicId}_$difficulty.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        } catch (e: Exception) {
            null
        }
        raw?.let { QuestionParser.parse(it, topicId, difficulty) }.orEmpty()
    }

    /**
     * Вопросы уровня: обычный — 5, босс — 10 (§6.1).
     * Порядок вопросов и вариантов ответа перемешивается при каждом показе (§2.3).
     */
    fun questionsForLevel(topicId: String, difficulty: Int, level: Int, random: Random = Random.Default): List<QuizQuestion> {
        val count = LevelSelector.questionsPerLevel(level)
        val pool = pool(topicId, difficulty)
        val base = if (pool.isEmpty() && topicId == MathGenerator.TOPIC) {
            MathGenerator.generate(difficulty, level, count, random)
        } else {
            LevelSelector.select(pool, topicId, difficulty, level)
        }
        return base.shuffled(random).map { it.shuffledOptions(random) }
    }

    companion object {
        const val DIR = "questions"
    }
}
