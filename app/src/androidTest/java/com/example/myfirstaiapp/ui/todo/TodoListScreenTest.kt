package com.example.myfirstaiapp.ui.todo

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasStateDescription
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.myfirstaiapp.R
import com.example.myfirstaiapp.data.todo.Todo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** UI tests for [TodoListScreen]. */
class TodoListScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun list_rendersEveryTodo() {
    composeTestRule.setContent {
      TodoListScreen(uiState = TodoListUiState.Success(TODOS))
    }

    TODOS.forEach { todo ->
      composeTestRule.onNodeWithText(todo.title).assertExists()
      composeTestRule.onNodeWithText(todo.description).assertExists()
    }
  }

  @Test
  fun emptyState_showsMessage() {
    composeTestRule.setContent {
      TodoListScreen(uiState = TodoListUiState.Empty)
    }

    val message = composeTestRule.activity.getString(R.string.todo_list_empty)
    composeTestRule.onNodeWithText(message).assertExists()
  }

  @Test
  fun checkbox_toggleInvokesCallbackWithNewValue() {
    val updated = mutableListOf<Pair<Long, Boolean>>()
    composeTestRule.setContent {
      TodoListScreen(
        uiState = TodoListUiState.Success(listOf(TEST_TODO)),
        onToggleDone = { id, isDone -> updated += id to isDone },
      )
    }

    composeTestRule.onNode(isToggleable()).performClick()

    assertEquals(listOf(TEST_TODO.id to true), updated)
  }

  @Test
  fun completedTodo_showsStrikethrough() {
    composeTestRule.setContent {
      TodoListScreen(uiState = TodoListUiState.Success(listOf(TEST_TODO.copy(isDone = true))))
    }

    val completedStateDescription =
      composeTestRule.activity.getString(R.string.todo_row_completed_state_description)
    composeTestRule.onNodeWithText(TEST_TODO.title)
      .assert(hasAnyAncestor(hasStateDescription(completedStateDescription)))
  }

  @Test
  fun notCompletedTodo_showsNoStrikethrough() {
    composeTestRule.setContent {
      TodoListScreen(uiState = TodoListUiState.Success(listOf(TEST_TODO)))
    }

    val completedStateDescription =
      composeTestRule.activity.getString(R.string.todo_row_completed_state_description)
    composeTestRule.onNodeWithText(TEST_TODO.title)
      .assert(!hasAnyAncestor(hasStateDescription(completedStateDescription)))
  }
}

private val TEST_TODO = Todo(id = 1, title = "Buy groceries", description = "Milk, bread, eggs")

private val TODOS =
  listOf(
    Todo(id = 1, title = "Buy groceries", description = "Milk, bread, eggs"),
    Todo(id = 2, title = "Walk the dog", description = "Evening, 30 minutes"),
  )