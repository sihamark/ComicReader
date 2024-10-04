package eu.heha.cyclone.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun httpClientFactory() = HttpClient(Darwin)