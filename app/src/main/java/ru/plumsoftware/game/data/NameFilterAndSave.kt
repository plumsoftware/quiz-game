package ru.plumsoftware.game.data

/** Проверка имени игрока (ТЗ §5.2): 1–14 символов, обрезка пробелов, локальный список стоп-слов. */
object NameFilter {
    const val MAX_LENGTH = 14

    // Корни стоп-слов после нормализации (нижний регистр, ё→е, латиница-двойники → кириллица).
    private val stopRoots = listOf(
        "хуй", "хуе", "хуя", "хую", "пизд", "ебат", "ебан", "ебал", "ебло", "ебну", "еблан", "уеб", "заеб",
        "выеб", "бля", "сука", "суки", "сучк", "мудак", "мудил", "пидор", "пидар", "педик", "гандон",
        "гондон", "залуп", "шлюх", "дроч", "манда", "говн", "дерьм", "срать", "сраный", "жопа", "жопу",
        "секс", "порн", "нацист", "гитлер", "наркот", "убей", "убить",
        "fuck", "shit", "bitch", "dick", "porn", "sex", "nazi", "hitler", "cunt", "whore"
    )

    private val lookalikes = mapOf(
        'a' to 'а', 'e' to 'е', 'o' to 'о', 'p' to 'р', 'c' to 'с', 'x' to 'х', 'y' to 'у',
        'k' to 'к', 'm' to 'м', 't' to 'т', 'b' to 'в', 'h' to 'н', '0' to 'о', '3' to 'з', '6' to 'б'
    )

    fun clean(raw: String): String = raw.trim().replace(Regex("\\s+"), " ").take(MAX_LENGTH)

    fun isAllowed(raw: String): Boolean {
        val name = clean(raw)
        if (name.isEmpty()) return false
        val plain = name.lowercase().replace('ё', 'е').filter { it.isLetterOrDigit() }
        val cyr = plain.map { lookalikes[it] ?: it }.joinToString("")
        return stopRoots.none { root ->
            val r = root.replace('ё', 'е')
            plain.contains(r) || cyr.contains(r) || cyr.contains(r.map { lookalikes[it] ?: it }.joinToString(""))
        }
    }
}

/**
 * Упаковка «сырых» данных хранилища (ключ → значение) в JSON для облачной копии и обратно.
 * Поддерживаются типы Preferences DataStore: Int, Long, Boolean, Float, Double, String, Set<String>.
 */
object SaveCodec {
    const val VERSION = 1
    const val XP_KEY = "experience"

    fun encode(values: Map<String, Any>): String {
        val items = values.entries.sortedBy { it.key }.mapNotNull { (k, v) ->
            val typed: kotlinx.serialization.json.JsonElement = when (v) {
                is Int -> obj("i", kotlinx.serialization.json.JsonPrimitive(v))
                is Long -> obj("l", kotlinx.serialization.json.JsonPrimitive(v))
                is Boolean -> obj("b", kotlinx.serialization.json.JsonPrimitive(v))
                is Float -> obj("f", kotlinx.serialization.json.JsonPrimitive(v))
                is Double -> obj("d", kotlinx.serialization.json.JsonPrimitive(v))
                is String -> obj("s", kotlinx.serialization.json.JsonPrimitive(v))
                is Set<*> -> obj(
                    "ss",
                    kotlinx.serialization.json.JsonArray(v.filterIsInstance<String>().sorted()
                        .map { kotlinx.serialization.json.JsonPrimitive(it) })
                )
                else -> null
            } ?: return@mapNotNull null
            k to typed
        }.toMap()
        val root = kotlinx.serialization.json.JsonObject(
            mapOf(
                "v" to kotlinx.serialization.json.JsonPrimitive(VERSION),
                "data" to kotlinx.serialization.json.JsonObject(items)
            )
        )
        return root.toString()
    }

    fun decode(raw: String): Map<String, Any>? = try {
        val root = kotlinx.serialization.json.Json.parseToJsonElement(raw) as kotlinx.serialization.json.JsonObject
        val data = root["data"] as kotlinx.serialization.json.JsonObject
        data.mapNotNull { (k, el) ->
            val o = el as? kotlinx.serialization.json.JsonObject ?: return@mapNotNull null
            val t = (o["t"] as? kotlinx.serialization.json.JsonPrimitive)?.content ?: return@mapNotNull null
            val value = o["v"] ?: return@mapNotNull null
            val parsed: Any? = when (t) {
                "i" -> (value as kotlinx.serialization.json.JsonPrimitive).content.toIntOrNull()
                "l" -> (value as kotlinx.serialization.json.JsonPrimitive).content.toLongOrNull()
                "b" -> (value as kotlinx.serialization.json.JsonPrimitive).content.toBooleanStrictOrNull()
                "f" -> (value as kotlinx.serialization.json.JsonPrimitive).content.toFloatOrNull()
                "d" -> (value as kotlinx.serialization.json.JsonPrimitive).content.toDoubleOrNull()
                "s" -> (value as kotlinx.serialization.json.JsonPrimitive).content
                "ss" -> (value as kotlinx.serialization.json.JsonArray)
                    .map { (it as kotlinx.serialization.json.JsonPrimitive).content }.toSet()
                else -> null
            }
            parsed?.let { k to it }
        }.toMap()
    } catch (e: Exception) {
        null
    }

    /** Суммарный XP в снимке — по нему решаются конфликты (§2.2). */
    fun totalXp(values: Map<String, Any>): Int = (values[XP_KEY] as? Int) ?: 0

    private fun obj(type: String, value: kotlinx.serialization.json.JsonElement) =
        kotlinx.serialization.json.JsonObject(mapOf("t" to kotlinx.serialization.json.JsonPrimitive(type), "v" to value))
}
