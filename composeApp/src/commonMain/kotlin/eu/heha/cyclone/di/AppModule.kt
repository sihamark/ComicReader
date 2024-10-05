package eu.heha.cyclone.di

import app.cash.sqldelight.db.SqlDriver
import coil3.ImageLoader
import coil3.PlatformContext
import eu.heha.cyclone.model.ComicRepository
import eu.heha.cyclone.model.DatabaseSource
import eu.heha.cyclone.model.ReaderController
import eu.heha.cyclone.ui.AddComicViewModel
import eu.heha.cyclone.ui.ComicReaderViewModel
import eu.heha.cyclone.ui.ComicsViewModel
import io.ktor.client.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun appModule(
    platformContext: PlatformContext,
    httpClientFactory: () -> HttpClient,
    sqlDriverFactory: (databaseName: String) -> SqlDriver
): Module = module {
    single { platformContext }
    single { httpClientFactory() }
    single { sqlDriverFactory("comics.db") }
    single { DatabaseSource(get()) }
    singleOf(::ImageLoader)
    singleOf(::ComicRepository)
    factoryOf(::ReaderController)
    viewModelOf(::AddComicViewModel)
    viewModelOf(::ComicReaderViewModel)
    viewModelOf(::ComicsViewModel)
}