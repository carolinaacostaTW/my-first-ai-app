package com.example.myfirstaiapp.ui.todo

import com.example.myfirstaiapp.NEW_TODO_ID
import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.data.todo.TodoRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoDetailViewModelTest {
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
  fun save_insertsNewTodo() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    val viewModel = TodoDetailViewModel(todoId = NEW_TODO_ID, todoRepository = repository)

    viewModel.onTitleChange("Buy milk")
    viewModel.onDescriptionChange("A carton  ")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) {
      repository.insert(Todo(title = "Buy milk", description = "A carton", isDone = false))
    }
  }

  @Test
  fun save_blankTitle_discardsRow() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    val viewModel = TodoDetailViewModel(todoId = NEW_TODO_ID, todoRepository = repository)

    viewModel.onTitleChange("   ")
    viewModel.onDescriptionChange("No title here")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 0) { repository.insert(any()) }
  }

  @Test
  fun save_calledTwice_insertsOnce() = runTest(dispatcher) {
    val repository = mockk<TodoRepository>(relaxed = true)
    val viewModel = TodoDetailViewModel(todoId = NEW_TODO_ID, todoRepository = repository)

    viewModel.onTitleChange("Walk the dog")
    viewModel.save()
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) { repository.insert(any()) }
  }

  @Test
  fun existingTodo_seedsFieldsFromDatabase() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.observeById(42) } returns flowOf(existing)
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    advanceUntilIdle()
    assertEquals("Original title", viewModel.title.value)
    assertEquals("Original desc", viewModel.description.value)
  }

  @Test
  fun save_existingTodo_updatesRowInsteadOfInserting() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc")
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.observeById(42) } returns flowOf(existing)
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    advanceUntilIdle()
    viewModel.onTitleChange("Updated title")
    viewModel.onDescriptionChange("  Updated desc ")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) {
      repository.update(Todo(id = 42, title = "Updated title", description = "Updated desc"))
    }
    coVerify(exactly = 0) { repository.insert(any()) }
  }

  @Test
  fun save_existingTodo_preservesDoneState() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.observeById(42) } returns flowOf(existing)
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    advanceUntilIdle()
    viewModel.onTitleChange("Updated title")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) { repository.update(existing.copy(title = "Updated title")) }
  }

  @Test
  fun save_beforeSeedLands_stillUpdatesExistingRow() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    val repository = mockk<TodoRepository>(relaxed = true)
    every { repository.observeById(42) } returns flowOf(existing)
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    viewModel.onTitleChange("Typed immediately")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) { repository.update(existing.copy(title = "Typed immediately")) }
    coVerify(exactly = 0) { repository.insert(any()) }
  }
}