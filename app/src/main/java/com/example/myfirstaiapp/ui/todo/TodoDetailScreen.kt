package com.example.myfirstaiapp.ui.todo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfirstaiapp.NEW_TODO_ID
import com.example.myfirstaiapp.R
import com.example.myfirstaiapp.theme.MyFirstAIAppTheme

@Composable
fun TodoDetailScreen(
  todoId: Long,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  viewModel: TodoDetailViewModel =
    hiltViewModel<TodoDetailViewModel, TodoDetailViewModel.Factory> { factory ->
      factory.create(todoId)
    },
) {
  val title by viewModel.title.collectAsStateWithLifecycle()
  val description by viewModel.description.collectAsStateWithLifecycle()
  DisposableEffect(Unit) { onDispose { viewModel.save() } }
  val topBarTitle =
    if (todoId == NEW_TODO_ID) {
      stringResource(R.string.todo_detail_new_title)
    } else {
      stringResource(R.string.todo_detail_edit_title)
    }
  TodoDetailScreen(
    topBarTitle = topBarTitle,
    title = title,
    onTitleChange = viewModel::onTitleChange,
    description = description,
    onDescriptionChange = viewModel::onDescriptionChange,
    onBack = onBack,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoDetailScreen(
  topBarTitle: String,
  title: String,
  onTitleChange: (String) -> Unit,
  description: String,
  onDescriptionChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = { Text(topBarTitle) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(R.string.todo_detail_back),
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .padding(16.dp),
    ) {
      OutlinedTextField(
        value = title,
        onValueChange = onTitleChange,
        label = { Text(stringResource(R.string.todo_detail_title_label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
      )
      Spacer(modifier = Modifier.height(12.dp))
      OutlinedTextField(
        value = description,
        onValueChange = onDescriptionChange,
        label = { Text(stringResource(R.string.todo_detail_description_label)) },
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TodoDetailNewPreview() {
  MyFirstAIAppTheme {
    TodoDetailScreen(
      topBarTitle = "New to-do",
      title = "",
      onTitleChange = {},
      description = "",
      onDescriptionChange = {},
    )
  }
}