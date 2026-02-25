package dev.sonle.androidbasearchitechture.core.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Module providing CoroutineDispatcher instances
 */
val dispatcherModule = module {
    single<CoroutineDispatcher>(named("IoDispatcher")) { Dispatchers.IO }
    single<CoroutineDispatcher>(named("MainDispatcher")) { Dispatchers.Main }
    single<CoroutineDispatcher>(named("DefaultDispatcher")) { Dispatchers.Default }
}
