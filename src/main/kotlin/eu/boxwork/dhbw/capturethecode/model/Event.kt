package eu.boxwork.dhbw.capturethecode.model

data class Event(
    val round : Int,
    val source: String?,
    val target: String?,
    val event: String,
)
