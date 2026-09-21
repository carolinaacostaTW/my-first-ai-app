package com.example.myfirstaiapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.myfirstaiapp.ui.todo.TodoDetailScreen
import com.example.myfirstaiapp.ui.todo.TodoListScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(TodoList)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryDecorators =
      listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberViewModelStoreNavEntryDecorator(),
      ),
    entryProvider =
      entryProvider {
        entry<TodoList> {
          TodoListScreen(
            modifier = Modifier.fillMaxSize(),
            onAddTodo = { backStack.add(TodoDetail(NEW_TODO_ID)) },
            onOpenTodo = { todoId -> backStack.add(TodoDetail(todoId)) },
          )
        }
        entry<TodoDetail> { detail ->
          TodoDetailScreen(
            todoId = detail.todoId,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize(),
          )
        }
      },
  )
}