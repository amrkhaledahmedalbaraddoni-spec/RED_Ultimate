package com.red.core.di

import android.content.Context
import androidx.room.Room
import com.red.core.database.RedDatabase
import com.red.core.database.StoryDao
import com.red.core.delivery.MessageDao
import com.red.feature.pstn.PstnDao
import com.red.feature.pstn.PstnDatabase
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
  fun provideDatabase(@ApplicationContext context: Context): RedDatabase =
    Room.databaseBuilder(context, RedDatabase::class.java, "red.db")
      .fallbackToDestructiveMigration()
      .build()

  @Provides
  fun provideMessageDao(db: RedDatabase): MessageDao = db.messageDao()

  @Provides
  fun provideStoryDao(db: RedDatabase): StoryDao = db.storyDao()

  @Provides
  @Singleton
  fun providePstnDatabase(@ApplicationContext context: Context): PstnDatabase =
    Room.databaseBuilder(context, PstnDatabase::class.java, "pstn.db")
      .fallbackToDestructiveMigration()
      .build()

  @Provides
  fun providePstnDao(db: PstnDatabase): PstnDao = db.pstnDao()
}
