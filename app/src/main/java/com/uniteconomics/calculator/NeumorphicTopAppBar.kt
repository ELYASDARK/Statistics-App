package com.uniteconomics.calculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.Folder

import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.graphics.Color

/**
 * Neumorphic Top App Bar with Kurdish Sorani / English language switcher toggle, Dark/Light mode theme toggle,
 * dictionary modal launcher, and dynamic active screen icon matching bottom navbar navigation (Dashboard, Analysis, Projects).
 */
@Composable
fun NeumorphicTopAppBar(
    currentScreen: NavScreen = NavScreen.DASHBOARD,
    isKurdish: Boolean,
    onLanguageToggle: (Boolean) -> Unit,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onThemeToggle: () -> Unit = {},
    onOpenDictionary: () -> Unit,
    onOpenProjects: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Dynamic Active Screen Icon (Dashboard vs Analysis vs Projects)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .then(
                        if (onOpenProjects != null) Modifier.clickable { onOpenProjects() } else Modifier
                    )
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
                        imageVector = when (currentScreen) {
                            NavScreen.DASHBOARD -> Icons.Default.GridView
                            NavScreen.ANALYSIS -> Icons.Default.BarChart
                            NavScreen.PROJECTS -> Icons.Outlined.Folder
                        },
                        contentDescription = "Active Screen Icon",
                        tint = neuColors.textMain,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = if (isKurdish) "ئامار" else "Statistics",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = neuColors.textMain
                )
            }

            // Actions: Language Toggle & Dictionary Launcher
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Dark / Light Mode Theme Switcher Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            cornerRadius = 18.dp,
                            elevation = 3.dp
                        )
                        .clickable { onThemeToggle() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = if (isDarkTheme) "Switch to Light Mode" else "Switch to Dark Mode",
                        tint = if (isDarkTheme) Color(0xFFFBBF24) else neuColors.textMain,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Kurdish Sorani / English Language Toggle Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .then(
                            if (isKurdish) {
                                Modifier.neuPressed(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 18.dp,
                                    elevation = 2.dp
                                )
                            } else {
                                Modifier.neuFlat(
                                    lightShadowColor = neuColors.shadowLight,
                                    darkShadowColor = neuColors.shadowDark,
                                    backgroundColor = neuColors.surface,
                                    cornerRadius = 18.dp,
                                    elevation = 3.dp
                                )
                            }
                        )
                        .clickable { onLanguageToggle(!isKurdish) }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language Switch",
                            tint = neuColors.textMain,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isKurdish) "کوردی" else "ENG",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textMain
                        )
                    }
                }

                // Dictionary Info Launcher Button with Text Label
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .neuConvex(
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            lightGradientColor = neuColors.shadowLight.copy(alpha = 0.6f),
                            darkGradientColor = neuColors.shadowDark.copy(alpha = 0.3f),
                            cornerRadius = 18.dp,
                            elevation = 3.dp
                        )
                        .clickable { onOpenDictionary() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = "Dictionary Info Modal",
                            tint = neuColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isKurdish) "فەرهەنگ" else "Terms",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = neuColors.textMuted
                        )
                    }
                }
            }
        }
    }
}
