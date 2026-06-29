package com.kidstamil.di

import com.kidstamil.data.repository.ChallengeRepositoryImpl
import com.kidstamil.data.repository.GameRepositoryImpl
import com.kidstamil.data.repository.PreferencesRepositoryImpl
import com.kidstamil.data.repository.ProgressionRepositoryImpl
import com.kidstamil.domain.repository.ChallengeRepository
import com.kidstamil.domain.repository.GameRepository
import com.kidstamil.domain.repository.PreferencesRepository
import com.kidstamil.domain.repository.ProgressionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository
    @Binds @Singleton abstract fun bindChallengeRepository(impl: ChallengeRepositoryImpl): ChallengeRepository
    @Binds @Singleton abstract fun bindProgressionRepository(impl: ProgressionRepositoryImpl): ProgressionRepository
    @Binds @Singleton abstract fun bindPreferencesRepository(impl: PreferencesRepositoryImpl): PreferencesRepository
}
