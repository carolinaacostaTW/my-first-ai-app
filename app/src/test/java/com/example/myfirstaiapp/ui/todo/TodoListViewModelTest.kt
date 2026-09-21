package com.example.myfirstaiapp.ui.todo

import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.data.todo.TodoRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun uiState_initiallyLoading() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.todos } returns flowOf(emptyList())
    val viewModel = TodoListViewModel(repository)
    assertEquals(TodoListUiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun uiState_withNoTodos_isEmpty() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.todos } returns flowOf(emptyList())
    val viewModel = TodoListViewModel(repository)
    val job = launch { viewModel.uiState.collect { } }
    advanceUntilIdle()
    assertEquals(TodoListUiState.Empty, viewModel.uiState.value)
    job.cancel()
  }

  @Test
  fun uiState_withTodos_isSuccess() = runTest(dispatcher) {
    val todos = listOf(Todo(id = 1, title = "Buy milk", description = "A carton"), Todo(id = 2, title = "Walk the dog"))
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.todos } returns flowOf(todos)
    val viewModel = TodoListViewModel(repository)
    val job = launch { viewModel.uiState.collect { } }
    advanceUntilIdle()
    assertEquals(TodoListUiState.Success(todos), viewModel.uiState.value)
    job.cancel()
  }

  @Test
  fun success_progressReflectsCompletedTodos() {
    val todos =
      listOf(
        Todo(id = 1, title = "Buy milk", isDone = true),
        Todo(id = 2, title = "Walk the dog", isDone = false),
        Todo(id = 3, title = "Water plants", isDone = true),
      )
    val state = TodoListUiState.Success(todos)
    assertEquals(2, state.completedCount)
    assertEquals(3, state.totalCount)
    assertEquals(2f / 3f, state.progress)
  }

  @Test
  fun success_withoutTodos_hasNoProgress() {
    val state = TodoListUiState.Success(emptyList())
    assertEquals(0, state.completedCount)
    assertEquals(0, state.totalCount)
    assertEquals(0f, state.progress)
  }

  @Test
  fun uiState_repositoryError_isError() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.todos } returns flow { throw RuntimeException("db down") }
    val viewModel = TodoListViewModel(repository)
    val job = launch { viewModel.uiState.collect { } }
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value is TodoListUiState.Error)
    job.cancel()
  }

  @Test
  fun setDone_propagatesToRepository() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.todos } returns flowOf(emptyList())
    val viewModel = TodoListViewModel(repository)
    viewModel.setDone(7, true)
    advanceUntilIdle()
    coVerify(exactly = 1) { repository.setDone(7, true) }
  }

  @Test
  fun deleteById_propagatesToRepository() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.todos } returns flowOf(emptyList())
    val viewModel = TodoListViewModel(repository)
    viewModel.deleteById(7)
    advanceUntilIdle()
    coVerify(exactly = 1) { repository.deleteById(7) }
  }
}