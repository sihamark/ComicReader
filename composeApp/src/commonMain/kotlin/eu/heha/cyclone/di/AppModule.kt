package eu.heha.cyclone.di

import coil3.ImageLoader
import coil3.PlatformContext
import eu.heha.cyclone.model.ComicRepository
import eu.heha.cyclone.model.ReaderController
import eu.heha.cyclone.ui.AddComicViewModel
import eu.heha.cyclone.ui.ComicReaderViewModel
import eu.heha.cyclone.ui.ComicsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun koinModule(platformContext: PlatformContext): Module = module {
    single<PlatformContext> { platformContext }
    single { httpClientFactory() }
    singleOf(::ImageLoader)
    singleOf(::ComicRepository)
    factoryOf(::ReaderController)
    viewModelOf(::AddComicViewModel)
    viewModelOf(::ComicReaderViewModel)
    viewModelOf(::ComicsViewModel)
}