package com.example.myfirstaiapp

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object TodoList : NavKey

@Serializable data class TodoDetail(val todoId: Long) : NavKey

const val NEW_TODO_ID: Long = -1L