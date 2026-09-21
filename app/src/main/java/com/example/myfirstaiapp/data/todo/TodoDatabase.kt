package com.example.myfirstaiapp.data.todo

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Todo::class], version = 1, exportSchema = true)
abstract class TodoDatabase : RoomDatabase() {
  abstract fun todoDao(): TodoDao
}