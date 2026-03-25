package com.ileader.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.ui.theme.AppColorScheme
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors
import com.ileader.app.ui.theme.cardShadow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.ileader.app.data.preferences.ThemePreference
import com.ileader.app.ui.theme.ThemeMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

val LocalSnackbarHost = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState") }

// ══════════════════════════════════════════════════════════
// BACKWARD-COMPATIBLE ACCESSOR
// Old code used `DarkTheme.Bg`, `DarkTheme.CardBg`, etc.
// Now `DarkTheme` is a composable-read proxy to LocalAppColors.
// ══════════════════════════════════════════════════════════

object DarkTheme {
    val Bg: Color @Composable get() = LocalAppColors.current.bg
    val BgSecondary: Color @Composable get() = LocalAppColors.current.bgSecondary
    val CardBg: Color @Composable get() = LocalAppColors.current.cardBg
    val CardBorder: Color @Composable get() = LocalAppColors.current.border
    val TextPrimary: Color @Composable get() = LocalAppColors.current.textPrimary
    val TextSecondary: Color @Composable get() = LocalAppColors.current.textSecondary
    val TextMuted: Color @Composable get() = LocalAppColors.current.textMuted
    val Accent: Color @Composable get() = LocalAppColors.current.accent
    val AccentDark: Color @Composable get() = LocalAppColors.current.accentDark
    val AccentSoft: Color @Composable get() = LocalAppColors.current.accentSoft

    val isDark: Boolean @Composable get() = LocalAppColors.current.bg == DarkAppColors.bg

    val cardBorderStroke: BorderStroke
        @Composable get() {
            val border = LocalAppColors.current.border
            return BorderStroke(
                0.5.dp,
                Brush.linearGradient(listOf(border.copy(alpha = 0.6f), border.copy(alpha = 0.2f)))
            )
        }
}

// ══════════════════════════════════════════════════════════
// ══════════════════════════════════════════════════════════
// SPORT ICON HELPER
// ══════════════════════════════════════════════════════════

fun sportEmoji(sportName: String) = when (sportName.lowercase().trim()) {
    "картинг", "karting"                          -> "🏎️"
    "стрельба", "shooting"                        -> "🎯"
    "теннис", "tennis"                            -> "🎾"
    "футбол", "football", "soccer"                -> "⚽"
    "бокс", "boxing"                              -> "🥊"
    "плавание", "swimming"                        -> "🏊"
    "лёгкая атлетика", "легкая атлетика",
    "athletics", "track and field"                -> "🏃"
    "гребля", "rowing"                            -> "🚣"
    else                                          -> "🏆"
}

fun sportIcon(sportName: String) = when (sportName.lowercase().trim()) {
    "картинг", "karting"                     -> Icons.Default.DirectionsCar
    "стрельба", "shooting"                   -> Icons.Default.GpsFixed
    "теннис", "tennis"                       -> Icons.Default.SportsTennis
    "футбол", "football", "soccer"           -> Icons.Default.SportsSoccer
    "бокс", "boxing"                         -> Icons.Default.SportsMartialArts
    "плавание", "swimming"                   -> Icons.Default.Pool
    "лёгкая атлетика", "легкая атлетика",
    "athletics", "track and field"           -> Icons.Default.DirectionsRun
    "гребля", "rowing"                       -> Icons.Default.Rowing
    else                                     -> Icons.Default.EmojiEvents
}

// CARDS
// ══════════════════════════════════════════════════════════

@Composable
fun DarkCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cardShadow(isDark),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else BorderStroke(
            0.5.dp,
            colors.border.copy(alpha = 0.3f)
        )
    ) {
        content()
    }
}

@Composable
fun DarkCardPadded(
    modifier: Modifier = Modifier,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cardShadow(isDark),
        shape = RoundedCornerShape(16.dp),
        color = colors.cardBg,
        border = if (isDark) DarkTheme.cardBorderStroke else BorderStroke(
            0.5.dp,
            colors.border.copy(alpha = 0.3f)
        )
    ) {
        Column(Modifier.padding(padding)) {
            content()
        }
    }
}

// ══════════════════════════════════════════════════════════
// ANIMATIONS
// ══════════════════════════════════════════════════════════

@Composable
fun FadeIn(visible: Boolean, delayMs: Int, content: @Composable () -> Unit) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(visible) { if (visible) { delay(delayMs.toLong()); show = true } }
    val alpha by animateFloatAsState(if (show) 1f else 0f, tween(350), label = "fi$delayMs")
    val offset by animateDpAsState(if (show) 0.dp else 20.dp, tween(350), label = "fo$delayMs")
    Column(Modifier.offset(y = offset).alpha(alpha)) { content() }
}

// ══════════════════════════════════════════════════════════
// HEADERS
// ══════════════════════════════════════════════════════════

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    val colors = LocalAppColors.current
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            letterSpacing = (-0.3).sp
        )
        if (action != null && onAction != null) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onAction() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(action, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = colors.accent)
                Spacer(Modifier.width(2.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                    tint = colors.accent, modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun BackHeader(title: String, onBack: () -> Unit, extra: @Composable RowScope.() -> Unit = {}) {
    val colors = LocalAppColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.cardBg)
                .border(0.5.dp, colors.border.copy(alpha = 0.5f), CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack, "Назад",
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
            letterSpacing = (-0.3).sp
        )
        extra()
    }
}

// ══════════════════════════════════════════════════════════
// STAT CARDS
// ══════════════════════════════════════════════════════════

@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    Surface(
        modifier = modifier.cardShadow(isDark),
        shape = RoundedCornerShape(14.dp),
        color = colors.cardBg
    ) {
        Row(
            Modifier
                .then(
                    if (isDark) Modifier.border(0.5.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    else Modifier
                )
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentSoft),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = colors.accent, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary,
                    letterSpacing = (-0.3).sp
                )
                Text(label, fontSize = 11.sp, color = colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun MiniStat(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    Surface(
        modifier = modifier.cardShadow(isDark),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBg
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .then(
                    if (isDark) Modifier.border(0.5.dp, colors.border.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    else Modifier
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                letterSpacing = (-0.3).sp
            )
            Text(label, fontSize = 11.sp, color = colors.textMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

// ══════════════════════════════════════════════════════════
// ICON CONTAINERS
// ══════════════════════════════════════════════════════════

@Composable
fun AccentIconBox(
    icon: ImageVector,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(colors.accent, colors.accentDark))),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun SoftIconBox(
    icon: ImageVector,
    size: Dp = 40.dp,
    iconSize: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.accentSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = colors.accent, modifier = Modifier.size(iconSize))
    }
}

// ══════════════════════════════════════════════════════════
// INPUTS
// ══════════════════════════════════════════════════════════

@Composable
fun DarkSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "Поиск...",
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBg
    ) {
        Row(
            Modifier
                .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = colors.textMuted, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Box(Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(placeholder, color = colors.textMuted, fontSize = 14.sp)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.sp),
                    singleLine = true,
                    cursorBrush = SolidColor(colors.accent)
                )
            }
        }
    }
}

@Composable
fun DarkFormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
    error: String? = null,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    val colors = LocalAppColors.current
    var isFocused by remember { mutableStateOf(false) }
    val borderColor = when {
        error != null -> ILeaderColors.Error
        isFocused -> colors.accent.copy(alpha = 0.5f)
        else -> colors.border
    }

    Column(modifier) {
        if (label.isNotEmpty()) {
            Text(
                label,
                fontSize = 13.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(6.dp))
        }
        Surface(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = colors.cardBg
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .onFocusChanged { isFocused = it.isFocused },
                textStyle = TextStyle(color = colors.textPrimary, fontSize = 14.sp),
                singleLine = singleLine,
                minLines = minLines,
                cursorBrush = SolidColor(colors.accent),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(placeholder, color = colors.textMuted, fontSize = 14.sp)
                        }
                        inner()
                    }
                }
            )
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = ILeaderColors.Error, fontSize = 11.sp)
        }
    }
}

@Composable
fun FieldLabel(label: String, content: @Composable () -> Unit) {
    val colors = LocalAppColors.current
    Column {
        Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

// ══════════════════════════════════════════════════════════
// FILTER CHIPS
// ══════════════════════════════════════════════════════════

@Composable
fun DarkFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = { Text(text, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.accent,
            selectedLabelColor = Color.White,
            containerColor = colors.cardBg,
            labelColor = colors.textSecondary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = colors.border,
            selectedBorderColor = Color.Transparent,
            enabled = true,
            selected = selected
        ),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
fun DarkSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.cardBg
    ) {
        Row(
            modifier = Modifier
                .border(0.5.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            options.forEachIndexed { index, label ->
                Surface(
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(9.dp),
                    color = if (selectedIndex == index) colors.accent else Color.Transparent
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selectedIndex == index) Color.White else colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// BADGES
// ══════════════════════════════════════════════════════════

@Composable
fun StatusBadge(text: String, color: Color = LocalAppColors.current.textSecondary) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.10f)
    ) {
        Text(
            text,
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun RoleBadge(role: com.ileader.app.data.models.UserRole) {
    val color = when (role) {
        com.ileader.app.data.models.UserRole.ATHLETE -> ILeaderColors.AthleteColor
        com.ileader.app.data.models.UserRole.TRAINER -> ILeaderColors.TrainerColor
        com.ileader.app.data.models.UserRole.ORGANIZER -> ILeaderColors.OrganizerColor
        com.ileader.app.data.models.UserRole.REFEREE -> ILeaderColors.RefereeColor
        com.ileader.app.data.models.UserRole.SPONSOR -> ILeaderColors.SponsorColor
        com.ileader.app.data.models.UserRole.MEDIA -> ILeaderColors.MediaColor
        com.ileader.app.data.models.UserRole.ADMIN -> ILeaderColors.AdminColor
        com.ileader.app.data.models.UserRole.USER -> ILeaderColors.ViewerColor
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.10f)
    ) {
        Text(
            role.displayName,
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

// ══════════════════════════════════════════════════════════
// EMPTY / INFO STATES
// ══════════════════════════════════════════════════════════

@Composable
fun EmptyState(
    title: String,
    subtitle: String = "Данные появятся позже",
    icon: ImageVector = Icons.Default.SearchOff,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = LocalAppColors.current
    DarkCard {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.border.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon, null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                fontSize = 12.sp,
                color = colors.textMuted,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent)
                ) {
                    Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SuccessBanner(text: String) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = ILeaderColors.Success.copy(alpha = 0.12f)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = ILeaderColors.Success, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, color = ILeaderColors.Success, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Composable
fun InfoBanner(text: String) {
    val colors = LocalAppColors.current
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = colors.accent.copy(alpha = 0.08f)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, null, tint = colors.accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 13.sp, color = colors.textSecondary, lineHeight = 18.sp)
        }
    }
}

// ══════════════════════════════════════════════════════════
// PROGRESS
// ══════════════════════════════════════════════════════════

@Composable
fun DarkProgressBar(progress: Float, modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(colors.border)
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(3.dp))
                .background(colors.textMuted)
        )
    }
}

// ══════════════════════════════════════════════════════════
// SHIMMER / SKELETON LOADING
// ══════════════════════════════════════════════════════════

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmer"
    )
    val colors = LocalAppColors.current
    return Brush.linearGradient(
        colors = listOf(
            colors.cardBg,
            colors.border.copy(alpha = 0.3f),
            colors.cardBg
        ),
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )
}

@Composable
fun ShimmerBox(modifier: Modifier) {
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(shimmerBrush()))
}

enum class LoadingVariant { SPINNER, DASHBOARD, LIST, DETAIL }

// ══════════════════════════════════════════════════════════
// FULL-SCREEN STATES
// ══════════════════════════════════════════════════════════

@Composable
fun LoadingScreen(variant: LoadingVariant = LoadingVariant.SPINNER) {
    when (variant) {
        LoadingVariant.SPINNER -> {
            val colors = LocalAppColors.current
            Box(
                Modifier.fillMaxSize().background(colors.bg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colors.accent, strokeWidth = 3.dp)
            }
        }
        LoadingVariant.DASHBOARD -> SkeletonDashboard()
        LoadingVariant.LIST -> SkeletonList()
        LoadingVariant.DETAIL -> SkeletonDetail()
    }
}

@Composable
private fun SkeletonDashboard() {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // 3 stat cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                ShimmerBox(Modifier.weight(1f).height(80.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        // Hero card
        ShimmerBox(Modifier.fillMaxWidth().height(200.dp))
        Spacer(Modifier.height(20.dp))
        // Section title
        ShimmerBox(Modifier.width(140.dp).height(20.dp))
        Spacer(Modifier.height(12.dp))
        // 2 small cards
        repeat(2) {
            ShimmerBox(Modifier.fillMaxWidth().height(100.dp))
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SkeletonList() {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        repeat(5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerBrush())
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
                    ShimmerBox(Modifier.fillMaxWidth(0.6f).height(16.dp))
                    ShimmerBox(Modifier.fillMaxWidth(0.4f).height(12.dp))
                    ShimmerBox(Modifier.fillMaxWidth(0.8f).height(12.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SkeletonDetail() {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        // Header: circle + tall box
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ShimmerBox(Modifier.size(40.dp).clip(CircleShape))
            ShimmerBox(Modifier.weight(1f).height(40.dp))
        }
        Spacer(Modifier.height(20.dp))
        // 3 info chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                ShimmerBox(Modifier.weight(1f).height(36.dp))
            }
        }
        Spacer(Modifier.height(20.dp))
        // 2 section cards
        repeat(2) {
            ShimmerBox(Modifier.fillMaxWidth().height(160.dp))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    val colors = LocalAppColors.current
    Box(
        Modifier.fillMaxSize().background(colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline, null,
                modifier = Modifier.size(48.dp),
                tint = colors.textMuted
            )
            Spacer(Modifier.height(16.dp))
            Text(
                message,
                color = colors.textSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Повторить")
            }
        }
    }
}

@Composable
fun NotFoundScreen(message: String, onBack: () -> Unit) {
    val colors = LocalAppColors.current
    Box(Modifier.fillMaxSize().background(colors.bg)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            BackHeader("", onBack)
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState(message)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Вернуться к списку")
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// WIZARD / FORM COMPONENTS
// ═══════════════════════════════════════════════════════════

@Composable
fun FormDropdown(
    label: String,
    selectedValue: String,
    placeholder: String,
    items: List<Pair<String, String>>,
    onItemSelected: (String) -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    val displayText = items.find { it.first == selectedValue }?.second ?: ""
    val borderColor = if (error != null) ILeaderColors.Error else colors.border

    Column(modifier) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
        }
        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = colors.cardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        displayText.ifEmpty { placeholder },
                        color = if (displayText.isEmpty()) colors.textMuted else colors.textPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null, Modifier.size(20.dp), colors.textMuted)
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colors.cardBg)
            ) {
                items.forEach { (value, display) ->
                    DropdownMenuItem(
                        text = { Text(display, fontSize = 14.sp, color = colors.textPrimary) },
                        onClick = { onItemSelected(value); expanded = false },
                        leadingIcon = if (value == selectedValue) {
                            { Icon(Icons.Default.Check, null, tint = colors.accent, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = ILeaderColors.Error, fontSize = 11.sp)
        }
    }
}

@Composable
fun DarkSwitchField(
    label: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.cardBg, RoundedCornerShape(12.dp))
            .border(0.5.dp, colors.border, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            if (description != null) {
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 12.sp, color = colors.textMuted, lineHeight = 16.sp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.accent,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.border
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "ГГГГ-ММ-ДД",
    error: String? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var showPicker by remember { mutableStateOf(false) }
    val borderColor = if (error != null) ILeaderColors.Error else colors.border

    Column(modifier) {
        if (label.isNotEmpty()) {
            Text(label, fontSize = 13.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(6.dp))
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showPicker = true },
            shape = RoundedCornerShape(12.dp),
            color = colors.cardBg
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CalendarToday, null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    if (value.isNotEmpty()) value else placeholder,
                    color = if (value.isNotEmpty()) colors.textPrimary else colors.textMuted,
                    fontSize = 14.sp
                )
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, color = ILeaderColors.Error, fontSize = 11.sp)
        }
    }

    if (showPicker) {
        val initialMillis = if (value.isNotEmpty()) {
            try {
                val parts = value.split("-")
                val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt(), 0, 0, 0)
                cal.timeInMillis
            } catch (_: Exception) { null }
        } else null

        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.timeInMillis = millis
                        val formatted = "%04d-%02d-%02d".format(
                            cal.get(java.util.Calendar.YEAR),
                            cal.get(java.util.Calendar.MONTH) + 1,
                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                        )
                        onValueChange(formatted)
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Отмена") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun DarkToggleCard(
    title: String,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.accentSoft else colors.cardBg,
        border = BorderStroke(
            if (selected) 1.5.dp else 0.5.dp,
            if (selected) colors.accent else colors.border
        )
    ) {
        Column(Modifier.padding(14.dp)) {
            Icon(
                icon, null,
                tint = if (selected) colors.accent else colors.textMuted,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (selected) colors.textPrimary else colors.textSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(description, fontSize = 11.sp, color = colors.textMuted, lineHeight = 14.sp)
        }
    }
}

@Composable
fun WizardStepIndicator(
    steps: List<Pair<String, ImageVector>>,
    currentStep: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (title, icon) ->
            if (index > 0) {
                Box(
                    Modifier
                        .width(12.dp)
                        .height(1.dp)
                        .background(if (index <= currentStep) colors.accent else colors.border)
                )
            }
            val isActive = index == currentStep
            val isDone = index < currentStep

            Surface(
                modifier = Modifier.clickable(enabled = isDone) { onStepClick(index) },
                shape = RoundedCornerShape(10.dp),
                color = when {
                    isActive -> colors.accent
                    isDone -> colors.accentSoft
                    else -> colors.cardBg
                },
                border = if (!isActive && !isDone) BorderStroke(0.5.dp, colors.border) else null
            ) {
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isDone) Icons.Default.Check else icon,
                        null,
                        tint = when {
                            isActive -> Color.White
                            isDone -> colors.accent
                            else -> colors.textMuted
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = when {
                            isActive -> Color.White
                            isDone -> colors.accent
                            else -> colors.textMuted
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicListField(
    label: String,
    items: List<String>,
    onItemsChange: (List<String>) -> Unit,
    placeholder: String = "Добавить...",
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    var inputValue by remember { mutableStateOf("") }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = colors.textSecondary)

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.cardBg, RoundedCornerShape(10.dp))
                    .border(0.5.dp, colors.border, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item, Modifier.weight(1f), fontSize = 13.sp, color = colors.textPrimary)
                IconButton(
                    onClick = { onItemsChange(items.toMutableList().also { it.removeAt(index) }) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.Close, "Удалить", tint = colors.textMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DarkFormField(
                label = "",
                value = inputValue,
                onValueChange = { inputValue = it },
                placeholder = placeholder,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (inputValue.isNotBlank()) {
                        onItemsChange(items + inputValue.trim())
                        inputValue = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier.height(44.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════
// THEME SWITCHER
// ══════════════════════════════════════════════════════════

@Composable
fun ThemeSwitcherCard() {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val themePref = remember { ThemePreference(context) }
    val currentTheme by themePref.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val scope = rememberCoroutineScope()

    DarkCard {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SoftIconBox(Icons.Default.Palette)
                Spacer(Modifier.width(12.dp))
                Text("Тема оформления", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                ThemeSwitcherOption(Modifier.weight(1f), "Светлая", Icons.Default.LightMode, currentTheme == ThemeMode.LIGHT) {
                    scope.launch { themePref.setThemeMode(ThemeMode.LIGHT) }
                }
                ThemeSwitcherOption(Modifier.weight(1f), "Тёмная", Icons.Default.DarkMode, currentTheme == ThemeMode.DARK) {
                    scope.launch { themePref.setThemeMode(ThemeMode.DARK) }
                }
                ThemeSwitcherOption(Modifier.weight(1f), "Система", Icons.Default.SettingsBrightness, currentTheme == ThemeMode.SYSTEM) {
                    scope.launch { themePref.setThemeMode(ThemeMode.SYSTEM) }
                }
            }
        }
    }
}

@Composable
private fun ThemeSwitcherOption(modifier: Modifier, label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.accentSoft else colors.border.copy(alpha = 0.3f),
        border = if (selected) BorderStroke(1.5.dp, colors.accent) else null
    ) {
        Column(
            Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, Modifier.size(20.dp), if (selected) colors.accent else colors.textMuted)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = if (selected) colors.accent else colors.textSecondary)
        }
    }
}
