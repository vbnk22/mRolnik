package com.example.mrolnik.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@SuppressLint("NewApi")
@Composable
fun PlannerScreen() {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Nawigacja miesiącami
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                Text("<")
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                fontSize = 20.sp
            )
            Button(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                Text(">")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dni miesiąca
        DaysOfMonth(currentMonth) { date ->
            selectedDate = date
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Zadania na wybrany dzień
        selectedDate?.let { date ->
            Text(
                text = "Zadania na ${date.dayOfMonth}/${date.monthValue}/${date.year}:",
                fontSize = 18.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            TaskList(date)
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun DaysOfMonth(month: YearMonth, onDateSelected: (LocalDate) -> Unit) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfWeek = (month.atDay(1).dayOfWeek.value + 6) % 7 // Poniedziałek = 0

    val daysList = buildList {
        repeat(firstDayOfWeek) { add(null) }
        for (day in 1..daysInMonth) {
            add(day)
        }
        // Uzupełnij ostatni tydzień pustymi polami jeśli potrzeba
        val remaining = 7 - (size % 7)
        if (remaining < 7) {
            repeat(remaining) { add(null) }
        }
    }

    // Nagłówki dni tygodnia
    Row(modifier = Modifier.fillMaxWidth()) {
        listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd").forEach { day ->
            Text(
                text = day,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            )
        }
    }

    // Siatka dni
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
                        .aspectRatio(1f) // ładne kwadraciki
                        .let {
                            if (day != null) it.clickable { onDateSelected(month.atDay(day)) }
                            else it
                        },
                    contentAlignment = Alignment.Center
                ) {
                    day?.let {
                        Text(
                            text = it.toString(),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun TaskList(date: LocalDate) {
    // Przykładowe zadania na podstawie daty
    val tasks = listOf(
        "Spotkanie o 10:00",
        "Zakupy spożywcze",
        "Trening o 18:00",
        "Napisanie raportu"
    ).filterIndexed { index, _ -> (date.dayOfMonth + index) % 2 == 0 } // losowy filtr

    if (tasks.isEmpty()) {
        Text(text = "Brak zadań.")
    } else {
        LazyColumn {
            items(tasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Text(
                        text = task,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}