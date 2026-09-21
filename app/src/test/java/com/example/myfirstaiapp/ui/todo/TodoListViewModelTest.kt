package com.example.myfirstaiapp.ui.todo

import com.example.myfirstaiapp.data.todo.Todo
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val viewModel = TodoListViewModel(FakeTodoRepository())
    assertEquals(TodoListUiState.Loading, viewModel.uiState.value)
  }

  @Test
  fun uiState_withNoTodos_isEmpty() = runTest(dispatcher) {
    val viewModel = TodoListViewModel(FakeTodoRepository(seed = emptyList()))
    val job = launch { viewModel.uiState.collect { } }
    advanceUntilIdle()
    assertEquals(TodoListUiState.Empty, viewModel.uiState.value)
    job.cancel()
  }

  @Test
  fun uiState_withTodos_isSuccess() = runTest(dispatcher) {
    val todos = listOf(Todo(id = 1, title = "Buy milk", description = "A carton"), Todo(id = 2, title = "Walk the dog"))
    val viewModel = TodoListViewModel(FakeTodoRepository(seed = todos))
    val job = launch { viewModel.uiState.collect { } }
    advanceUntilIdle()
    assertEquals(TodoListUiState.Success(todos), viewModel.uiState.value)
    job.cancel()
  }

  @Test
  fun uiState_repositoryError_isError() = runTest(dispatcher) {
    val viewModel = TodoListViewModel(FakeTodoRepository(error = RuntimeException("db down")))
    val job = launch { viewModel.uiState.collect { } }
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value is TodoListUiState.Error)
    job.cancel()
  }

  @Test
  fun setDone_propagatesToRepository() = runTest(dispatcher) {
    val repository = FakeTodoRepository(seed = listOf(Todo(id = 7, title = "Buy milk")))
    val viewModel = TodoListViewModel(repository)
    viewModel.setDone(7, true)
    advanceUntilIdle()
    assertEquals(listOf(7L to true), repository.setDoneCalls)
  }

  @Test
  fun deleteById_propagatesToRepository() = runTest(dispatcher) {
    val repository = FakeTodoRepository(seed = listOf(Todo(id = 7, title = "Buy milk")))
    val viewModel = TodoListViewModel(repository)
    viewModel.deleteById(7)
    advanceUntilIdle()
    assertEquals(listOf(7L), repository.deletedById)
  }
}