package com.knowledgepearls.app.di

import android.content.Context
import androidx.room.Room
import com.knowledgepearls.app.data.local.MedPearlsDatabase
import com.knowledgepearls.app.data.local.dao.FolderDao
import com.knowledgepearls.app.data.local.dao.KnowledgePearlDao
import com.knowledgepearls.app.data.local.dao.PearlMediaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MedPearlsDatabase =
        Room.databaseBuilder(
            context,
            MedPearlsDatabase::class.java,
            MedPearlsDatabase.NAME,
        ).build()

    @Provides
    fun provideKnowledgePearlDao(database: MedPearlsDatabase): KnowledgePearlDao =
        database.knowledgePearlDao()

    @Provides
    fun providePearlMediaDao(database: MedPearlsDatabase): PearlMediaDao =
        database.pearlMediaDao()

    @Provides
    fun provideFolderDao(database: MedPearlsDatabase): FolderDao =
        database.folderDao()
}
