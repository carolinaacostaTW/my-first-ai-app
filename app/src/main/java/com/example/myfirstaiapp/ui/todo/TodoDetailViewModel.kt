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

  private val isNew = todoId == NEW_TODO_ID
  private var seeded = false
  private var userEdited = false
  private var didSave = false

  // Track explicit user edits so a seed that lands late can neither clobber
  // the visible fields nor let an untouched field drop back to an empty string.
  private var titleOverride: String? = null
  private var descriptionOverride: String? = null

  init {
    if (!isNew) {
      viewModelScope.launch {
        val loaded = todoRepository.observeById(todoId).first()
        if (loaded != null && !seeded) {
          seeded = true
          if (!userEdited) {
            _title.value = loaded.title
            _description.value = loaded.description
          }
        }
      }
    }
  }

  fun onTitleChange(value: String) {
    userEdited = true
    titleOverride = value
    _title.value = value
  }

  fun onDescriptionChange(value: String) {
    userEdited = true
    descriptionOverride = value
    _description.value = value
  }

  fun save() {
    if (didSave) return
    didSave = true
    // NonCancellable so the DB write completes even if the entry is popped
    // (its ViewModelStore is cleared) right after this screen leaves composition.
    viewModelScope.launch(NonCancellable) {
      // Re-read the row as the authoritative base so an edit always updates
      // (never re-inserts) and never loses fields that were untouched.
      val loaded = if (isNew) null else todoRepository.observeById(todoId).first()
      val title = (titleOverride ?: loaded?.title ?: _title.value).trim()
      if (title.isBlank()) return@launch
      val description = (descriptionOverride ?: loaded?.description ?: _description.value).trim()
      if (loaded != null) {
        todoRepository.update(loaded.copy(title = title, description = description))
      } else {
        todoRepository.insert(Todo(title = title, description = description, isDone = false))
      }
    }
  }

  @AssistedFactory
  interface Factory {
    fun create(todoId: Long): TodoDetailViewModel
  }
}