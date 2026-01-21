package com.baek.diract.di

import com.baek.diract.data.datasource.remote.VideoRemoteDataSource
import com.baek.diract.data.datasource.remote.VideoRemoteDataSourceImpl
import com.baek.diract.data.repository.GalleryRepositoryImpl
import com.baek.diract.data.repository.VideoRepositoryImpl
import com.baek.diract.domain.repository.GalleryRepository
import com.baek.diract.domain.repository.VideoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VideoModule {

    @Binds
    @Singleton
    abstract fun bindVideoRepository(
        impl: VideoRepositoryImpl
    ): VideoRepository

    @Binds
    @Singleton
    abstract fun bindVideoRemoteDataSource(
        impl: VideoRemoteDataSourceImpl
    ): VideoRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindGalleryRepository(
        impl: GalleryRepositoryImpl
    ): GalleryRepository
}