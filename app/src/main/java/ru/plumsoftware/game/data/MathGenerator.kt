package ru.plumsoftware.game.data

import kotlin.random.Random

/**
 * Генератор вопросов для темы «Математика» — уровни бесконечно разнообразны,
 * поэтому JSON для этой темы не нужен (но если положить `math_N.json`, он будет использован).
 * Сложность растёт вместе с номером уровня внутри каждой из трёх сложностей.
 */
object MathGenerator {
    const val TOPIC = "math"

    fun generate(difficulty: Int, level: Int, count: Int, random: Random = Random.Default): List<QuizQuestion> {
        val result = ArrayList<QuizQuestion>(count)
        val texts = HashSet<String>()
        var guard = 0
        while (result.size < count && guard < count * 20) {
            guard++
            val q = one(difficulty, level, random, result.size)
            if (texts.add(q.text)) result += q
        }
        return result
    }

    private fun band(level: Int): Int = when {
        level <= 10 -> 0
        level <= 20 -> 1
        else -> 2
    }

    private fun one(d: Int, level: Int, r: Random, index: Int): QuizQuestion {
        val b = band(level)
        val gen: () -> Triple<String, Int, String> = when (d) {
            0 -> when (b) {
                0 -> pick(r, { add(r, 1, 5) }, { count(r) }, { next(r, 1, 9) })
                1 -> pick(r, { add(r, 2, 10) }, { sub(r, 20) }, { between(r, 20) }, { word(r, 10) })
                else -> pick(r, { missing(r, 20) }, { chain(r, 10) }, { sub(r, 20) }, { word(r, 20) })
            }
            1 -> when (b) {
                0 -> pick(r, { add(r, 10, 50) }, { sub(r, 100) }, { mul(r, 2, 5) })
                1 -> pick(r, { mul(r, 2, 9) }, { div(r, 2, 9) }, { add(r, 20, 60) })
                else -> pick(r, { order(r) }, { mulWord(r) }, { mul(r, 3, 9) }, { div(r, 3, 9) })
            }
            else -> when (b) {
                0 -> pick(r, { mul2(r) }, { div2(r) }, { square(r) })
                1 -> pick(r, { equation(r) }, { percent(r) }, { mul2(r) })
                else -> pick(r, { brackets(r) }, { fraction(r) }, { rectangle(r) }, { equation(r) })
            }
        }
        val (text, answer, explanation) = gen()
        val options = distractors(answer, r)
        return QuizQuestion(
            id = "math_${d}_${level}_${index}_${text.hashCode()}",
            topic = TOPIC,
            difficulty = d,
            level = level,
            text = text,
            image = null,
            options = options.map { it.toString() },
            answer = options.indexOf(answer),
            explanation = explanation
        )
    }

    private fun pick(r: Random, vararg gens: () -> Triple<String, Int, String>): () -> Triple<String, Int, String> =
        gens[r.nextInt(gens.size)]

    private fun distractors(answer: Int, r: Random): List<Int> {
        val set = linkedSetOf(answer)
        val deltas = listOf(1, -1, 2, -2, 10, -10, 3, -3, 5, -5).shuffled(r)
        for (delta in deltas) {
            if (set.size >= 4) break
            val v = answer + delta
            if (v >= 0) set += v
        }
        var extra = 4
        while (set.size < 4) set += answer + extra++
        return set.toList().shuffled(r)
    }

    // ---- 5–7 лет ----
    private fun add(r: Random, lo: Int, hi: Int): Triple<String, Int, String> {
        val a = r.nextInt(lo, hi + 1); val b = r.nextInt(lo, hi + 1)
        return Triple("Сколько будет $a + $b?", a + b, "$a + $b = ${a + b}")
    }

    private fun sub(r: Random, max: Int): Triple<String, Int, String> {
        val a = r.nextInt(max / 2, max + 1); val b = r.nextInt(1, a + 1)
        return Triple("Сколько будет $a − $b?", a - b, "$a − $b = ${a - b}")
    }

    private val things = listOf("🍎", "⭐", "🐟", "🌸", "🎈", "🐞")
    private fun count(r: Random): Triple<String, Int, String> {
        val n = r.nextInt(2, 10); val t = things[r.nextInt(things.size)]
        return Triple("Сколько здесь предметов? ${t.repeat(n)}", n, "Посчитай по одному: всего $n.")
    }

    private fun next(r: Random, lo: Int, hi: Int): Triple<String, Int, String> {
        val n = r.nextInt(lo, hi + 1)
        return Triple("Какое число идёт после $n?", n + 1, "После $n идёт ${n + 1}.")
    }

    private fun between(r: Random, max: Int): Triple<String, Int, String> {
        val a = r.nextInt(1, max - 1)
        return Triple("Какое число стоит между $a и ${a + 2}?", a + 1, "$a, ${a + 1}, ${a + 2}")
    }

    private val names = listOf("Маши", "Пети", "Оли", "Вани", "Кати", "Миши")
    private fun word(r: Random, max: Int): Triple<String, Int, String> {
        val name = names[r.nextInt(names.size)]
        val t = things[r.nextInt(things.size)]
        val a = r.nextInt(1, max / 2 + 1); val b = r.nextInt(1, max / 2 + 1)
        return if (r.nextBoolean()) {
            Triple("У $name было $a $t, потом дали ещё $b $t. Сколько $t стало?", a + b, "$a + $b = ${a + b}")
        } else {
            val total = a + b
            Triple("У $name было $total $t, $b $t отдали. Сколько $t осталось?", a, "$total − $b = $a")
        }
    }

    private fun missing(r: Random, max: Int): Triple<String, Int, String> {
        val a = r.nextInt(1, max / 2); val x = r.nextInt(1, max / 2)
        return Triple("$a + ? = ${a + x}. Какое число пропущено?", x, "${a + x} − $a = $x")
    }

    private fun chain(r: Random, max: Int): Triple<String, Int, String> {
        val a = r.nextInt(2, max); val b = r.nextInt(1, max); val c = r.nextInt(1, a + b)
        return Triple("Сколько будет $a + $b − $c?", a + b - c, "$a + $b = ${a + b}, ${a + b} − $c = ${a + b - c}")
    }

    // ---- 8–10 лет ----
    private fun mul(r: Random, lo: Int, hi: Int): Triple<String, Int, String> {
        val a = r.nextInt(lo, hi + 1); val b = r.nextInt(2, 10)
        return Triple("Сколько будет $a × $b?", a * b, "$a × $b = ${a * b}")
    }

    private fun div(r: Random, lo: Int, hi: Int): Triple<String, Int, String> {
        val a = r.nextInt(lo, hi + 1); val b = r.nextInt(2, 10)
        return Triple("Сколько будет ${a * b} : $b?", a, "${a * b} : $b = $a, потому что $a × $b = ${a * b}")
    }

    private fun order(r: Random): Triple<String, Int, String> {
        val a = r.nextInt(2, 20); val b = r.nextInt(2, 10); val c = r.nextInt(2, 10)
        return Triple("Сколько будет $a + $b × $c?", a + b * c, "Сначала умножение: $b × $c = ${b * c}, потом $a + ${b * c} = ${a + b * c}")
    }

    private fun mulWord(r: Random): Triple<String, Int, String> {
        val rows = r.nextInt(3, 10); val per = r.nextInt(2, 10)
        return Triple("В коробке $rows рядов по $per конфет. Сколько всего конфет?", rows * per, "$rows × $per = ${rows * per}")
    }

    // ---- 11+ ----
    private fun mul2(r: Random): Triple<String, Int, String> {
        val a = r.nextInt(12, 99); val b = r.nextInt(3, 10)
        return Triple("Сколько будет $a × $b?", a * b, "$a × $b = ${a * b}")
    }

    private fun div2(r: Random): Triple<String, Int, String> {
        val a = r.nextInt(12, 60); val b = r.nextInt(3, 10)
        return Triple("Сколько будет ${a * b} : $b?", a, "${a * b} : $b = $a")
    }

    private fun square(r: Random): Triple<String, Int, String> {
        val a = r.nextInt(4, 16)
        return Triple("Сколько будет $a²?", a * a, "$a² = $a × $a = ${a * a}")
    }

    private fun equation(r: Random): Triple<String, Int, String> {
        return if (r.nextBoolean()) {
            val x = r.nextInt(5, 60); val a = r.nextInt(5, 50)
            Triple("Реши уравнение: x + $a = ${x + a}", x, "x = ${x + a} − $a = $x")
        } else {
            val x = r.nextInt(2, 13); val a = r.nextInt(2, 10)
            Triple("Реши уравнение: $a · x = ${a * x}", x, "x = ${a * x} : $a = $x")
        }
    }

    private fun percent(r: Random): Triple<String, Int, String> {
        val p = listOf(10, 20, 25, 50).random(r)
        val base = if (p == 25) r.nextInt(1, 13) * 4 else r.nextInt(1, 21) * 10
        val value = base * p / 100
        return Triple("Сколько будет $p % от $base?", value, "$base × $p : 100 = $value")
    }

    private fun brackets(r: Random): Triple<String, Int, String> {
        val a = r.nextInt(2, 15); val b = r.nextInt(2, 15); val c = r.nextInt(2, 8)
        return Triple("Сколько будет ($a + $b) × $c?", (a + b) * c, "Сначала скобки: $a + $b = ${a + b}, потом × $c = ${(a + b) * c}")
    }

    private fun fraction(r: Random): Triple<String, Int, String> {
        val den = listOf(2, 3, 4, 5).random(r); val k = r.nextInt(2, 13)
        val base = den * k
        return Triple("Сколько будет 1/$den от $base?", k, "$base : $den = $k")
    }

    private fun rectangle(r: Random): Triple<String, Int, String> {
        val w = r.nextInt(2, 13); val h = r.nextInt(2, 13)
        return if (r.nextBoolean()) {
            Triple("Чему равна площадь прямоугольника $w × $h см (в см²)?", w * h, "S = $w × $h = ${w * h}")
        } else {
            Triple("Чему равен периметр прямоугольника $w × $h см?", 2 * (w + h), "P = 2 × ($w + $h) = ${2 * (w + h)}")
        }
    }
}
