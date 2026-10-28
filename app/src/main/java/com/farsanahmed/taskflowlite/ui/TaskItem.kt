package com.farsanahmed.taskflowlite.ui

import androidx.compose.foundation.checkbox
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.interaction
import androidx.compose.foundation.rememberClickable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawable.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.farsanahmed.taskflowlite.database.TaskEntity

@Composable
fun TaskItem(
    task: TaskEntity,
    selectedFilter: String,
    onToggleComplete: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter check: show only tasks matching the current filter
    val showItem = when (selectedFilter) {
        "completed" -> task.isCompleted
        "pending" -> !task.isCompleted
        else -> true
    }

    if (!showItem) return

    var checked by remember { mutableStateOf(task.isCompleted) }

    ButtonContainer(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = { onToggleComplete(task) },
        enabled = true
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            interactionSource = MutableInteractionSource(),
            state = remember { interactionSource }
        ) {
            // Checkbox for completion status
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    checked = it
                    onToggleComplete(task.copy(isCompleted = it))
                },
                modifier = Modifier
                    .width(24.dp)
                    .height(24.dp)
            )

            // Task title and description
            Column(
                modifier = Modifier.weight(1f).padding(8.dp, 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        // color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            // Delete button (trailing)
            Button(
                onClick = { onDelete(task) },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
