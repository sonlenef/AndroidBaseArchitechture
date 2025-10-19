package dev.sonle.androidbasearchitechture.core.di

import androidx.navigation.NavController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.sonle.androidbasearchitechture.domain.navigation.Navigator
import dev.sonle.androidbasearchitechture.presentation.navigation.NavigationComposeNavigator

/**
 * DI module for navigation dependencies
 * 
 * Note: NavController cannot be directly injected into ViewModels as it's tied to the UI lifecycle.
 * Instead, we'll provide Navigator through a different approach in the UI layer.
 */
@Module
@InstallIn(ViewModelComponent::class)
object NavigationModule {
    
    // This module will be used differently in practice
    // Navigator will be provided from the UI layer where NavController is available
}
