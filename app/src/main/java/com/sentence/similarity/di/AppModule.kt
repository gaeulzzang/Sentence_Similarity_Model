package com.sentence.similarity.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEmbeddingManager(
        @ApplicationContext context: Context
    ): EmbeddingManager = EmbeddingManager(context)

    @Singleton
    @Provides
    fun provideProtoVectorStorage(
        @ApplicationContext context: Context
    ): ProtoVectorStorage = ProtoVectorStorage(context)
}
