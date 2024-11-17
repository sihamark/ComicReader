package eu.heha.cyclone.ui

import kotlinx.serialization.Serializable

@Serializable
object Comics

@Serializable
object ComicAdd

@Serializable
data class Comic(val id: Long)