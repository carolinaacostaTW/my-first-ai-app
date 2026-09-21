package com.example.myfirstaiapp.data.todo

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
  val todos: Flow<List<Todo>>

  fun observeById(id: Long): Flow<Todo?>

  suspend fun insert(todo: Todo): Long

  suspend fun update(todo: Todo)

  suspend fun setDone(id: Long, isDone: Boolean)

  suspend fun deleteById(id: Long)
}

class DefaultTodoRepository @Inject constructor(private val todoDao: TodoDao) : TodoRepository {
  override val todos: Flow<List<Todo>> = todoDao.getTodos()

  override fun observeById(id: Long): Flow<Todo?> = todoDao.observeById(id)

  override suspend fun insert(todo: Todo): Long = todoDao.insert(todo)

  override suspend fun update(todo: Todo) = todoDao.update(todo)

  override suspend fun setDone(id: Long, isDone: Boolean) = todoDao.setDone(id, isDone)

  override suspend fun deleteById(id: Long) = todoDao.deleteById(id)
}