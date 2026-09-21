# To-Do App Product Plan & Vertical-Slice Execution

A super simple local-only To-Do app built with Jetpack Compose, Room (local
persistence), and Hilt (DI). Currently the project is an empty Compose
scaffold (greeting screen) — this plan replaces that with the real feature.

## Product spec

- **To-do list screen**: shows all to-dos. Each row has a checkbox that toggles
  "done" and persists immediately in the local Room database. Completed rows
  render with strikethrough + reduced opacity and stay in place. Empty-state
  message when there are no to-dos.
- **To-do details screen**: edit the title and description. On back navigation
  the changes are automatically saved to the local database.
- **Adding a to-do**: a FloatingActionButton on the list screen opens the
  details screen in "new" mode. Saving on back inserts the row (blank/untouched
  new rows are discarded).
- **Deleting a to-do**: long-press a row to open a confirmation dialog, then
  delete from the database.
- **To-do data model**: `id`, `title`, `description`, `isDone` — persisted
  locally in Room only. No network.

## Decisions (confirmed with the user)

- Create via **FAB** opening an empty detail screen (recommended option chosen).
- **Delete** is in v1 via long-press + confirmation dialog.
- Detail screen saves **strictly on back** (no debounced save-as-you-type).
- **Hilt** for dependency injection.
- Completed to-dos use **strikethrough + dimmed**, keeping position in the list.

## Version pins (verified for toolchain Kotlin 2.3.20 / AGP 9.1.1 / KSP2)

> Updated during S1: `androidx.hilt:hilt-navigation-compose:1.4.0` (pinned below) requires
> AGP >= 9.1 and compileSdk 37, so the toolchain was bumped from the originally planned
> AGP 9.0.1 / compileSdk 36 / Gradle 9.1.0 → AGP 9.1.1 / compileSdk 37 / Gradle 9.3.1.
> `hiltViewModel()` now lives in `androidx.hilt.lifecycle.viewmodel.compose` (pulled from
> `hilt-navigation-compose`). `targetSdk` stays 36.

| Dependency | Version | Notes |
| --- | --- | --- |
| KSP plugin `com.google.devtools.ksp` | `2.3.x` (latest stable) | Decoupled from Kotlin since 2.3.0; KSP2 only |
| Room `androidx.room:*` | `2.8.4` | Stable; KSP2 support (Room >= 2.7.2) |
| Hilt `com.google.dagger:hilt-android` + compiler | `2.60.1` | Supports Kotlin 2.3.x and AGP 9 |
| Hilt Gradle plugin `com.google.dagger.hilt.android` | `2.60.1` | Applied `apply false` at root |
| `androidx.hilt:hilt-navigation-compose` | `1.4.0` | Nav3 back-stack support |

Both Hilt and Room run through KSP; no kapt, no Room Gradle plugin.

## Reference details

- **Data layer** — `app/src/main/java/com/example/myfirstaiapp/data/todo/`:
  - `Todo.kt` — `@Entity(tableName = "todos")` with `@PrimaryKey(autoGenerate = true) Long id`, `String title`, `String description`, `Boolean isDone`. Database version 1. Room maps `Boolean` natively.
  - `TodoDao.kt` — `val todos: Flow<List<Todo>>` ordered by `id`, `fun observeById(id: Long): Flow<Todo?>`, `suspend fun insert(todo: Todo): Long`, `suspend fun update(todo: Todo)`, `suspend fun setDone(id: Long, isDone: Boolean)`, `suspend fun deleteById(id: Long)`.
  - `TodoDatabase.kt` — Room singleton; export schema to `app/schemas/` via the KSP arg `room.schemaLocation`.
  - `TodoRepository.kt` — interface + Room-backed implementation, following the existing `DataRepository` pattern.
  - Hilt module `DatabaseModule` provides the DB and DAO (`@Provides`/`@Singleton`); repository bound via `@Binds`.
- **UI + navigation** — `ui/todo/`, `Navigation.kt`, `NavigationKeys.kt`:
  - Typed destinations: `data object TodoList : NavKey`, `data class TodoDetail(val todoId: Long) : NavKey`; new-todo mode uses sentinel id `-1`.
  - `TodoListScreen`/`TodoListViewModel`: collects `repository.todos` via `collectAsStateWithLifecycle`; checkbox → `setDone` immediately; strikethrough + dim for completed rows; empty-state message; FAB → new-todo detail; long-press → `AlertDialog` → `deleteById`; `Loading`/`Error`/`Empty`/`Success` states.
  - `TodoDetailScreen`/`TodoDetailViewModel`: title + description `TextField`s; editable state seeded once from the database (rotation-safe); save-on-back via `DisposableEffect` `onDispose` → `update` or `insert`; top app bar with system back.
- **Build & manifest** — root `build.gradle.kts` adds KSP + Hilt plugins `apply false`; `gradle/libs.versions.toml` adds versions/libraries/plugins; `app/build.gradle.kts` applies plugins, adds deps + `ksp(hilt.compiler)`/`ksp(room.compiler)`, `room.schemaLocation` arg; `@HiltAndroidApp` `MyApplication` declared via `android:name`; `MainActivity` `@AndroidEntryPoint`.
- **Nav3+Hilt fallback** — if `hiltViewModel()` + `SavedStateHandle` fails to receive `todoId` for Nav3 back-stack entries, use `viewModel { ... }` factory with `createSavedStateHandle()` (lifecycle-viewmodel-navigation3, already a dependency) + repository from a Hilt entry point. Verified in Slice 3.
- **Verify commands** — `./gradlew :app:assembleDebug`, `./gradlew :app:testDebugUnitTest`, `./gradlew :app:connectedDebugAndroidTest` (device/emulator required).

## Execution plan (vertical slices)

Each slice is a complete, end-to-end feature and ends with a verify gate. Only
Slice 1 touches build configuration.

1. **S1 — Show to-dos (foundations + read path)**
   - Scope: all build changes (version catalog, KSP/Hilt plugins, Room+Hilt deps, `room.schemaLocation`), Hilt app wiring (`MyApplication`, manifest, `@AndroidEntryPoint`), data layer (`Todo`, `TodoDao`, `TodoDatabase`, `TodoRepository`, `DatabaseModule`), NavKeys, `Navigation.kt`, `TodoListViewModel` with Loading/Error/Empty/Success rendering DB rows. Remove greeting scaffold + old tests.
   - Verify: `assembleDebug`; schemas emitted to `app/schemas/`; `testDebugUnitTest` (VM states); Compose test (renders rows / empty state).

2. **S2 — Toggle done**
   - Scope: checkbox → `repository.setDone(id, isDone)`; Room Flow auto-refreshes; strikethrough + dim for completed rows.
   - Verify: `assembleDebug`; unit test (toggle propagates to fake repo); Compose test (toggle + strikethrough shown).

3. **S3 — Add a to-do**
   - Scope: FAB → detail in "new" mode (sentinel `-1`); title + description `TextField`s; save-on-back via `DisposableEffect` `onDispose` → `insert`; blank rows discarded. **Resolves the Nav3+Hilt+`SavedStateHandle` seam** — apply the fallback before proceeding if the bridge fails.
   - Verify: `assembleDebug`; unit tests (insert on back, blank not inserted).

4. **S4 — Edit a to-do**
   - Scope: tap existing row → detail seeded once from `observeById` (rotation-safe); edits saved on back via `update`.
   - Verify: `assembleDebug`; unit tests (update on back, seed-from-DB).

5. **S5 — Delete a to-do**
   - Scope: long-press row → confirm `AlertDialog` → `deleteById`.
   - Verify: `assembleDebug`; unit test (delete propagates); Compose test (dialog flow).

**Full verification** after all slices: `assembleDebug`, `testDebugUnitTest`, `connectedDebugAndroidTest`.

## Tests (parity with current structure)

- **Unit** (`test/`): fake in-memory `TodoRepository` + `runTest`.
- **Instrumented** (`androidTest/`): Compose UI tests using a fake repository (no Hilt needed).
- **Optional** Room DAO test against an in-memory database (`room-testing`).

## Risks / notes

- **Navigation3 + Hilt** is the one uncertain seam (see fallback above). Verified at build time in Slice 3.
- Strictly-on-back saving means edits are lost if the process is killed while the user is mid-edit. Accepted by design.
- No migrations at v1 (version 1 schema), but the schema export is set up now so future migrations are cheap.