package com.example.myfirstaiapp.ui.todo

import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.data.todo.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

internal class FakeTodoRepository(
  private val seed: List<Todo> = listOf(Todo(title = "Sample")),
  private val error: Throwable? = null,
) : TodoRepository {
  val setDoneCalls = mutableListOf<Pair<Long, Boolean>>()
  val inserted = mutableListOf<Todo>()
  val updated = mutableListOf<Todo>()

  override val todos: Flow<List<Todo>> =
    flow {
      error?.let { throw it }
      emit(seed)
    }

  override fun observeById(id: Long): Flow<Todo?> = flowOf(seed.firstOrNull { it.id == id })

  override suspend fun insert(todo: Todo): Long {
    inserted += todo
    return inserted.size.toLong()
  }

  override suspend fun update(todo: Todo) {
    updated += todo
  }

  override suspend fun setDone(id: Long, isDone: Boolean) {
    setDoneCalls += id to isDone
  }

  override suspend fun deleteById(id: Long) = Unit
}