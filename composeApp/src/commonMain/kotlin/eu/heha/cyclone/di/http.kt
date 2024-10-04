package eu.heha.cyclone.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO


expect fun httpClientFactory(): HttpClient

fun defaultHttpFactory() = HttpClient(CIO)