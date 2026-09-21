package com.example.myfirstaiapp.data.todo

import android.content.Context
import androidx.room.Room
import dagger.Binds
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
  fun provideTodoDatabase(@ApplicationContext context: Context): TodoDatabase =
    Room.databaseBuilder(context, TodoDatabase::class.java, "todos.db").build()

  @Provides
  fun provideTodoDao(database: TodoDatabase): TodoDao = database.todoDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
  @Binds
  abstract fun bindTodoRepository(implementation: DefaultTodoRepository): TodoRepository
}