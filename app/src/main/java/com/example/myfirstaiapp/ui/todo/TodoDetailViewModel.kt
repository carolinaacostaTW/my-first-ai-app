package com.example.myfirstaiapp.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstaiapp.NEW_TODO_ID
import com.example.myfirstaiapp.data.todo.Todo
import com.example.myfirstaiapp.data.todo.TodoRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TodoDetailViewModel.Factory::class)
class TodoDetailViewModel @AssistedInject constructor(
  @Assisted private val todoId: Long,
  private val todoRepository: TodoRepository,
) : ViewModel() {
  private val _title = MutableStateFlow("")
  val title = _title.asStateFlow()

  private val _description = MutableStateFlow("")
  val description = _description.asStateFlow()

  private var seeded = false
  private var savedId: Long? = null
  private var didSave = false

  init {
    if (todoId != NEW_TODO_ID) {
      viewModelScope.launch {
        val existing = todoRepository.observeById(todoId).first()
        if (existing != null && !seeded) {
          seeded = true
          _title.value = existing.title
          _description.value = existing.description
        }
      }
    }
  }

  fun onTitleChange(value: String) {
    _title.value = value
  }

  fun onDescriptionChange(value: String) {
    _description.value = value
  }

  fun save() {
    if (didSave) return
    didSave = true
    // NonCancellable so the DB write completes even if the entry is popped
    // (its ViewModelStore is cleared) right after this screen leaves composition.
    viewModelScope.launch(NonCancellable) {
      val title = _title.value.trim()
      if (title.isBlank()) return@launch
      val description = _description.value.trim()
      val existingId = savedId
      if (existingId != null) {
        todoRepository.update(Todo(id = existingId, title = title, description = description))
      } else {
        savedId =
          todoRepository.insert(Todo(title = title, description = description, isDone = false))
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(todoId: Long): TodoDetailViewModel
  }
}