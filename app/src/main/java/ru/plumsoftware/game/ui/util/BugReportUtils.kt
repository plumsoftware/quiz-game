package ru.plumsoftware.game.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

private const val BUG_REPORT_EMAIL = "Plumsoftware@yandex.ru"
private const val BUG_REPORT_SUBJECT = "Баг в приложении Викторина"

fun openBugReportEmail(context: Context): Boolean {
    val subject = Uri.encode(BUG_REPORT_SUBJECT)
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:$BUG_REPORT_EMAIL?subject=$subject")
    }
    return try {
        context.startActivity(
            Intent.createChooser(intent, "Выберите приложение")
        )
        true
    } catch (_: ActivityNotFoundException) {
        false
    }
}
