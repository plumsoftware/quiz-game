package ru.plumsoftware.game.data.firebase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.plumsoftware.game.data.Quiz

@Serializable
data class RemoteConfigQuizModel(
    @SerialName("card_title") val cardTitle: String,
    @SerialName("quiz") val quiz: Quiz,
)
