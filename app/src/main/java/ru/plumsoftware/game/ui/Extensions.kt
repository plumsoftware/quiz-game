package ru.plumsoftware.game.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

fun Modifier.extendOutsideParent(
    start: Dp = 0.dp,
    end: Dp = 0.dp,
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
    isRtl: Boolean = false
) = this.layout { measurable, constraints ->

    val newConstraints = constraints.copy(
        maxWidth = constraints.maxWidth + start.roundToPx() + end.roundToPx(),
        maxHeight = constraints.maxHeight + top.roundToPx() + bottom.roundToPx()
    )

    val placeable = measurable.measure(newConstraints)

    val xOffset = if (isRtl) {
        ((start - end) / 2).roundToPx()
    } else {
        ((end - start) / 2).roundToPx()
    }

    val yOffset = ((bottom - top) / 2).roundToPx()
    layout(placeable.width, placeable.height) {
        placeable.place(xOffset, yOffset)
    }
}

fun isBetween(startDateStr: String, endDateStr: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

    val startDate = LocalDateTime.parse(startDateStr, formatter)
    val endDate = LocalDateTime.parse(endDateStr, formatter)

    val now = LocalDateTime.now()

    val isBetween = now.isAfter(startDate) && now.isBefore(endDate) || now == startDate || now == endDate

    return isBetween
}

fun formatDuration(duration: Duration): String {
    val days = duration.toDays()
    val hours = duration.toHours() % 24
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60

    return buildString {
        if (days > 0) {
            if (days < 20) {
                when (days) {
                    1L -> {
                        append("$days день ")
                    }
                    in 2L..4L -> {
                        append("$days дня ")
                    }
                    else -> {
                        append("$days дней ")
                    }
                }
            } else {
                when {
                    days % 10 == 1L -> {
                        append("$days день ")
                    }
                    days % 10 in 2L..9L -> {
                        append("$days дня ")
                    }
                    else -> {
                        append("$days дней ")
                    }
                }
            }
        }
        if (hours > 0) {
            if (hours < 10) {
                append("0$hours")
            } else {
                append("$hours")
            }
        }
        if (minutes > 0) {
            if (minutes < 10) {
                append(":0$minutes")
            } else {
                append(":$minutes")
            }
        }
        if (seconds > 0) {
            if (seconds < 10) {
                append(":0$seconds ")
            } else {
                append(":$seconds ")
            }
        }
    }.trim()
}