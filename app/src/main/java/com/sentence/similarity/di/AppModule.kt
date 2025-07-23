package com.sentence.similarity.di

import android.content.Context
import com.sentence.similarity.proto.ProtoVectorStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideProtoVectorStorage(
        @ApplicationContext context: Context
    ): ProtoVectorStorage = ProtoVectorStorage(context)
}
