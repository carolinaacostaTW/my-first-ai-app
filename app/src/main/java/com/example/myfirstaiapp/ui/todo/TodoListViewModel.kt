package com.example.myfirstaiapp.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.data.todo.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TodoListViewModel @Inject constructor(
  private val todoRepository: TodoRepository,
) : ViewModel() {
  val uiState: StateFlow<TodoListUiState> =
    todoRepository.todos
      .map<List<Todo>, TodoListUiState> { todos ->
        if (todos.isEmpty()) TodoListUiState.Empty else TodoListUiState.Success(todos)
      }
      .catch { emit(TodoListUiState.Error(it)) }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoListUiState.Loading)

  fun setDone(id: Long, isDone: Boolean) {
    viewModelScope.launch { todoRepository.setDone(id, isDone) }
  }

  fun deleteById(id: Long) {
    viewModelScope.launch { todoRepository.deleteById(id) }
  }
}

sealed interface TodoListUiState {
  data object Loading : TodoListUiState

  data object Empty : TodoListUiState

  data class Error(val throwable: Throwable) : TodoListUiState

  data class Success(val todos: List<Todo>) : TodoListUiState {
    val completedCount: Int = todos.count { it.isDone }
    val totalCount: Int = todos.size
    val progress: Float =
      if (totalCount == 0) 0f else completedCount.toFloat() / totalCount.toFloat()
  }
}