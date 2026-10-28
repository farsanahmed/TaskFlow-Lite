package com.farsanahmed.taskflowlite.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberClickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.keyboardActions
import androidx.compose.foundation.text.keyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.RequestFocus
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.keyEvent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.viewModelScope
import com.farsanahmed.taskflowlite.database.TaskDatabase
import com.farsanahmed.taskflowlite.database.TaskEntity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TaskScreen() {
    val context = LocalContext.current
    val filterState = remember { mutableStateOf("all") }
    var addTaskTitle by remember { mutableStateOf("") }
    var addTaskDesc by remember { mutableStateOf("") }

    // ViewModel-like state
    val db = TaskDatabase.getInstance(context)
    val dao = db.taskDao()

    // Load tasks from database
    var tasks by remember { mutableStateOf<List<TaskEntity>>(emptyList()) }
    val pendingCount: Int = dao.getPendingTasks().size
    val completedCount: Int = dao.getCompletedTasks().size

    // Load tasks on composition
    effect(Unit) {
        tasks = dao.getAllTasks()
    }

    // Filtered tasks
    val filteredTasks: List<TaskEntity> {
        when (filterState) {
            "all" -> tasks
            "pending" -> tasks.filter { !it.isCompleted }
            "completed" -> tasks.filter { it.isCompleted }
            else -> tasks
        }
    }

    // Summary counts based on filtered tasks
    val totalTasksCount = filteredTasks.size
    val pendingTasksCount = filteredTasks.count { !it.isCompleted }
    val completedTasksCount = filteredTasks.count { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // App header
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            MaterialTheme.typography.displayMedium
        }

        // Summary section
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${totalTasksCount} tasks",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (filterState == "all" || filterState == "pending") {
                    Text(
                        text = "${pendingTasksCount} pending",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (filterState == "all" || filterState == "completed") {
                    Text(
                        text = "${completedTasksCount} completed",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Filter chips
            TabGrouping(
                modifier = Modifier.padding(end = 8.dp),
                value = filterState,
                onValueSelected = { filterState = it }
            ) {
                chip("All")
                chip("Pending")
                chip("Completed")
            }
        }

        // Tasks list
        if (filteredTasks.isEmpty()) {
            EmptyState(showAddButton = filterState == "all")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWeight(),
                contentPadding = PaddingValues(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredTasks) { task ->
                    TaskItem(
                        task = task,
                        selectedFilter = filterState,
                        onToggleComplete = { updatedTask ->
                            dao.updateTask(updatedTask)
                            tasks = dao.getAllTasks()
                        },
                        onDelete = { task ->
                            // Show confirmation
                            val confirmation = androidx.compose.foundation.isSystemInDarkTheme
                            // Simple confirmation dialog
                            ScaffoldSnackbar(
                                message = "Delete task: ${task.title}?"
                            )
                            dao.deleteTask(task)
                            tasks = dao.getAllTasks()
                        }
                    )
                }
            }
        }

        // Add task floating action button area
        if (filterState == "all") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = { /* Open add task dialog */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "Add Task")
                }
            }
        }
    }
}

// Empty state composable
@Composable
fun EmptyState(showAddButton: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_task_empty),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            contentScale = ImageViewScale.Fit,
            modifier = Modifier.size(64.dp)
        )
        Text(
            text = "No tasks yet",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        if (showAddButton) {
            OutlinedButton(
                onClick = { /* Open add task dialog */ },
                modifier = Modifier.width(120.dp)
            ) {
                Text(text = "Add First Task")
            }
        } else {
            Text(
                text = "No tasks match your filter",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// Tab grouping composable for filter chips
@Composable
fun TabGrouping(
    modifier: Modifier = Modifier,
    value: String,
    onValueSelected: (String) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(modifier = modifier) {
        content()
        // Selected indicator
        Box(
            modifier = Modifier
                .align(Alignment.CenterRight)
                .size(value.length * 8.dp + 8.dp)
                .background(
                    if (value != "") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    }
                )
        )
    }
}

// Snackbar helper
@Composable
fun ScaffoldSnackbar(message: String) {
    SnackbarHostState().showSnackbar(message)
}
