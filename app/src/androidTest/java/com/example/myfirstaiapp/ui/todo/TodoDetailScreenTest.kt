package com.example.myfirstaiapp.ui.todo

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.myfirstaiapp.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/** UI tests for [TodoDetailScreen]. */
class TodoDetailScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Test
  fun fields_renderTitleAndDescription() {
    composeTestRule.setContent {
      TodoDetailScreen(
        topBarTitle = "New to-do",
        title = "Buy milk",
        onTitleChange = {},
        description = "A carton",
        onDescriptionChange = {},
      )
    }

    composeTestRule.onNodeWithText("Buy milk").assertExists()
    composeTestRule.onNodeWithText("A carton").assertExists()
  }

  @Test
  fun typing_updatesBothFields() {
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    composeTestRule.setContent {
      TodoDetailScreen(
        topBarTitle = "New to-do",
        title = title,
        onTitleChange = { title = it },
        description = description,
        onDescriptionChange = { description = it },
      )
    }

    val titleLabel = composeTestRule.activity.getString(R.string.todo_detail_title_label)
    val descriptionLabel =
      composeTestRule.activity.getString(R.string.todo_detail_description_label)
    composeTestRule.onNodeWithText(titleLabel).performTextInput("Walk the dog")
    composeTestRule.onNodeWithText(descriptionLabel).performTextInput("Evening")

    composeTestRule.onNodeWithText("Walk the dog").assertExists()
    composeTestRule.onNodeWithText("Evening").assertExists()
  }

  @Test
  fun backButton_invokesCallback() {
    var backPressed = 0
    composeTestRule.setContent {
      TodoDetailScreen(
        topBarTitle = "New to-do",
        title = "",
        onTitleChange = {},
        description = "",
        onDescriptionChange = {},
        onBack = { backPressed++ },
      )
    }

    val backDescription = composeTestRule.activity.getString(R.string.todo_detail_back)
    composeTestRule.onNodeWithContentDescription(backDescription).performClick()

    assertEquals(1, backPressed)
  }
}