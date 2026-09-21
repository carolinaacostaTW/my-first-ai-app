package com.example.myfirstaiapp.ui.todo

import com.example.myfirstaiapp.NEW_TODO_ID
import com.example.myfirstaiapp.data.todo.Todo
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val repository = FakeTodoRepository()
    val viewModel = TodoDetailViewModel(todoId = NEW_TODO_ID, todoRepository = repository)

    viewModel.onTitleChange("Buy milk")
    viewModel.onDescriptionChange("A carton  ")
    viewModel.save()

    advanceUntilIdle()
    assertEquals(
      listOf(Todo(title = "Buy milk", description = "A carton", isDone = false)),
      repository.inserted,
    )
  }

  @Test
  fun save_blankTitle_discardsRow() = runTest(dispatcher) {
    val repository = FakeTodoRepository()
    val viewModel = TodoDetailViewModel(todoId = NEW_TODO_ID, todoRepository = repository)

    viewModel.onTitleChange("   ")
    viewModel.onDescriptionChange("No title here")
    viewModel.save()

    advanceUntilIdle()
    assertEquals(emptyList<Todo>(), repository.inserted)
  }

  @Test
  fun save_calledTwice_insertsOnce() = runTest(dispatcher) {
    val repository = FakeTodoRepository()
    val viewModel = TodoDetailViewModel(todoId = NEW_TODO_ID, todoRepository = repository)

    viewModel.onTitleChange("Walk the dog")
    viewModel.save()
    viewModel.save()

    advanceUntilIdle()
    assertEquals(1, repository.inserted.size)
  }

  @Test
  fun existingTodo_seedsFieldsFromDatabase() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    val repository = FakeTodoRepository(seed = listOf(existing))
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    advanceUntilIdle()
    assertEquals("Original title", viewModel.title.value)
    assertEquals("Original desc", viewModel.description.value)
  }

  @Test
  fun save_existingTodo_updatesRowInsteadOfInserting() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc")
    val repository = FakeTodoRepository(seed = listOf(existing))
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    advanceUntilIdle()
    viewModel.onTitleChange("Updated title")
    viewModel.onDescriptionChange("  Updated desc ")
    viewModel.save()

    advanceUntilIdle()
    assertEquals(
      listOf(Todo(id = 42, title = "Updated title", description = "Updated desc")),
      repository.updated,
    )
    assertEquals(emptyList<Todo>(), repository.inserted)
  }

  @Test
  fun save_existingTodo_preservesDoneState() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    val repository = FakeTodoRepository(seed = listOf(existing))
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    advanceUntilIdle()
    viewModel.onTitleChange("Updated title")
    viewModel.save()

    advanceUntilIdle()
    assertEquals(listOf(existing.copy(title = "Updated title")), repository.updated)
  }

  @Test
  fun save_beforeSeedLands_stillUpdatesExistingRow() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    val repository = FakeTodoRepository(seed = listOf(existing))
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = repository)

    viewModel.onTitleChange("Typed immediately")
    viewModel.save()

    advanceUntilIdle()
    assertEquals(listOf(existing.copy(title = "Typed immediately")), repository.updated)
    assertEquals(emptyList<Todo>(), repository.inserted)
  }
}