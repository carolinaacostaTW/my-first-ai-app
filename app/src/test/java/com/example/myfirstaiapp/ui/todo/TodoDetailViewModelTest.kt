package com.example.myfirstaiapp.ui.todo

import com.example.myfirstaiapp.NEW_TODO_ID
import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.data.todo.TodoRepository
import io.mockk.MockKAnnotations
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
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
  @MockK
  private lateinit var todoRepository: TodoRepository

  private val dispatcher = StandardTestDispatcher()

  private lateinit var sut: TodoDetailViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(dispatcher)
    MockKAnnotations.init(this)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `when a new todo is edit then it should save it correctly`() = runTest(dispatcher) {
    // Given
    sut = TodoDetailViewModel(
      todoId = NEW_TODO_ID,
      todoRepository = todoRepository,
    )

    coJustRun {
      todoRepository.insert(any())
    }

    sut.onTitleChange("Edit title")
    sut.onDescriptionChange("Edit description")

    // When
    sut.save()
    advanceUntilIdle()

    //Then
    coVerify(exactly = 1) {
      todoRepository.insert(Todo(title = "Edit title", description = "Edit description", isDone = false))
    }
  }

  @Test
  fun `when is edit with blank row then it should not save it`() = runTest(dispatcher) {
    // Given
    coJustRun {
      todoRepository.insert(any())
    }

    sut = TodoDetailViewModel(
      todoId = NEW_TODO_ID,
      todoRepository = todoRepository,
    )

    sut.onTitleChange("   ")
    sut.onDescriptionChange("No title here")

    // When
    sut.save()
    advanceUntilIdle()

    // Then
    coVerify(exactly = 0) { todoRepository.insert(any()) }
  }

  @Test
  fun `when saved more than once then it should save once`() = runTest(dispatcher) {
    // Given
    coJustRun {
      todoRepository.insert(any())
    }

    sut = TodoDetailViewModel(
      todoId = NEW_TODO_ID,
      todoRepository = todoRepository,
    )

    sut.onTitleChange("Walk the dog")

    // When
    sut.save()
    sut.save()
    advanceUntilIdle()

    // Then
    coVerify(exactly = 1) { todoRepository.insert(any()) }
  }

  @Test
  fun `when the todo exists then seeds response from database`() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    every { todoRepository.observeById(42) } returns flowOf(existing)
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = todoRepository)

    advanceUntilIdle()
    assertEquals("Original title", viewModel.title.value)
    assertEquals("Original desc", viewModel.description.value)
  }

  @Test
  fun `when saving an existing todo then call update instead of insert`() = runTest(dispatcher) {
    // Given
    val existing = Todo(id = 42, title = "Original title", description = "Original desc")
    every { todoRepository.observeById(42) } returns flowOf(existing)
    coJustRun {
      todoRepository.update(any())
    }
    sut = TodoDetailViewModel(todoId = 42, todoRepository = todoRepository)

    advanceUntilIdle()
    sut.onTitleChange("Updated title")
    sut.onDescriptionChange("  Updated desc ")

    // When
    sut.save()

    advanceUntilIdle()

    // Then
    coVerify(exactly = 1) {
      todoRepository.update(Todo(id = 42, title = "Updated title", description = "Updated desc"))
    }
    coVerify(exactly = 0) { todoRepository.insert(any()) }
  }

  @Test
  fun `when saving then the complete state survives`() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    every { todoRepository.observeById(42) } returns flowOf(existing)
    coJustRun {
      todoRepository.update(any())
    }
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = todoRepository)

    advanceUntilIdle()
    viewModel.onTitleChange("Updated title")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) { todoRepository.update(existing.copy(title = "Updated title")) }
  }

  @Test
  fun `save beforeSeedLands still Updates Existing Row`() = runTest(dispatcher) {
    val existing = Todo(id = 42, title = "Original title", description = "Original desc", isDone = true)
    every { todoRepository.observeById(42) } returns flowOf(existing)
    coJustRun {
      todoRepository.update(any())
    }
    val viewModel = TodoDetailViewModel(todoId = 42, todoRepository = todoRepository)

    viewModel.onTitleChange("Typed immediately")
    viewModel.save()

    advanceUntilIdle()
    coVerify(exactly = 1) { todoRepository.update(existing.copy(title = "Typed immediately")) }
    coVerify(exactly = 0) { todoRepository.insert(any()) }
  }
}