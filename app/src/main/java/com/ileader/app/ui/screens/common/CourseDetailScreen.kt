package com.ileader.app.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ileader.app.data.models.User
import com.ileader.app.data.remote.UiState
import com.ileader.app.data.remote.dto.CourseLessonDto
import com.ileader.app.ui.components.*
import com.ileader.app.ui.viewmodels.CourseDetailViewModel

private val Bg: Color @Composable get() = DarkTheme.Bg
private val CardBg: Color @Composable get() = DarkTheme.CardBg
private val CardBorder: Color @Composable get() = DarkTheme.CardBorder
private val TextPrimary: Color @Composable get() = DarkTheme.TextPrimary
private val TextSecondary: Color @Composable get() = DarkTheme.TextSecondary
private val TextMuted: Color @Composable get() = DarkTheme.TextMuted
private val Accent: Color @Composable get() = DarkTheme.Accent
private val AccentSoft: Color @Composable get() = DarkTheme.AccentSoft

@Composable
fun CourseDetailScreen(
    courseId: String,
    user: User,
    onBack: () -> Unit
) {
    val viewModel: CourseDetailViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val enrollState by viewModel.enrollState.collectAsState()

    LaunchedEffect(courseId) { viewModel.load(courseId, user.id) }

    when (val s = state) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(s.message) { viewModel.load(courseId, user.id) }
        is UiState.Success -> {
            val data = s.data
            val course = data.course
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }

            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                // Cover image
                if (!course.coverUrl.isNullOrBlank()) {
                    Box {
                        AsyncImage(
                            model = course.coverUrl,
                            contentDescription = course.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentScale = ContentScale.Crop
                        )
                        // Back button overlay
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = Color.White
                            )
                        }
                    }
                } else {
                    FadeIn(visible, 0) {
                        BackHeader(title = "Курс", onBack = onBack)
                    }
                }

                Column(Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(16.dp))

                    // Sport + Price
                    FadeIn(visible, 0) {
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
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (course.isFree) Color(0xFF22C55E) else Accent
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Title
                    FadeIn(visible, 100) {
                        Text(
                            text = course.title,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    // Author
                    if (course.profiles?.name != null) {
                        Spacer(Modifier.height(8.dp))
                        FadeIn(visible, 150) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = TextMuted
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Автор: ${course.profiles.name}",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats row
                    FadeIn(visible, 200) {
                        DarkCard {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    icon = Icons.Default.MenuBook,
                                    value = "${data.lessonsCount}",
                                    label = "Уроков"
                                )
                                StatItem(
                                    icon = Icons.Default.People,
                                    value = "${data.studentsCount}",
                                    label = "Студентов"
                                )
                                StatItem(
                                    icon = Icons.Default.Schedule,
                                    value = formatTotalDuration(data.lessons),
                                    label = "Длительность"
                                )
                            }
                        }
                    }

                    // Description
                    if (!course.description.isNullOrBlank()) {
                        Spacer(Modifier.height(20.dp))
                        FadeIn(visible, 250) {
                            Column {
                                SectionHeader(title = "Описание")
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = course.description,
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Enroll button
                    FadeIn(visible, 300) {
                        if (data.hasAccess) {
                            DarkCard {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "У вас есть доступ к этому курсу",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF22C55E)
                                    )
                                }
                            }
                        } else if (course.isFree) {
                            Button(
                                onClick = { viewModel.enroll(user.id, courseId) },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent),
                                enabled = enrollState !is UiState.Loading
                            ) {
                                if (enrollState is UiState.Loading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Записаться бесплатно",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Button(
                                onClick = { /* Paid courses — not implemented in mobile */ },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Accent)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Получить доступ — ${course.price?.toInt() ?: 0} ${course.currency ?: "KZT"}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Show enroll error
                    if (enrollState is UiState.Error) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            (enrollState as? UiState.Error)?.message ?: "Ошибка записи",
                            fontSize = 13.sp,
                            color = Accent
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Lessons list
                    FadeIn(visible, 400) {
                        Column {
                            SectionHeader(title = "Уроки (${data.lessons.size})")
                            Spacer(Modifier.height(12.dp))

                            if (data.lessons.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.MenuBook,
                                    title = "Уроки пока не добавлены",
                                    subtitle = "Автор скоро добавит учебные материалы"
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    data.lessons.forEachIndexed { index, lesson ->
                                        LessonCard(
                                            lesson = lesson,
                                            index = index + 1,
                                            hasAccess = data.hasAccess
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Accent
        )
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun LessonCard(
    lesson: CourseLessonDto,
    index: Int,
    hasAccess: Boolean
) {
    val isLocked = !hasAccess && !lesson.isFreePreview

    DarkCard {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lesson number
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isLocked) CardBorder else AccentSoft),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TextMuted
                    )
                } else {
                    Text(
                        "$index",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = lesson.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLocked) TextMuted else TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Meta info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (lesson.durationMinutes != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = TextMuted
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "${lesson.durationMinutes} мин",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Content type icons
                    if (lesson.videoUrl != null) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = "Видео",
                            modifier = Modifier.size(14.dp),
                            tint = TextMuted
                        )
                    }
                    if (lesson.audioUrl != null) {
                        Icon(
                            Icons.Default.Headphones,
                            contentDescription = "Аудио",
                            modifier = Modifier.size(14.dp),
                            tint = TextMuted
                        )
                    }
                    if (lesson.textContent != null) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = "Текст",
                            modifier = Modifier.size(14.dp),
                            tint = TextMuted
                        )
                    }

                    if (lesson.isFreePreview && !hasAccess) {
                        Text(
                            "Бесплатно",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF22C55E)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTotalDuration(lessons: List<CourseLessonDto>): String {
    val totalMinutes = lessons.mapNotNull { it.durationMinutes }.sum()
    return if (totalMinutes >= 60) {
        "${totalMinutes / 60}ч ${totalMinutes % 60}м"
    } else {
        "${totalMinutes} мин"
    }
}
