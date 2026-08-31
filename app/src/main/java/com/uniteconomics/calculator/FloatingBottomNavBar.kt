package com.uniteconomics.calculator

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen enumeration for multi-screen layout navigation.
 */
enum class NavScreen {
    DASHBOARD,
    ANALYSIS,
    PROJECTS,
    GLOSSARY
}

/**
 * Symmetrical Neumorphic Floating Bottom Navigation Bar:
 * - Left Group: Dashboard & Analysis tabs (equal weights)
 * - Center: Upper-floating prominent circular Neumorphic Convex (+) Plus Action Button with dedicated clearance
 * - Right Group: Projects & Glossary screens (equal weights)
 * - Vertical stacked item anatomy (icon top, label bottom) preventing text clipping and collisions
 * - Tactile Neumorphic design system with crisp light/dark shadows and primary accent indication
 */
@Composable
fun FloatingBottomNavBar(
    currentScreen: NavScreen,
    onScreenSelected: (NavScreen) -> Unit,
    onOpenSaveModal: () -> Unit,
    isKurdish: Boolean = true,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating NeuFlat Navigation Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .heightIn(min = 64.dp)
                .neuFlat(
                    lightShadowColor = neuColors.shadowLight,
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    cornerRadius = 32.dp,
                    elevation = 4.dp
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Dashboard (Left 1)
                NavItem(
                    label = if (isKurdish) "داشبۆرد" else "Dashboard",
                    icon = Icons.Default.Dashboard,
                    isSelected = currentScreen == NavScreen.DASHBOARD,
                    onClick = { onScreenSelected(NavScreen.DASHBOARD) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 2: Analysis (Left 2)
                NavItem(
                    label = if (isKurdish) "شیکاری" else "Analysis",
                    icon = Icons.Default.Analytics,
                    isSelected = currentScreen == NavScreen.ANALYSIS,
                    onClick = { onScreenSelected(NavScreen.ANALYSIS) },
                    modifier = Modifier.weight(1f)
                )

                // Center Spacer for Upper Floating Plus Button (74dp gap prevents any collision)
                Spacer(modifier = Modifier.width(74.dp))

                // Tab 3: Projects (Right 1)
                NavItem(
                    label = if (isKurdish) "پڕۆژەکان" else "Projects",
                    icon = Icons.Default.Folder,
                    isSelected = currentScreen == NavScreen.PROJECTS,
                    onClick = { onScreenSelected(NavScreen.PROJECTS) },
                    modifier = Modifier.weight(1f)
                )

                // Tab 4: Glossary (Right 2)
                NavItem(
                    label = if (isKurdish) "فەرهەنگ" else "Glossary",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    isSelected = currentScreen == NavScreen.GLOSSARY,
                    onClick = { onScreenSelected(NavScreen.GLOSSARY) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val centerInteractionSource = remember { MutableInteractionSource() }
        val isCenterPressed by centerInteractionSource.collectIsPressedAsState()

        // CENTER HERO ACTION: Prominent Upper Floating Circular Neumorphic Convex (+) Plus Button (Elevated Offset)
        Box(
            modifier = Modifier
                .offset(y = (-16).dp)
                .size(56.dp)
                .then(
                    if (isCenterPressed) {
                        Modifier.neuPressed(
                            cornerShape = CircleShape,
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            elevation = 2.dp
                        )
                    } else {
                        Modifier.neuConvex(
                            cornerShape = CircleShape,
                            lightShadowColor = neuColors.shadowLight,
                            darkShadowColor = neuColors.shadowDark,
                            backgroundColor = neuColors.surface,
                            lightGradientColor = neuColors.primary.copy(alpha = 0.18f),
                            darkGradientColor = neuColors.primary.copy(alpha = 0.06f),
                            elevation = 5.dp
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .clickable(
                        role = Role.Button,
                        interactionSource = centerInteractionSource,
                        indication = ripple(bounded = true, color = neuColors.primary)
                    ) { onOpenSaveModal() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isKurdish) "پاشەکەوتکردن" else "Save Project",
                    tint = neuColors.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "NavIndicatorElevation"
    )

    val navInteractionSource = remember { MutableInteractionSource() }
    val isItemPressed by navInteractionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .padding(vertical = 2.dp, horizontal = 3.dp)
            .heightIn(min = 52.dp)
            .then(
                if (isItemPressed || isSelected) {
                    Modifier.neuPressed(
                        cornerShape = RoundedCornerShape(16.dp),
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        elevation = if (isSelected) elevation else 1.5.dp
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = isSelected,
                role = Role.Tab,
                interactionSource = navInteractionSource,
                indication = ripple(bounded = true, color = neuColors.primary)
            ) { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) neuColors.primary else neuColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                lineHeight = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) neuColors.textMain else neuColors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Adaptive Neumorphic Navigation Rail for Expanded tablet viewports (>= 840dp).
 * Provides ergonomic side-anchored destination switching and prominent action trigger.
 */
@Composable
fun NeumorphicNavRail(
    currentScreen: NavScreen,
    onScreenSelected: (NavScreen) -> Unit,
    onOpenSaveModal: () -> Unit,
    isKurdish: Boolean = true,
    modifier: Modifier = Modifier
) {
    val neuColors = LocalNeumorphicColors.current

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(88.dp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .displayCutoutPadding()
            .padding(start = 10.dp, top = 10.dp, bottom = 10.dp, end = 2.dp)
            .neuFlat(
                lightShadowColor = neuColors.shadowLight,
                darkShadowColor = neuColors.shadowDark,
                backgroundColor = neuColors.surface,
                cornerRadius = 24.dp,
                elevation = 4.dp
            )
            .padding(vertical = 16.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Prominent Plus (+) Action Button
            val centerInteractionSource = remember { MutableInteractionSource() }
            val isCenterPressed by centerInteractionSource.collectIsPressedAsState()

            Box(
                modifier = Modifier
                    .size(54.dp)
                    .then(
                        if (isCenterPressed) {
                            Modifier.neuPressed(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                cornerRadius = 27.dp,
                                elevation = 3.dp
                            )
                        } else {
                            Modifier.neuConvex(
                                lightShadowColor = neuColors.shadowLight,
                                darkShadowColor = neuColors.shadowDark,
                                backgroundColor = neuColors.surface,
                                lightGradientColor = neuColors.primary.copy(alpha = 0.2f),
                                darkGradientColor = neuColors.primary.copy(alpha = 0.08f),
                                cornerRadius = 27.dp,
                                elevation = 4.dp
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .clickable(
                            role = Role.Button,
                            interactionSource = centerInteractionSource,
                            indication = ripple(bounded = true, color = neuColors.primary)
                        ) { onOpenSaveModal() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = if (isKurdish) "پاشەکەوتکردن" else "Save Project",
                        tint = neuColors.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Center: Navigation Destinations (Dashboard, Analysis, Projects, Glossary)
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                RailNavItem(
                    label = if (isKurdish) "داشبۆرد" else "Dashboard",
                    icon = Icons.Default.Dashboard,
                    isSelected = currentScreen == NavScreen.DASHBOARD,
                    onClick = { onScreenSelected(NavScreen.DASHBOARD) }
                )

                RailNavItem(
                    label = if (isKurdish) "شیکاری" else "Analysis",
                    icon = Icons.Default.Analytics,
                    isSelected = currentScreen == NavScreen.ANALYSIS,
                    onClick = { onScreenSelected(NavScreen.ANALYSIS) }
                )

                RailNavItem(
                    label = if (isKurdish) "پڕۆژەکان" else "Projects",
                    icon = Icons.Default.Folder,
                    isSelected = currentScreen == NavScreen.PROJECTS,
                    onClick = { onScreenSelected(NavScreen.PROJECTS) }
                )

                RailNavItem(
                    label = if (isKurdish) "فەرهەنگ" else "Glossary",
                    icon = Icons.AutoMirrored.Outlined.MenuBook,
                    isSelected = currentScreen == NavScreen.GLOSSARY,
                    onClick = { onScreenSelected(NavScreen.GLOSSARY) }
                )
            }

            // Bottom spacer for visual balance
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RailNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val neuColors = LocalNeumorphicColors.current
    val navInteractionSource = remember { MutableInteractionSource() }
    val isItemPressed by navInteractionSource.collectIsPressedAsState()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .width(68.dp)
            .heightIn(min = 54.dp)
            .then(
                if (isItemPressed || isSelected) {
                    Modifier.neuPressed(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 16.dp,
                        elevation = if (isSelected) 3.dp else 2.dp
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(16.dp))
            .selectable(
                selected = isSelected,
                role = Role.Tab,
                interactionSource = navInteractionSource,
                indication = ripple(bounded = true, color = neuColors.primary)
            ) { onClick() }
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) neuColors.primary else neuColors.textMuted,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) neuColors.textMain else neuColors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
