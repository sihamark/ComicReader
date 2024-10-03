package eu.heha.cyclone.di

import android.content.Context
import coil3.ImageLoader
import coil3.PlatformContext
import eu.heha.cyclone.model.ComicRepository
import eu.heha.cyclone.model.ReaderController
import eu.heha.cyclone.ui.AddComicViewModel
import eu.heha.cyclone.ui.ComicReaderViewModel
import eu.heha.cyclone.ui.ComicsViewModel
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun koinModule(context: Context): Module = module {
    single<PlatformContext> { context.applicationContext }
    single { HttpClient(CIO) }
    singleOf(::ImageLoader)
    singleOf(::ComicRepository)
    factoryOf(::ReaderController)
    viewModelOf(::AddComicViewModel)
    viewModelOf(::ComicReaderViewModel)
    viewModelOf(::ComicsViewModel)
}