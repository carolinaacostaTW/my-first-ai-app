# AGENTS.md

## Project Overview

This is a personal Android To Do application.

The application must be built using modern Android development practices with a clean, maintainable and testable architecture.

The project is intentionally simple. Do not introduce unnecessary complexity, abstractions, libraries or architectural patterns unless they provide a clear benefit.

## Technology Stack

The project uses:

* Kotlin
* Jetpack Compose
* Kotlin Coroutines
* MVVM
* Local database
* Hilt for dependency injection
* AndroidX / Jetpack libraries
* Unit tests for every application layer

Prefer official Android / Jetpack solutions whenever possible.

---

# General Development Rules

## Kotlin

* Write idiomatic Kotlin.
* Prefer immutable data whenever possible.
* Prefer `val` over `var`.
* Use nullable types only when null is a meaningful state.
* Avoid `!!`.
* Prefer sealed classes/interfaces, enums or explicit state objects when representing finite states.
* Use extension functions when they improve readability.
* Avoid overly clever Kotlin code.
* Favor readable and maintainable code over compact code.

Do not introduce Java code unless there is a strong technical reason.

---

# Architecture

The application follows MVVM with clear separation of responsibilities.

Use the following conceptual layers:

```text
UI
 ↓
ViewModel
 ↓
Use Case / Domain
 ↓
Repository
 ↓
Data Source
 ↓
Local Database
```

The exact package structure may evolve, but responsibilities must remain separated.

## UI Layer

The UI layer is responsible for:

* Jetpack Compose screens
* UI state rendering
* User interactions
* Navigation
* Collecting state from ViewModels

The UI layer must NOT:

* Access the database directly.
* Contain business logic.
* Instantiate repositories manually.
* Perform dependency injection manually.
* Contain complex data manipulation that belongs in the domain layer.

Compose functions should remain focused on rendering UI and forwarding user actions.

---

# ViewModel Layer

ViewModels are responsible for:

* Managing UI state.
* Receiving user actions from the UI.
* Calling the appropriate use cases.
* Exposing state to Compose.
* Handling coroutine execution related to UI actions.

Prefer:

```kotlin
StateFlow
```

for observable UI state.

ViewModels should not:

* Access Room/SQLite directly.
* Contain database-specific logic.
* Contain large amounts of business logic.
* Create dependencies manually.

Dependencies must be provided through Hilt.

---

# Domain Layer

Business logic should live in the domain layer.

Use cases should represent meaningful application actions, for example:

```text
GetTodos
AddTodo
DeleteTodo
ToggleTodo
UpdateTodo
```

A use case should have a single clear responsibility.

Use cases should not know about:

* Compose
* Android UI
* ViewModels
* Hilt implementation details
* Specific UI components

Keep the domain layer as independent from Android as reasonably practical.

---

# Data Layer

The data layer is responsible for persistence and retrieving data.

Use the Repository pattern.

Repositories provide an abstraction between the domain layer and data sources.

Example:

```kotlin
interface TodoRepository {
    fun observeTodos(): Flow<List<Todo>>
    suspend fun addTodo(todo: Todo)
    suspend fun deleteTodo(id: Long)
    suspend fun updateTodo(todo: Todo)
}
```

The domain layer should depend on repository interfaces rather than concrete implementations.

Concrete implementations belong in the data layer.

---

# Local Database

The application uses a local database.

Prefer Room unless there is a specific reason not to use it.

Database responsibilities must remain inside the data layer.

Typical structure:

```text
Room Database
    ↓
DAO
    ↓
Data Source / Repository implementation
    ↓
Repository interface
    ↓
Use Case
    ↓
ViewModel
    ↓
Compose UI
```

Do not expose Room entities directly to the UI unless there is a clear reason.

Prefer mapping between:

```text
Database Entity
        ↓
Domain Model
        ↓
UI Model
```

when the separation provides meaningful value.

For a simple project, avoid creating unnecessary duplicate models merely for the sake of following a pattern. Use judgment.

---

# Dependency Injection

Use Hilt for dependency injection.

Dependencies must not be instantiated manually inside ViewModels, Composables or other application classes.

Prefer constructor injection:

```kotlin
class TodoRepositoryImpl @Inject constructor(
    private val todoDao: TodoDao
) : TodoRepository
```

Use Hilt modules when constructor injection is not possible or when binding interfaces to implementations.

Example:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindTodoRepository(
        implementation: TodoRepositoryImpl
    ): TodoRepository
}
```

Avoid creating unnecessary Hilt modules.

---

# Coroutines

Use Kotlin Coroutines for asynchronous work.

Use `suspend` functions for one-shot asynchronous operations.

Use `Flow` for observable streams of data.

Do not use:

```kotlin
GlobalScope
```

ViewModels should use:

```kotlin
viewModelScope
```

Repository/database operations must not block the main thread.

Do not manually create coroutine scopes unless there is a specific lifecycle requirement.

---

# UI State

Prefer a single observable UI state where appropriate.

For example:

```kotlin
data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

Avoid exposing many unrelated mutable states from a ViewModel unless there is a good reason.

Mutable state should remain private.

Example:

```kotlin
private val _uiState = MutableStateFlow(TodoUiState())
val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()
```

---

# Compose

Use Jetpack Compose for the UI.

Prefer small, focused composables.

Separate:

* Screen-level composables
* Reusable UI components
* State handling
* Event handling

Prefer stateless composables when practical.

Example:

```kotlin
@Composable
fun TodoItem(
    todo: Todo,
    onToggle: () -> Unit,
    onDelete: () -> Unit
)
```

Avoid putting business logic inside composables.

Do not launch coroutines directly from composables unless lifecycle-aware Compose APIs such as `LaunchedEffect` are appropriate.

---

# Navigation

Keep navigation concerns separate from business logic.

ViewModels and domain classes should not know about Compose navigation implementations.

Navigation events should be represented explicitly when needed.

---

# Testing

Every application layer must have appropriate tests.

At minimum, test:

## Domain

Test every use case.

Examples:

```text
GetTodosTest
AddTodoTest
DeleteTodoTest
ToggleTodoTest
```

Tests should verify business behavior rather than implementation details.

## ViewModel

Test:

* Initial state
* State changes
* User actions
* Successful operations
* Error states
* Coroutine behavior where relevant

Prefer testing observable state rather than internal implementation details.

## Data

Test:

* Repository behavior
* Mapping logic
* DAO/database behavior where appropriate

Use an in-memory Room database for database integration tests when appropriate.

## UI

Add Compose UI tests for important user flows and critical screens.

Examples:

* Displaying todos
* Adding a todo
* Completing a todo
* Deleting a todo

Do not attempt to test every implementation detail of a Composable.

---

# Test Philosophy

Tests should be:

* Deterministic
* Readable
* Fast
* Independent
* Focused on behavior

Do not write tests merely to increase code coverage.

When adding new functionality:

1. Implement the feature.
2. Add or update tests for the affected layers.
3. Run the relevant tests.
4. Fix failures before considering the task complete.

A feature is not considered complete if its required tests are missing.

---

# Error Handling

Do not silently swallow exceptions.

Avoid:

```kotlin
catch (e: Exception) {
}
```

Errors should be handled intentionally.

The UI should receive an appropriate representation of recoverable errors.

Do not expose raw technical exceptions directly to the user interface unless appropriate.

---

# Code Organization

Prefer feature-oriented organization when it improves maintainability.

For example:

```text
todo/
    data/
    domain/
    presentation/
```

rather than putting every repository, ViewModel and model from the entire application into global folders.

Keep related functionality close together.

Do not create packages containing only one class unless there is a clear architectural reason.

---

# Dependencies

Do not add a dependency without a reason.

Before introducing a new library:

1. Check whether AndroidX/Kotlin already provides the required functionality.
2. Check whether an existing project dependency can solve the problem.
3. Only then consider adding a new dependency.

Avoid dependency bloat.

---

# Coding Style

Follow the existing project's formatting and naming conventions.

Use descriptive names.

Avoid abbreviations unless they are standard Android/Kotlin terminology.

Prefer simple code.

Do not optimize prematurely.

Do not create abstractions for hypothetical future requirements.

---

# Git

Keep changes focused.

Do not modify unrelated files.

Do not rewrite large parts of the project when a smaller change is sufficient.

Do not remove existing functionality unless explicitly requested.

Do not create commits unless explicitly requested.

---

# Agent Behavior

When working on this project:

1. First inspect the existing project structure.
2. Read relevant existing files before modifying them.
3. Follow the architecture defined in this document.
4. Reuse existing patterns before introducing new ones.
5. Make the smallest reasonable change.
6. Add or update tests for the affected layer(s).
7. Run relevant tests after making changes.
8. Report what was changed and whether tests passed.

Do not invent files, classes, APIs or dependencies without checking the existing project.

Do not assume that a dependency exists. Check the project's Gradle configuration first.

Do not change architecture without explaining why the change is necessary.

If requirements are ambiguous, prefer the simplest reasonable interpretation and state the assumption.

---

# Definition of Done

A task is considered complete only when:

* The requested functionality works.
* The code follows the project's architecture.
* Dependencies are provided through Hilt where appropriate.
* Coroutines are used correctly for asynchronous operations.
* The affected layers have appropriate tests.
* Existing tests continue to pass.
* No unrelated files have been modified.
* The implementation does not introduce unnecessary complexity.
