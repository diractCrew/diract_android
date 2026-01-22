package com.baek.diract.di

import com.baek.diract.data.datasource.remote.TeamspaceRemoteDataSource
import com.baek.diract.data.repository.TeamspaceRepositoryImpl
import com.baek.diract.domain.repository.TeamspaceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TeamspaceModule {

    @Binds
    @Singleton
    abstract fun bindTeamspaceRepository(
        impl: TeamspaceRepositoryImpl
    ): TeamspaceRepository



    // @Binds
    // @Singleton
    // abstract fun bindTeamspaceRemoteDataSource(
    //     impl: TeamspaceRemoteDataSourceImpl
    // ): TeamspaceRemoteDataSource
}
