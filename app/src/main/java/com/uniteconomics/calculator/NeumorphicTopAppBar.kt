package com.uniteconomics.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Neumorphic Top App Bar with dynamic active screen icon & title matching bottom navigation
 * (Dashboard, Analysis, Projects, Glossary), Dark/Light mode theme toggle, and Kurdish Sorani / English language switcher.
 */
@Composable
fun NeumorphicTopAppBar(
    currentScreen: NavScreen = NavScreen.DASHBOARD,
    isKurdish: Boolean,
    onLanguageToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onThemeToggle: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    val screenTitle = when (currentScreen) {
        NavScreen.DASHBOARD -> if (isKurdish) "داشبۆردی ئامار" else "Executive Dashboard"
        NavScreen.ANALYSIS -> if (isKurdish) "شیکاری و ئامێر" else "Analysis & Machine"
        NavScreen.PROJECTS -> if (isKurdish) "پڕۆژە تۆمارکراوەکان" else "Saved Projects"
        NavScreen.GLOSSARY -> if (isKurdish) "فەرهەنگی دارایی" else "Financial Glossary"
    }

    val screenIcon = when (currentScreen) {
        NavScreen.DASHBOARD -> Icons.Default.GridView
        NavScreen.ANALYSIS -> Icons.Default.BarChart
        NavScreen.PROJECTS -> Icons.Outlined.Folder
        NavScreen.GLOSSARY -> Icons.AutoMirrored.Outlined.MenuBook
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .displayCutoutPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 20.dp,
                elevation = 4.dp
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        val isCompactWidth = maxWidth < 360.dp

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Dynamic Active Screen Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = 48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 20.dp,
                            elevation = 3.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = screenIcon,
                        contentDescription = null,
                        tint = neuColors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = screenTitle,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .semantics { heading() }
                )
            }

            // Actions: Theme Switcher & Language Toggle (Zero redundancy)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dark / Light Mode Theme Switcher Button (48dp touch target)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            indication = ripple(bounded = true, color = neuColors.primary),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onThemeToggle() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .neuConvex(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 19.dp,
                                elevation = 3.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) (if (isKurdish) "گۆڕین بۆ دۆخی ڕووناک" else "Switch to Light Mode")
                            else (if (isKurdish) "گۆڕین بۆ دۆخی تاریک" else "Switch to Dark Mode"),
                            tint = if (isDarkTheme) neuColors.warning else neuColors.textMain,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Kurdish Sorani / English Language Toggle Button (48dp touch target)
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 18.dp,
                            elevation = 3.dp
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .clickable(
                            role = Role.Button,
                            onClickLabel = if (isKurdish) "گۆڕینی زمان بۆ ئینگلیزی" else "Switch language to Kurdish",
                            indication = ripple(bounded = true, color = neuColors.primary),
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onLanguageToggle(!isKurdish) }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = neuColors.textMain,
                            modifier = Modifier.size(16.dp)
                        )
                        if (!isCompactWidth) {
                            Text(
                                text = if (isKurdish) "کوردی" else "ENG",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = neuColors.textMain
                            )
                        }
                    }
                }
            }
        }
    }
}
