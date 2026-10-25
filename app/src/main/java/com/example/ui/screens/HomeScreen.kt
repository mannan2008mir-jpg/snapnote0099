package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Task
import com.example.ui.components.glassCard
import com.example.viewmodel.TaskStats
import com.example.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: TaskViewModel, isDark: Boolean) {
    val tasks by viewModel.filteredTasks.collectAsState()
    val allTasks by viewModel.allTasks.collectAsState(initial = emptyList())
    val stats by viewModel.stats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var showModal by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Navigation and personalized user profile states
    var currentTab by remember { mutableStateOf("HOME") }
    var userName by remember { mutableStateOf("Sarah") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen"),
        containerColor = Color.Transparent, // Let LiquidBackground glow underneath!
        bottomBar = {
            ImmersiveBottomBar(
                currentTab = currentTab,
                onTabSelect = { currentTab = it },
                isDark = isDark
            )
        },
        floatingActionButton = {
            if (currentTab == "HOME" || currentTab == "PLAN") {
                FloatingActionButton(
                    onClick = {
                        taskToEdit = null
                        showModal = true
                    },
                    modifier = Modifier
                        .scale(1.05f)
                        .testTag("add_task_fab"),
                    containerColor = Color(0xFFCA72FF),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task Button",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Choose which view content to display dynamically with responsive transitions
            when (currentTab) {
                "HOME" -> {
                    // Header Greeting
                    ImmersiveHeaderSection(
                        isDark = isDark,
                        userName = userName,
                        onThemeToggle = { viewModel.toggleDarkMode() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Productivity Summary Card matching HTML specs perfectly
                    ImmersiveStatsCard(stats = stats, isDark = isDark)

                    Spacer(modifier = Modifier.height(18.dp))

                    // Premium frosted glass search bar
                    GlassSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.searchQuery.value = it },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Soft horizontal filter pills
                    FilterChipsRow(
                        activeFilter = selectedFilter,
                        onFilterSelected = { viewModel.selectedFilter.value = it },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Advanced sorting toggler
                    SortToggleBar(
                        activeSort = sortBy,
                        onSortSelected = { viewModel.sortBy.value = it },
                        isDark = isDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Main Tasks Board List or empty illustration view
                    if (tasks.isEmpty()) {
                        EmptyStateIllustration(searchQuery.isNotEmpty(), isDark)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                TaskListItemCard(
                                    task = task,
                                    isDark = isDark,
                                    onCompleteToggle = { viewModel.toggleTaskCompletion(task) },
                                    onEdit = {
                                        taskToEdit = task
                                        showModal = true
                                    },
                                    onDelete = { viewModel.deleteTask(task) }
                                )
                            }
                        }
                    }
                }
                "PLAN" -> {
                    PlanTabContent(
                        tasks = allTasks,
                        isDark = isDark,
                        onCompleteToggle = { viewModel.toggleTaskCompletion(it) },
                        onEdit = {
                            taskToEdit = it
                            showModal = true
                        },
                        onDelete = { viewModel.deleteTask(it) }
                    )
                }
                "STATS" -> {
                    StatsTabContent(
                        viewModel = viewModel,
                        isDark = isDark
                    )
                }
                "ME" -> {
                    MeTabContent(
                        userName = userName,
                        onUserNameChange = { userName = it },
                        totalCount = allTasks.size,
                        completedCount = stats.completed,
                        isDark = isDark,
                        onThemeToggle = { viewModel.toggleDarkMode() }
                    )
                }
            }
        }
    }

    // Modal forms overlay
    if (showModal) {
        TaskFormModal(
            taskToEdit = taskToEdit,
            isDark = isDark,
            onDismiss = { showModal = false },
            onSave = { title, desc, priority, date, category ->
                if (taskToEdit == null) {
                    viewModel.addTask(title, desc, priority, date, category)
                } else {
                    viewModel.updateTask(
                        taskToEdit!!.copy(
                            title = title,
                            description = desc,
                            priority = priority,
                            dueDate = date,
                            category = category
                        )
                    )
                    viewModel.showToast("Task updated! 💅")
                }
                showModal = false
            }
        )
    }
}

@Composable
fun ImmersiveHeaderSection(isDark: Boolean, userName: String, onThemeToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "WELCOME BACK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCA72FF),
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Hello, $userName 👋",
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isDark) Color.White else Color(0xFF1E293B),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Theme toggle box
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .glassCard(isDark = isDark, cornerRadius = 12.dp, opacity = 0.15f)
                    .clickable { onThemeToggle() }
                    .testTag("dark_mode_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Theme Toggle Icon",
                    tint = if (isDark) Color(0xFFFFD84D) else Color(0xFFCA72FF),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Glass framed avatar box with neon gradient background
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .glassCard(isDark = isDark, cornerRadius = 14.dp, opacity = 0.25f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFCA72FF), Color(0xFFFFD84D))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun ImmersiveStatsCard(stats: TaskStats, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark = isDark, cornerRadius = 24.dp, opacity = 0.2f)
            .testTag("productivity_summary_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Productivity",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color.White.copy(0.6f) else Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${stats.completionPercentage}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Text(
                        text = "%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White.copy(0.5f) else Color(0xFF94A3B8),
                        modifier = Modifier.padding(bottom = 4.dp, start = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val trendText = if (stats.completionPercentage >= 60) "+12% from yesterday" else "Check things off! 🚀"
                val trendColor = if (stats.completionPercentage >= 60) Color(0xFF10B981) else Color(0xFFCA72FF)

                Text(
                    text = trendText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = trendColor
                )
            }

            // Circular progress ring showing DONE label in center
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(76.dp)
            ) {
                val animateSweep by animateFloatAsState(
                    targetValue = stats.completionPercentage.toFloat() * 3.6f,
                    animationSpec = tween(1200),
                    label = "percentageRing"
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFCA72FF).copy(0.12f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawArc(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFCA72FF), Color(0xFFFFD84D))
                        ),
                        startAngle = -90f,
                        sweepAngle = animateSweep,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Text(
                    text = "DONE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) Color.White else Color(0xFF1E293B),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassSearchBar(query: String, onQueryChange: (String) -> Unit, isDark: Boolean) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                "Search tasks or description...",
                fontSize = 14.sp,
                color = if (isDark) Color.White.copy(0.4f) else Color(0xFF1E293B).copy(0.4f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search icon",
                tint = if (isDark) Color.White.copy(0.5f) else Color(0xFFCA72FF).copy(0.6f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search query",
                        tint = if (isDark) Color.White.copy(0.6f) else Color.Gray
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = Color(0xFFCA72FF)
        ),
        textStyle = LocalTextStyle.current.copy(
            color = if (isDark) Color.White else Color(0xFF1E293B),
            fontSize = 14.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .glassCard(isDark = isDark, cornerRadius = 16.dp, opacity = 0.08f)
            .testTag("search_input")
    )
}

@Composable
fun FilterChipsRow(activeFilter: String, onFilterSelected: (String) -> Unit, isDark: Boolean) {
    val filters = listOf("All", "Pending", "Completed")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == activeFilter

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clip(RoundedCornerShape(50)) // Pill Shape
                    .background(
                        if (isSelected) Color(0xFFCA72FF) else (if (isDark) Color(0x22FFFFFF) else Color(0x66FFFFFF))
                    )
                    .border(
                        width = 1.dp,
                        color = if (isSelected) Color.Transparent else (if (isDark) Color(0x1AFFFFFF) else Color(0x4DFFFFFF)),
                        shape = RoundedCornerShape(50)
                    )
                    .clickable { onFilterSelected(filter) }
                    .testTag("filter_chip_${filter.lowercase()}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else (if (isDark) Color.White.copy(0.7f) else Color(0xFF475569))
                )
            }
        }
    }
}

@Composable
fun SortToggleBar(activeSort: String, onSortSelected: (String) -> Unit, isDark: Boolean) {
    val options = listOf("Date Created", "Due Date", "Priority")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Sort:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF1E293B).copy(alpha = 0.5f),
            modifier = Modifier.padding(end = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.weight(1f)
        ) {
            options.forEach { opt ->
                val isSelected = opt == activeSort
                Box(
                    modifier = Modifier
                        .glassCard(
                            isDark = isDark,
                            cornerRadius = 8.dp,
                            opacity = if (isSelected) 0.3f else 0.02f,
                            borderWidth = 0.5.dp
                        )
                        .clickable { onSortSelected(opt) }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color(0xFFCA72FF) else (if (isDark) Color.White.copy(0.5f) else Color.Gray)
                    )
                }
            }
        }
    }
}

@Composable
fun TaskListItemCard(
    task: Task,
    isDark: Boolean,
    onCompleteToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateText = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(task.dueDate))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark = isDark, cornerRadius = 24.dp, opacity = if (task.isCompleted) 0.06f else 0.12f)
            .testTag("task_card_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Elegant Rounded Glass Checkbox
            Box(
                modifier = Modifier
                    .padding(top = 2.dp, end = 12.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (task.isCompleted) Color(0xFFCA72FF).copy(0.15f) else Color.Transparent
                    )
                    .border(
                        1.5.dp,
                        if (task.isCompleted) Color(0xFFCA72FF) else (if (isDark) Color.White.copy(0.3f) else Color(0xFFCA72FF).copy(0.4f)),
                        CircleShape
                    )
                    .clickable { onCompleteToggle() }
                    .testTag("task_checkbox_toggle_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed checkmark",
                        tint = Color(0xFFCA72FF),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Task Meta Data Fields
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Category Tag
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFCA72FF).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = task.category,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCA72FF)
                        )
                    }

                    // Priority level Badge
                    PriorityBadge(priority = task.priority)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (task.isCompleted) {
                        if (isDark) Color.White.copy(0.4f) else Color.LightGray
                    } else {
                        if (isDark) Color.White else Color(0xFF1E293B)
                    },
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.description,
                        fontSize = 12.sp,
                        color = if (task.isCompleted) {
                            if (isDark) Color.White.copy(0.25f) else Color.LightGray.copy(0.8f)
                        } else {
                            if (isDark) Color.White.copy(0.65f) else Color(0xFF1E293B).copy(0.7f)
                        },
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Due Date calendar icon",
                        tint = if (isDark) Color.White.copy(0.4f) else Color.Gray,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: $dateText",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isDark) Color.White.copy(0.5f) else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action options column
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("task_edit_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Button",
                        tint = Color(0xFFCA72FF).copy(0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("task_delete_button_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Button",
                        tint = Color(0xFFEF5350).copy(0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (labelColor, wrapperColor) = when (priority.uppercase()) {
        "HIGH" -> Pair(Color(0xFFEF5350), Color(0x1AEF5350))
        "MEDIUM" -> Pair(Color(0xFFFFB74D), Color(0x1AFFB74D))
        else -> Pair(Color(0xFF64B5F6), Color(0x1A64B5F6))
    }

    Box(
        modifier = Modifier
            .background(wrapperColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = labelColor
        )
    }
}

@Composable
fun EmptyStateIllustration(isSearchActive: Boolean, isDark: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
            .testTag("empty_state_view"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(Color(0xFFCA72FF).copy(0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSearchActive) Icons.Default.SearchOff else Icons.Default.FilterListOff,
                contentDescription = "Empty list status icon",
                tint = Color(0xFFCA72FF),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isSearchActive) "No matches found" else "Ready to SnapNote!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isSearchActive) "Try standard keywords or redefine text fields." else "Tap the floating button (+) below to write down your first immersive task card!",
            fontSize = 12.sp,
            color = if (isDark) Color.White.copy(0.5f) else Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun PlanTabContent(
    tasks: List<Task>,
    isDark: Boolean,
    onCompleteToggle: (Task) -> Unit,
    onEdit: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    var selectedDayCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val daysOfWeek = remember {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val list = mutableListOf<Calendar>()
        for (i in 0 until 7) {
            list.add(calendar.clone() as Calendar)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "WEEKLY AGENDA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCA72FF),
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Task Planner 🗓️",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Horizontal week date cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            daysOfWeek.forEach { dayCal ->
                val isSelected = dayCal.get(Calendar.DAY_OF_YEAR) == selectedDayCalendar.get(Calendar.DAY_OF_YEAR) &&
                        dayCal.get(Calendar.YEAR) == selectedDayCalendar.get(Calendar.YEAR)

                val dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(dayCal.time)
                val dayNum = SimpleDateFormat("dd", Locale.getDefault()).format(dayCal.time)

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(colors = listOf(Color(0xFFCA72FF), Color(0xFFFFD84D)))
                            } else {
                                androidx.compose.ui.graphics.SolidColor(if (isDark) Color(0x1BFFFFFF) else Color(0x33FFFFFF))
                            }
                        )
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else (if (isDark) Color(0x1AFFFFFF) else Color(0x4DFFFFFF)),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedDayCalendar = dayCal }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = dayName.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else (if (isDark) Color.White.copy(0.6f) else Color(0xFF64748B))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNum,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color.White else (if (isDark) Color.White else Color(0xFF1E293B))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        val dayTasks = remember(tasks, selectedDayCalendar) {
            tasks.filter { task ->
                val taskCal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
                taskCal.get(Calendar.YEAR) == selectedDayCalendar.get(Calendar.YEAR) &&
                        taskCal.get(Calendar.DAY_OF_YEAR) == selectedDayCalendar.get(Calendar.DAY_OF_YEAR)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AGENDA FOR " + SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDayCalendar.time).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
                letterSpacing = 1.sp
            )

            Text(
                text = "${dayTasks.size} notes",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCA72FF)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (dayTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .glassCard(isDark = isDark, cornerRadius = 24.dp, opacity = 0.1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Cleared plan indicator",
                        tint = Color(0xFFCA72FF).copy(0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "A clean slate! 🧘",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Take a break or schedule a new task for this day using the floating button (+).",
                        fontSize = 12.sp,
                        color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(dayTasks, key = { it.id }) { task ->
                    TaskListItemCard(
                        task = task,
                        isDark = isDark,
                        onCompleteToggle = { onCompleteToggle(task) },
                        onEdit = { onEdit(task) },
                        onDelete = { onDelete(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsTabContent(
    viewModel: TaskViewModel,
    isDark: Boolean
) {
    val stats by viewModel.stats.collectAsState()
    val tasks by viewModel.allTasks.collectAsState(initial = emptyList())

    val highPriorityCount = remember(tasks) { tasks.count { it.priority.uppercase() == "HIGH" } }
    val medPriorityCount = remember(tasks) { tasks.count { it.priority.uppercase() == "MEDIUM" } }
    val lowPriorityCount = remember(tasks) { tasks.count { it.priority.uppercase() == "LOW" } }

    val highCompleted = remember(tasks) { tasks.count { it.priority.uppercase() == "HIGH" && it.isCompleted } }
    val medCompleted = remember(tasks) { tasks.count { it.priority.uppercase() == "MEDIUM" && it.isCompleted } }
    val lowCompleted = remember(tasks) { tasks.count { it.priority.uppercase() == "LOW" && it.isCompleted } }

    val milestoneText = when {
        stats.completionPercentage >= 85 -> "Elite Optimizer 🏆"
        stats.completionPercentage >= 60 -> "Focused Prodigy ⚡"
        stats.completionPercentage >= 30 -> "Action Rookie 🌱"
        else -> "Rising Organizer ✨"
    }

    val motivatorQuote = when {
        stats.completionPercentage >= 85 -> "You are fully crushing your workflows. Take a bow!"
        stats.completionPercentage >= 60 -> "Strong alignment detected. Keep completing targeted items."
        stats.completionPercentage >= 30 -> "Incredible start! Power up with a couple more checkmarks."
        else -> "One task at a time. The secret to getting ahead is getting started."
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "PRODUCTIVITY INSIGHTS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCA72FF),
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Analytics Pulse 📊",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Large Premium Badge Rank Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(isDark = isDark, cornerRadius = 24.dp, opacity = 0.25f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFCA72FF).copy(0.12f), Color.Transparent),
                        radius = 400f
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRODUCTIVITY RANKING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD84D),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = milestoneText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFD84D).copy(0.2f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DoneAll,
                            contentDescription = "Milestone Medal",
                            tint = Color(0xFFFFD84D),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = motivatorQuote,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(0.7f) else Color(0xFF475569)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Numerical breakdown cards row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .glassCard(isDark = isDark, cornerRadius = 14.dp, opacity = 0.1f)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("COMPLETED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B))
                            Text("${stats.completed}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFCA72FF))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .glassCard(isDark = isDark, cornerRadius = 14.dp, opacity = 0.1f)
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("OUTSTANDING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B))
                            Text("${stats.pending}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD84D))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "PRIORITY BREAKDOWN METER",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PriorityBarMeter(
                priorityName = "HIGH PRIORITY",
                completed = highCompleted,
                total = highPriorityCount,
                color = Color(0xFFEF5350),
                isDark = isDark
            )
            PriorityBarMeter(
                priorityName = "MEDIUM PRIORITY",
                completed = medCompleted,
                total = medPriorityCount,
                color = Color(0xFFFFB74D),
                isDark = isDark
            )
            PriorityBarMeter(
                priorityName = "LOW PRIORITY",
                completed = lowCompleted,
                total = lowPriorityCount,
                color = Color(0xFF64B5F6),
                isDark = isDark
            )
        }
    }
}

@Composable
fun PriorityBarMeter(
    priorityName: String,
    completed: Int,
    total: Int,
    color: Color,
    isDark: Boolean
) {
    val percentage = if (total > 0) (completed.toFloat() / total.toFloat() * 100).toInt() else 0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark = isDark, cornerRadius = 18.dp, opacity = 0.12f)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priorityName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )
                Text(
                    text = "$completed/$total ($percentage%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color.copy(0.12f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (total > 0) completed.toFloat() / total.toFloat() else 0f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun MeTabContent(
    userName: String,
    onUserNameChange: (String) -> Unit,
    totalCount: Int,
    completedCount: Int,
    isDark: Boolean,
    onThemeToggle: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 80.dp)
    ) {
        Text(
            text = "PERSONAL PROFILE",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFCA72FF),
            letterSpacing = 1.5.sp
        )
        Text(
            text = "User Identity 👤",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else Color(0xFF1E293B)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Center glass profile card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassCard(isDark = isDark, cornerRadius = 24.dp, opacity = 0.25f)
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Large styled Avatar with linear brush
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFFCA72FF), Color(0xFFFFD84D))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userName.isNotEmpty()) userName.take(1).uppercase() else "S",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Edit Profile Name",
                    fontSize = 11.sp,
                    color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                TextField(
                    value = userName,
                    onValueChange = onUserNameChange,
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x33000000) else Color(0xFFF1EEF8),
                        unfocusedContainerColor = if (isDark) Color(0x33000000) else Color(0xFFF1EEF8),
                        focusedIndicatorColor = Color(0xFFCA72FF),
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("me_username_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats quick counters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                        Text(
                            text = "Total tasks",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(30.dp)
                            .background(if (isDark) Color.White.copy(0.15f) else Color.Gray.copy(0.2f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$completedCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFCA72FF)
                        )
                        Text(
                            text = "Completed",
                            fontSize = 11.sp,
                            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "CUSTOM CONTROL PREFERENCES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Theme setting switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(isDark = isDark, cornerRadius = 16.dp, opacity = 0.12f)
                    .clickable { onThemeToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Theme switcher",
                        tint = Color(0xFFCA72FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Visual Interface Tone",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }

                Text(
                    text = if (isDark) "DARK THEME" else "LIGHT THEME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD84D)
                )
            }

            // Version info setting
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassCard(isDark = isDark, cornerRadius = 16.dp, opacity = 0.12f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info Icon",
                        tint = Color(0xFFFFD84D),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SnapNote Engine",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color(0xFF1E293B)
                    )
                }

                Text(
                    text = "v2.5.0 Premium",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(0.5f) else Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
fun ImmersiveBottomBar(
    currentTab: String,
    onTabSelect: (String) -> Unit,
    isDark: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .glassCard(isDark = isDark, cornerRadius = 0.dp, opacity = 0.35f, borderWidth = 0.dp)
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(0)
            )
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("HOME", Icons.Default.Home, "HOME"),
                Triple("PLAN", Icons.Default.CalendarToday, "PLAN"),
                Triple("STATS", Icons.Default.BarChart, "STATS"),
                Triple("ME", Icons.Default.Person, "ME")
            )

            tabs.forEach { (tabId, icon, label) ->
                val isSelected = currentTab == tabId
                val activeColor = Color(0xFFCA72FF)
                val inactiveColor = if (isDark) Color.White.copy(0.6f) else Color(0xFF94A3B8)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onTabSelect(tabId) }
                        .testTag("nav_tab_$tabId")
                        .padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) activeColor else inactiveColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) activeColor else inactiveColor,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskFormModal(
    taskToEdit: Task?,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, priority: String, dueDate: Long, category: String) -> Unit
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var priority by remember { mutableStateOf(taskToEdit?.priority ?: "Medium") }
    var category by remember { mutableStateOf(taskToEdit?.category ?: "Work") }

    val initialDue = taskToEdit?.dueDate ?: (System.currentTimeMillis() + 86400000) // tomorrow default
    var dueDateMills by remember { mutableStateOf(initialDue) }

    val context = LocalContext.current
    val activityContext = remember(context) {
        var ctx = context
        while (ctx is android.content.ContextWrapper) {
            if (ctx is android.app.Activity) {
                break
            }
            ctx = ctx.baseContext
        }
        ctx
    }
    val simpleDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .glassCard(isDark = isDark, cornerRadius = 28.dp, opacity = 0.2f)
                .padding(20.dp)
                .testTag("task_form_modal")
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Title Header
                Text(
                    text = if (taskToEdit == null) "New Productivity Task" else "Edit Task Parameters",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E293B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                Text(
                    "Task Title",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(0.6f) else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Log a task...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x33000000) else Color(0xFFF1EEF8),
                        unfocusedContainerColor = if (isDark) Color(0x33000000) else Color(0xFFF1EEF8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description Input
                Text(
                    "Detailed Description",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(0.6f) else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Add instructions or note details...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color(0x33000000) else Color(0xFFF1EEF8),
                        unfocusedContainerColor = if (isDark) Color(0x33000000) else Color(0xFFF1EEF8),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(78.dp)
                        .testTag("modal_task_description_input")
                )

                Spacer(modifier = Modifier.height(11.dp))

                // Category row
                Text(
                    "Category Tag",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(0.6f) else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                val categories = listOf("Work", "Personal", "Health", "Leisure")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSel = cat == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .glassCard(
                                    isDark = isDark,
                                    cornerRadius = 8.dp,
                                    opacity = if (isSel) 0.35f else 0.05f,
                                    borderWidth = 0.5.dp
                                )
                                .clickable { category = cat },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cat,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color(0xFFCA72FF) else (if (isDark) Color.White.copy(0.5f) else Color.Gray)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(11.dp))

                // Priority Selection row
                Text(
                    "Task Priority",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White.copy(0.6f) else Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                val priorities = listOf("Low", "Medium", "High")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    priorities.forEach { prio ->
                        val isSel = prio == priority
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .glassCard(
                                    isDark = isDark,
                                    cornerRadius = 8.dp,
                                    opacity = if (isSel) 0.35f else 0.05f,
                                    borderWidth = 0.5.dp
                                )
                                .clickable { priority = prio }
                                .testTag("priority_selector_$prio"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                prio,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) {
                                    when (prio) {
                                        "High" -> Color(0xFFEF5350)
                                        "Medium" -> Color(0xFFFFB74D)
                                        else -> Color(0xFF64B5F6)
                                    }
                                } else {
                                    if (isDark) Color.White.copy(0.5f) else Color.Gray
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Due Date Picker row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Due Date",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White.copy(0.6f) else Color.Gray
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = simpleDate.format(Date(dueDateMills)),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color(0xFF1E293B)
                        )
                    }

                    Button(
                        onClick = {
                            val c = Calendar.getInstance()
                            c.timeInMillis = dueDateMills
                            val picker = DatePickerDialog(
                                activityContext,
                                { _, year, month, day ->
                                    val out = Calendar.getInstance()
                                    out.set(year, month, day)
                                    dueDateMills = out.timeInMillis
                                },
                                c.get(Calendar.YEAR),
                                c.get(Calendar.MONTH),
                                c.get(Calendar.DAY_OF_MONTH)
                            )
                            picker.show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA72FF).copy(0.15f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("date_picker_button")
                    ) {
                        Text("Select Date", color = Color(0xFFCA72FF), fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Dialog Cancel/Save Controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCA72FF)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("modal_cancel_button")
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, description, priority, dueDateMills, category)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCA72FF)),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("modal_save_button"),
                        enabled = title.isNotBlank()
                    ) {
                        Text("Save Task")
                    }
                }
            }
        }
    }
}
