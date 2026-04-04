package com.ileader.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ileader.app.R
import com.ileader.app.data.models.UserRole
import com.ileader.app.ui.theme.DarkAppColors
import com.ileader.app.ui.theme.ILeaderColors
import com.ileader.app.ui.theme.LocalAppColors

@Composable
fun ILeaderLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
    titleSize: Float = 42f,
    subtitleSize: Float = 14f,
    showSubtitle: Boolean = true,
    showIconBackground: Boolean = false
) {
    val colors = LocalAppColors.current
    val isDark = colors.bg == DarkAppColors.bg
    val logoRes = if (isDark) R.drawable.logo_dark else R.drawable.logo_light

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = logoRes),
            contentDescription = "iLeader",
            modifier = Modifier.size(iconSize)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "iLeader",
            fontSize = titleSize.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.textPrimary,
            letterSpacing = (-1).sp
        )

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Спортивная платформа",
                fontSize = subtitleSize.sp,
                fontWeight = FontWeight.Normal,
                color = colors.textSecondary,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun ILeaderBrandHeader(
    role: UserRole,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    val roleIcon = getRoleIcon(role)
    val roleLabel = role.displayName

    val dotAlpha = 1f

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(
                        listOf(ILeaderColors.PrimaryRed, ILeaderColors.DarkRed)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = roleIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = ILeaderColors.PrimaryRed, fontWeight = FontWeight.Black)) {
                        append("i")
                    }
                    withStyle(SpanStyle(color = colors.textPrimary, fontWeight = FontWeight.Black)) {
                        append("Leader")
                    }
                },
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            )

            Spacer(Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ILeaderColors.PrimaryRed.copy(alpha = dotAlpha))
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = roleLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun getRoleIcon(role: UserRole): ImageVector = when (role) {
    UserRole.ATHLETE -> Icons.Default.EmojiEvents
    UserRole.TRAINER -> Icons.Default.Groups
    UserRole.ORGANIZER -> Icons.Default.EmojiEvents
    UserRole.REFEREE -> Icons.Default.Shield
    UserRole.SPONSOR -> Icons.Default.AttachMoney
    UserRole.MEDIA -> Icons.Default.Newspaper
    UserRole.CONTENT_MANAGER -> Icons.Default.Edit
    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
    UserRole.USER -> Icons.Default.Person
}
