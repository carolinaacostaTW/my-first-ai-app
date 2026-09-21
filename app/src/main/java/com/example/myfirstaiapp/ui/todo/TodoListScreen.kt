package com.example.myfirstaiapp.ui.todo

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myfirstaiapp.R
import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.theme.MyFirstAIAppTheme

@Composable
fun TodoListScreen(
  modifier: Modifier = Modifier,
  viewModel: TodoListViewModel = hiltViewModel(),
  onAddTodo: () -> Unit = {},
  onOpenTodo: (Long) -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  TodoListScreen(
    uiState = uiState,
    onToggleDone = viewModel::setDone,
    onDelete = viewModel::deleteById,
    onAddTodo = onAddTodo,
    onOpenTodo = onOpenTodo,
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoListScreen(
  uiState: TodoListUiState,
  modifier: Modifier = Modifier,
  onToggleDone: (id: Long, isDone: Boolean) -> Unit = { _, _ -> },
  onAddTodo: () -> Unit = {},
  onOpenTodo: (Long) -> Unit = {},
  onDelete: (Long) -> Unit = {},
) {
  var pendingDelete by remember { mutableStateOf<Todo?>(null) }
  Scaffold(
    modifier = modifier,
    topBar = { TopAppBar(title = { Text(stringResource(R.string.todo_list_title)) }) },
    floatingActionButton = {
      FloatingActionButton(onClick = onAddTodo) {
        Icon(
          imageVector = Icons.Default.Add,
          contentDescription = stringResource(R.string.todo_list_add),
        )
      }
    },
  ) { innerPadding ->
    val contentModifier = Modifier.fillMaxSize().padding(innerPadding)
    when (uiState) {
      TodoListUiState.Loading -> LoadingState(contentModifier)
      TodoListUiState.Empty -> EmptyState(contentModifier)
      is TodoListUiState.Error -> ErrorState(contentModifier)
      is TodoListUiState.Success ->
        TodoList(
          todos = uiState.todos,
          onToggleDone = onToggleDone,
          onOpenTodo = onOpenTodo,
          onLongPress = { pendingDelete = it },
          modifier = contentModifier,
        )
    }
  }
  pendingDelete?.let { todo ->
    AlertDialog(
      onDismissRequest = { pendingDelete = null },
      title = { Text(stringResource(R.string.todo_list_delete_dialog_title)) },
      text = {
        Text(stringResource(R.string.todo_list_delete_dialog_message, todo.title))
      },
      confirmButton = {
        TextButton(
          onClick = {
            pendingDelete = null
            onDelete(todo.id)
          },
        ) {
          Text(stringResource(R.string.todo_list_delete_dialog_confirm))
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingDelete = null }) {
          Text(stringResource(R.string.todo_list_delete_dialog_dismiss))
        }
      },
    )
  }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    CircularProgressIndicator()
  }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Text(
      text = stringResource(R.string.todo_list_empty),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun ErrorState(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Text(
      text = stringResource(R.string.todo_list_error),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.error,
    )
  }
}

@Composable
private fun TodoList(
  todos: List<Todo>,
  modifier: Modifier = Modifier,
  onToggleDone: (Long, Boolean) -> Unit,
  onOpenTodo: (Long) -> Unit,
  onLongPress: (Todo) -> Unit,
) {
  LazyColumn(modifier = modifier) {
    items(todos, key = { it.id }) { todo ->
      TodoRow(
        todo = todo,
        onToggleDone = onToggleDone,
        onOpenTodo = onOpenTodo,
        onLongPress = onLongPress,
      )
    }
  }
}

@Composable
internal fun TodoRow(
  todo: Todo,
  modifier: Modifier = Modifier,
  onToggleDone: (Long, Boolean) -> Unit = { _, _ -> },
  onOpenTodo: (Long) -> Unit = {},
  onLongPress: (Todo) -> Unit = {},
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(checked = todo.isDone, onCheckedChange = { isDone -> onToggleDone(todo.id, isDone) })
    val completedStateDescription =
      stringResource(R.string.todo_row_completed_state_description)
    val doneDecoration = if (todo.isDone) TextDecoration.LineThrough else null
    val contentModifier =
      Modifier
        .padding(vertical = 8.dp, horizontal = 4.dp)
        .combinedClickable(
          onClick = { onOpenTodo(todo.id) },
          onLongClick = { onLongPress(todo) },
        )
        .alpha(if (todo.isDone) 0.5f else 1f)
        .then(
          if (todo.isDone) {
            Modifier.semantics { stateDescription = completedStateDescription }
          } else {
            Modifier
          },
        )
    Column(modifier = contentModifier) {
      Text(
        text = todo.title,
        style = MaterialTheme.typography.bodyLarge,
        textDecoration = doneDecoration,
      )
      if (todo.description.isNotBlank()) {
        Text(
          text = todo.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textDecoration = doneDecoration,
        )
      }
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun TodoListSuccessPreview() {
  MyFirstAIAppTheme {
    TodoListScreen(
      uiState =
        TodoListUiState.Success(
          listOf(Todo(id = 1, title = "Buy groceries", description = "Milk, bread, eggs"), Todo(id = 2, title = "Walk the dog")),
        ),
    )
  }
}

@Preview(showBackground = true)
@Composable
private fun TodoListEmptyPreview() {
  MyFirstAIAppTheme { TodoListScreen(uiState = TodoListUiState.Empty) }
}

@Preview(showBackground = true)
@Composable
private fun TodoListErrorPreview() {
  MyFirstAIAppTheme { TodoListScreen(uiState = TodoListUiState.Error(IllegalStateException("boom"))) }
}