package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Task
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val completionPercentage: Int = 0
)

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val allTasks = repository.allTasks

    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("All") // "All", "Pending", "Completed"
    val sortBy = MutableStateFlow("Date Created") // "Date Created", "Due Date", "Priority"

    // UI state for Toast messages
    val toastMessage = MutableStateFlow<String?>(null)

    // Current app screen flow: Splash -> Onboarding -> Home
    val currentScreen = MutableStateFlow(com.example.viewmodel.AppScreen.Splash)

    // Dark Mode Toggle
    val isDarkMode = MutableStateFlow(false)

    // Filtered and sorted tasks
    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks,
        searchQuery,
        selectedFilter,
        sortBy
    ) { taskList, query, filter, sort ->
        var filtered = taskList.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                               task.description.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                "Completed" -> task.isCompleted
                "Pending" -> !task.isCompleted
                else -> true
            }
            matchesQuery && matchesFilter
        }

        filtered = when (sort) {
            "Due Date" -> filtered.sortedBy { it.dueDate }
            "Priority" -> filtered.sortedWith(compareByDescending { getPriorityWeight(it.priority) })
            else -> filtered.sortedByDescending { task -> task.dateCreated } // Default Date Created descending
        }

        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard Statistics calculated reactively
    val stats: StateFlow<TaskStats> = allTasks.map { taskList ->
        val total = taskList.size
        val completed = taskList.count { it.isCompleted }
        val pending = total - completed
        val percentage = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
        TaskStats(total, completed, pending, percentage)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskStats()
    )

    private fun getPriorityWeight(priority: String): Int {
        return when (priority.uppercase()) {
            "HIGH" -> 3
            "MEDIUM" -> 2
            "LOW" -> 1
            else -> 0
        }
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            toastMessage.value = message
        }
    }

    fun clearToast() {
        toastMessage.value = null
    }

    // Database Actions
    fun addTask(title: String, description: String, priority: String, dueDate: Long, category: String) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                priority = priority,
                dueDate = dueDate,
                category = category
            )
            repository.insertTask(task)
            showToast("Task added successfully! 🎉")
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updated)
            if (updated.isCompleted) {
                showToast("Task completed! Keep it up! 🏆")
            } else {
                showToast("Task marked as pending.")
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showToast("Task deleted.")
        }
    }

    fun navigateTo(screen: AppScreen) {
        currentScreen.value = screen
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }
}

enum class AppScreen {
    Splash,
    Onboarding,
    Home
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
