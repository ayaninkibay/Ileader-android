package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.CourseListItem
import com.ileader.app.ui.viewmodels.CoursesListViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun CoursesListScreen(
    user: User,
    onNavigateToDetail: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val viewModel: CoursesListViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load() }
        is UiState.Success -> {
            val data = s.data
            var searchQuery by remember { mutableStateOf("") }
            var sportFilter by remember { mutableIntStateOf(0) }
            var priceFilter by remember { mutableIntStateOf(0) }
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            val sportFilters = remember(data.sports) {
                buildList {
                    add("all" to "Все виды")
                    data.sports.forEach { add(it.id to it.name) }
                }
            }
            val priceFilters = listOf("all" to "Все", "free" to "Бесплатные", "paid" to "Платные")

            val filteredCourses = remember(searchQuery, sportFilter, priceFilter, data.courses) {
                val sportValue = sportFilters.getOrNull(sportFilter)?.first ?: "all"
                val priceValue = priceFilters.getOrNull(priceFilter)?.first ?: "all"
                data.courses.filter { item ->
                    val c = item.course
                    (sportValue == "all" || c.sportId == sportValue) &&
                    (priceValue == "all" || (priceValue == "free" && c.isFree) || (priceValue == "paid" && !c.isFree)) &&
                    (searchQuery.isBlank() || c.title.contains(searchQuery, true) || (c.description ?: "").contains(searchQuery, true))
                }
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                if (onBack != null) {
                    BackHeader(title = "Академия", onBack = onBack)
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Академия",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Обучающие курсы и материалы",
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                        UserAvatar(
                            avatarUrl = user.avatarUrl,
                            displayName = user.displayName
                        )
                    }
                }
                

                Spacer(Modifier.height(20.dp))

                DarkSearchField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = "Поиск по названию, описанию..."
                )
                

                Spacer(Modifier.height(12.dp))

                Column {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priceFilters.forEachIndexed { index, (_, label) ->
                            DarkFilterChip(
                                text = label,
                                selected = priceFilter == index,
                                onClick = { priceFilter = index }
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sportFilters.forEachIndexed { index, (_, label) ->
                            DarkFilterChip(
                                text = label,
                                selected = sportFilter == index,
                                onClick = { sportFilter = index }
                            )
                        }
                    }
                }
                

                Spacer(Modifier.height(12.dp))

                Column {
                    Text(
                        "Найдено: ${filteredCourses.size} курсов",
                        fontSize = 13.sp,
                        color = TextMuted
                    )

                    Spacer(Modifier.height(12.dp))

                    if (filteredCourses.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.School,
                            title = "Курсы не найдены",
                            subtitle = "Попробуйте изменить фильтры поиска"
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            filteredCourses.forEach { item ->
                                CourseCard(
                                    item = item,
                                    onClick = { onNavigateToDetail(item.course.id) }
                                )
                            }
                        }
                    }
                }
                

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun CourseCard(
    item: CourseListItem,
    onClick: () -> Unit
) {
    val course = item.course

    DarkCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Cover image
            if (!course.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = course.coverUrl,
                    contentDescription = course.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.padding(16.dp)) {
                // Sport badge + price
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (course.sports?.name != null) {
                        StatusBadge(
                            text = course.sports.name,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = if (course.isFree) "Бесплатно" else "${course.price?.toInt() ?: 0} ${course.currency ?: "KZT"}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (course.isFree) Color(0xFF22C55E) else Accent
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Title
                Text(
                    text = course.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (!course.description.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = course.description,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Stats row
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Author
                    if (course.profiles?.name != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextMuted
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                course.profiles.name,
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Lessons count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextMuted
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${item.lessonsCount} уроков",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    // Students count
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextMuted
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${item.studentsCount}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}
