package dev.frederikpietzko.example

import kotlinx.serialization.Serializable

@Serializable
data class State(
    val initialMessage: String
)