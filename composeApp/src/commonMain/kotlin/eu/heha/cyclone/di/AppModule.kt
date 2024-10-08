package eu.heha.cyclone.di

import app.cash.sqldelight.db.SqlDriver
import coil3.ImageLoader
import coil3.PlatformContext
import eu.heha.cyclone.model.ComicRepository
import eu.heha.cyclone.model.ReaderController
import eu.heha.cyclone.model.RemoteSource
import eu.heha.cyclone.model.database.DatabaseSource
import eu.heha.cyclone.ui.AddComicViewModel
import eu.heha.cyclone.ui.ComicReaderViewModel
import eu.heha.cyclone.ui.ComicsViewModel
import io.ktor.client.HttpClient
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
    single { sqlDriverFactory("comics.db") }
    singleOf(httpClientFactory)
    singleOf(::DatabaseSource)
    singleOf(::RemoteSource)
    singleOf(::ImageLoader)
    singleOf(::ComicRepository)
    factoryOf(::ReaderController)
    viewModelOf(::AddComicViewModel)
    viewModelOf(::ComicReaderViewModel)
    viewModelOf(::ComicsViewModel)
}