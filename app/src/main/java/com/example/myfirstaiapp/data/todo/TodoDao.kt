package com.example.myfirstaiapp.data.todo

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
  @Query("SELECT * FROM todos ORDER BY id ASC")
  fun getTodos(): Flow<List<Todo>>

  @Query("SELECT * FROM todos WHERE id = :id")
  fun observeById(id: Long): Flow<Todo?>

  @Insert
  suspend fun insert(todo: Todo): Long

  @Update
  suspend fun update(todo: Todo)

  @Query("UPDATE todos SET isDone = :isDone WHERE id = :id")
  suspend fun setDone(id: Long, isDone: Boolean)

  @Query("DELETE FROM todos WHERE id = :id")
  suspend fun deleteById(id: Long)
}