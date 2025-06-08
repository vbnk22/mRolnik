package com.example.mrolnik.screen

import Task
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mrolnik.model.Planner
import com.example.mrolnik.service.PlannerService
import com.example.mrolnik.service.TaskService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@SuppressLint("NewApi")
@Composable
fun PlannerScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var taskText by remember { mutableStateOf("") }

    var taskName by remember { mutableStateOf("") }
    var taskDate by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Stan edytowanego zadania
    var editTaskName by remember { mutableStateOf("") }
    var editTaskDate by remember { mutableStateOf("") }
    var editTaskDescription by remember { mutableStateOf("") }

    // Stan do wyświetlania info o zadaniu
    var infoTaskName by remember { mutableStateOf("") }
    var infoTaskDate by remember { mutableStateOf("") }
    var infoTaskDescription by remember { mutableStateOf("") }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val plannerService = PlannerService()
    var planner by remember { mutableStateOf<Planner?>(null) }

    val taskService = TaskService()
    var taskList by remember { mutableStateOf<List<Task>>(emptyList()) }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    // Add this LaunchedEffect to initialize the planner
    LaunchedEffect(Unit) {
        planner = plannerService.createOrReturnPlanner()
        Log.i("PlannerScreen", "Initialized planner: ${planner?.plannerId}, ${planner?.createDate}")
    }

    LaunchedEffect(selectedDate, planner?.plannerId) {
        if (planner != null) {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val formattedDate = selectedDate.format(dateFormatter)
            planner?.plannerId?.let { pid ->
                taskList = taskService.getTasksByDate(pid, formattedDate)
                Log.d("TaskService", "Fetched ${taskList.size} tasks for plannerId=${pid}, date=$formattedDate")
            }
        }
    }

    Log.i("PlannerScreen", "PlannerId: ${planner?.plannerId}, ${planner?.createDate}")

    fun onEditTask(name: String, date: String, description: String) {
        editTaskName = name
        editTaskDate = date
        editTaskDescription = description
        showEditDialog = true
    }

    fun onInfoTask(name: String, date: String, description: String) {
        infoTaskName = name
        infoTaskDate = date
        infoTaskDescription = description
        showInfoDialog = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = 80.dp)
        ) {
            // Header z przyciskiem powrotu i tytułem
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Wróć",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Planer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }

            // Nawigacja miesięcy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { currentMonth = currentMonth.minusMonths(1) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Poprzedni miesiąc",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { currentMonth = currentMonth.plusMonths(1) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Następny miesiąc",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Kalendarz
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    DaysOfMonth(
                        month = currentMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { date -> selectedDate = date }
                    )
                }
            }

            // Tytuł listy zadań
            Text(
                text = "Zadania na ${selectedDate.dayOfMonth}.${selectedDate.monthValue}.${selectedDate.year}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Lista zadań
            if (taskList.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = "Brak zadań",
                            modifier = Modifier.size(48.dp),
                            tint = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Brak zadań",
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Dodaj pierwsze zadanie używając pola poniżej",
                            fontSize = 14.sp,
                            color = Color(0xFF9E9E9E),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(taskList) { task ->
                        TaskCard(
                            task = task,
                            onEditTask = { task ->
                                editTaskName = task.taskName
                                editTaskDate = task.realizeDate
                                editTaskDescription = task.description
                                editingTask = task
                                showEditDialog = true
                            },
                            onInfoTask = { task ->
                                infoTaskName = task.taskName
                                infoTaskDate = task.realizeDate
                                infoTaskDescription = task.description
                                showInfoDialog = true
                            },
                            onDeleteTask = { task ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        taskService.deleteTask(task)
                                        withContext(Dispatchers.Main) {
                                            taskList = taskList.filterNot { it.taskId == task.taskId }
                                            snackbarHostState.showSnackbar(
                                                message = "Zadanie zostało usunięte",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            snackbarHostState.showSnackbar(
                                                message = "Błąd podczas usuwania zadania",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        // Dolny pasek do dodawania zadania
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = taskText,
                    onValueChange = { taskText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Dodaj zadanie...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        taskName = taskText
                        taskDate = selectedDate.toString()
                        showAddDialog = true
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF4CAF50))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Dodaj zadanie",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ) {
                Text(
                    text = snackbarData.visuals.message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }

        // Dialog dodawania zadania
        if (showAddDialog) {
            CustomModalDialog(
                onDismiss = { showAddDialog = false },
                title = "Nowe zadanie",
                onConfirm = {
                    if (taskName.isBlank()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar(
                                message = "Nazwa zadania nie może być pusta",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@CustomModalDialog
                    }

                    planner?.let {
                        val newTask = Task(
                            taskId = 0,
                            taskName = taskName,
                            realizeDate = taskDate,
                            description = taskDescription,
                            plannerId = it.plannerId
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                taskService.addTask(newTask)
                                val refreshed = taskService.getTasksByDate(it.plannerId, selectedDate.toString())
                                withContext(Dispatchers.Main) {
                                    taskList = refreshed
                                    showAddDialog = false
                                    taskName = ""
                                    taskDate = ""
                                    taskDescription = ""
                                    taskText = ""
                                    snackbarHostState.showSnackbar(
                                        message = "Zadanie zostało dodane",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar(
                                        message = "Błąd podczas dodawania zadania",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            ) {
                Column {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Nazwa zadania") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Nazwa zadania",
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()

                            if (taskDate.isNotEmpty()) {
                                val parsedDate = LocalDate.parse(taskDate)
                                calendar.set(
                                    parsedDate.year,
                                    parsedDate.monthValue - 1,
                                    parsedDate.dayOfMonth
                                )
                            }

                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selected = LocalDate.of(year, month + 1, dayOfMonth)
                                    taskDate = selected.toString()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Wybierz datę",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (taskDate.isNotEmpty()) taskDate else "Wybierz datę",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = taskDescription,
                        onValueChange = { taskDescription = it },
                        label = { Text("Opis zadania") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Opis zadania",
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }

        // Dialog edycji zadania
        if (showEditDialog) {
            CustomModalDialog(
                onDismiss = { showEditDialog = false },
                title = "Edytuj zadanie",
                onConfirm = {
                    if (editTaskName.isBlank()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            snackbarHostState.showSnackbar(
                                message = "Nazwa zadania nie może być pusta",
                                duration = SnackbarDuration.Short
                            )
                        }
                        return@CustomModalDialog
                    }

                    editingTask?.let {
                        val updated = it.copy(
                            taskName = editTaskName,
                            realizeDate = editTaskDate,
                            description = editTaskDescription
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                taskService.updateTask(updated)
                                val refreshed = taskService.getTasksByDate(updated.plannerId, selectedDate.toString())
                                withContext(Dispatchers.Main) {
                                    taskList = refreshed
                                    showEditDialog = false
                                    snackbarHostState.showSnackbar(
                                        message = "Zadanie zostało zaktualizowane",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    snackbarHostState.showSnackbar(
                                        message = "Błąd podczas aktualizacji zadania",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        }
                    }
                }
            ) {
                Column {
                    OutlinedTextField(
                        value = editTaskName,
                        onValueChange = { editTaskName = it },
                        label = { Text("Nazwa zadania") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = "Nazwa zadania",
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            if (editTaskDate.isNotEmpty()) {
                                val parsedDate = LocalDate.parse(editTaskDate)
                                calendar.set(
                                    parsedDate.year,
                                    parsedDate.monthValue - 1,
                                    parsedDate.dayOfMonth
                                )
                            }
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val selected = LocalDate.of(year, month + 1, dayOfMonth)
                                    editTaskDate = selected.toString()
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Wybierz datę",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (editTaskDate.isNotEmpty()) editTaskDate else "Wybierz datę",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editTaskDescription,
                        onValueChange = { editTaskDescription = it },
                        label = { Text("Opis zadania") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Opis zadania",
                                tint = Color(0xFF4CAF50)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF4CAF50),
                            focusedLabelColor = Color(0xFF4CAF50)
                        )
                    )
                }
            }
        }

        // Dialog z informacjami o zadaniu
        if (showInfoDialog) {
            InfoModalDialog(
                onDismiss = { showInfoDialog = false },
                title = "Informacje o zadaniu"
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = "Nazwa:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text(text = infoTaskName, modifier = Modifier.padding(bottom = 8.dp))

                    Text(text = "Data:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text(text = infoTaskDate, modifier = Modifier.padding(bottom = 8.dp))

                    Text(text = "Opis:", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    Text(text = infoTaskDescription)
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun DaysOfMonth(
    month: YearMonth,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = (month.atDay(1).dayOfWeek.value + 6) % 7 // Poniedziałek = 0

    val daysList = buildList {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) {
            add(day)
        }
        val remaining = 7 - (size % 7)
        if (remaining < 7) {
            repeat(remaining) { add(null) }
        }
    }

    Column {
        // Nagłówki dni tygodnia
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
            }
        }

        // Dni miesiąca
        daysList.chunked(7).forEach { week ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (day != null && selectedDate == month.atDay(day)) {
                                    Modifier.background(Color(0xFF4CAF50))
                                } else {
                                    Modifier.background(Color.Transparent)
                                }
                            )
                            .let {
                                if (day != null) it.clickable { onDateSelected(month.atDay(day)) }
                                else it
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        day?.let {
                            Text(
                                text = it.toString(),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                color = if (selectedDate == month.atDay(day)) Color.White else Color(0xFF2E7D32),
                                fontWeight = if (selectedDate == month.atDay(day)) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEditTask: (Task) -> Unit,
    onInfoTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.taskName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32)
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        fontSize = 14.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row {
                IconButton(
                    onClick = { onInfoTask(task) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Informacje",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onEditTask(task) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edytuj",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteTask(task) },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
//@SuppressLint("NewApi")
//@Composable
//fun TaskList(
//    tasks: List<Task>,
//    onEditTask: (Task) -> Unit,
//    onInfoTask: (Task) -> Unit,
//    onDeleteTask: (Task) -> Unit
//) {
//    val editIcon = painterResource(id = R.drawable.baseline_edit)
//    val deleteIcon = painterResource(id = R.drawable.baseline_delete)
//    val infoIcon = painterResource(id = R.drawable.baseline_info)
//
//    if (tasks.isEmpty()) {
//        Text(text = "Brak zadań.")
//    } else {
//        LazyColumn {
//            items(tasks) { task ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 4.dp)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Text(
//                            text = task.taskName,
//                            fontSize = 16.sp,
//                            modifier = Modifier.weight(1f)
//                        )
//
//                        IconButton(onClick = { onEditTask(task) }) {
//                            Icon(painter = editIcon, contentDescription = "EDIT")
//                        }
//                        IconButton(onClick = { onDeleteTask(task) }) {
//                            Icon(painter = deleteIcon, contentDescription = "DELETE")
//                        }
//                        IconButton(onClick = { onInfoTask(task) }) {
//                            Icon(painter = infoIcon, contentDescription = "INFO")
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

