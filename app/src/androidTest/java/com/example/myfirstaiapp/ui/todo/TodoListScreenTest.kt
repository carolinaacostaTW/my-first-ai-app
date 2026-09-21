package com.example.myfirstaiapp.ui.todo

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.myfirstaiapp.data.todo.Todo
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

    val message = composeTestRule.activity.getString(com.example.myfirstaiapp.R.string.todo_list_empty)
    composeTestRule.onNodeWithText(message).assertExists()
  }
}

private val TODOS =
  listOf(
    Todo(id = 1, title = "Buy groceries", description = "Milk, bread, eggs"),
    Todo(id = 2, title = "Walk the dog", description = "Evening, 30 minutes"),
  )