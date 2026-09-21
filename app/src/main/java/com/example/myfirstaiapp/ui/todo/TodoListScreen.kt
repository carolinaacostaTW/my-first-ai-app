package com.example.myfirstaiapp.ui.todo

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
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
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(R.string.todo_list_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
          )
        },
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
          ),
      )
    },
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = onAddTodo,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        icon = {
          Icon(
            imageVector = Icons.Default.Add,
            contentDescription = stringResource(R.string.todo_list_add),
          )
        },
        text = { Text(stringResource(R.string.todo_list_add_label)) },
      )
    },
  ) { innerPadding ->
    val contentModifier = Modifier.fillMaxSize().padding(innerPadding)
    when (uiState) {
      TodoListUiState.Loading -> LoadingState(contentModifier)
      TodoListUiState.Empty -> EmptyState(contentModifier)
      is TodoListUiState.Error -> ErrorState(contentModifier)
      is TodoListUiState.Success ->
        TodoListContent(
          state = uiState,
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
  Column(
    modifier = modifier.padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    IconBadge(
      icon = Icons.Default.CheckCircle,
      contentDescription = null,
      containerColor = MaterialTheme.colorScheme.primaryContainer,
      contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = stringResource(R.string.todo_list_empty_title),
      style = MaterialTheme.typography.titleLarge,
      fontWeight = FontWeight.SemiBold,
      color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(modifier = Modifier.height(8.dp))
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
  Column(
    modifier = modifier.padding(horizontal = 32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    IconBadge(
      icon = Icons.Default.Warning,
      contentDescription = null,
      containerColor = MaterialTheme.colorScheme.errorContainer,
      contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = stringResource(R.string.todo_list_error),
      style = MaterialTheme.typography.bodyLarge,
      textAlign = TextAlign.Center,
      color = MaterialTheme.colorScheme.error,
    )
  }
}

@Composable
private fun IconBadge(
  icon: ImageVector,
  contentDescription: String?,
  containerColor: Color,
  contentColor: Color,
) {
  Box(
    modifier = Modifier.size(72.dp),
    contentAlignment = Alignment.Center,
  ) {
    Card(
      modifier = Modifier.fillMaxSize(),
      shape = CircleShape,
      colors = CardDefaults.cardColors(containerColor = containerColor),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Icon(
          imageVector = icon,
          contentDescription = contentDescription,
          tint = contentColor,
          modifier = Modifier.size(36.dp),
        )
      }
    }
  }
}

@Composable
private fun TodoListContent(
  state: TodoListUiState.Success,
  modifier: Modifier = Modifier,
  onToggleDone: (Long, Boolean) -> Unit,
  onOpenTodo: (Long) -> Unit,
  onLongPress: (Todo) -> Unit,
) {
  Column(modifier = modifier.fillMaxSize()) {
    ProgressHeader(state = state, modifier = Modifier.padding(horizontal = 16.dp))
    Spacer(modifier = Modifier.height(8.dp))
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      items(state.todos, key = { it.id }) { todo ->
        TodoRow(
          todo = todo,
          onToggleDone = onToggleDone,
          onOpenTodo = onOpenTodo,
          onLongPress = onLongPress,
        )
      }
    }
  }
}

@Composable
private fun ProgressHeader(state: TodoListUiState.Success, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
      Text(
        text = stringResource(R.string.todo_list_progress, state.completedCount, state.totalCount),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Spacer(modifier = Modifier.height(12.dp))
      LinearProgressIndicator(
        progress = { state.progress },
        modifier = Modifier.fillMaxWidth().height(8.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
        strokeCap = StrokeCap.Round,
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
  val shape = RoundedCornerShape(18.dp)
  Card(
    modifier = modifier.fillMaxWidth(),
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Checkbox(
        checked = todo.isDone,
        onCheckedChange = { isDone -> onToggleDone(todo.id, isDone) },
        colors =
          CheckboxDefaults.colors(
            checkedColor = MaterialTheme.colorScheme.primary,
            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
      )
      val completedStateDescription =
        stringResource(R.string.todo_row_completed_state_description)
      val doneDecoration = if (todo.isDone) TextDecoration.LineThrough else null
      val contentModifier =
        Modifier
          .weight(1f)
          .padding(vertical = 6.dp, horizontal = 4.dp)
          .combinedClickable(
            onClick = { onOpenTodo(todo.id) },
            onLongClick = { onLongPress(todo) },
          )
          .alpha(if (todo.isDone) 0.55f else 1f)
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
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Medium,
          textDecoration = doneDecoration,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        if (todo.description.isNotBlank()) {
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = todo.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = doneDecoration,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      )
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
          listOf(
            Todo(id = 1, title = "Buy groceries", description = "Milk, bread, eggs"),
            Todo(id = 2, title = "Walk the dog", isDone = true),
            Todo(id = 3, title = "Book dentist appointment", description = "Ask about the 3pm slot"),
          ),
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
