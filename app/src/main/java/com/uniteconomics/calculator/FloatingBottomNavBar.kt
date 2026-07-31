package com.uniteconomics.calculator

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen enumeration for multi-screen layout navigation.
 */
enum class NavScreen {
    DASHBOARD,
    ANALYSIS,
    PROJECTS
}

/**
 * Floating bottom navigation bar matching screenshot #8:
 * - Left: Dashboard tab with text label
 * - Center: Upper-floating prominent circular Neumorphic Convex (+) Plus button
 * - Right: Analysis tab with text label
 * - Clean original Neumorphic design system with ZERO blue tinting (dark charcoal slate textMain/textMuted)
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
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating NeuFlat Navigation Bar Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .neuFlat(
                    lightShadowColor = neuColors.shadowLight.copy(alpha = 0.2f),
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    cornerRadius = 30.dp,
                    elevation = 3.dp
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tab 1: Dashboard (Left, with Text Label)
                NavItem(
                    label = if (isKurdish) "داشبۆرد" else "Dashboard",
                    icon = Icons.Default.Dashboard,
                    isSelected = currentScreen == NavScreen.DASHBOARD,
                    onClick = { onScreenSelected(NavScreen.DASHBOARD) },
                    modifier = Modifier.weight(1f)
                )

                // Center Spacer for Upper Floating Plus Button
                Spacer(modifier = Modifier.width(64.dp))

                // Tab 2: Analysis (Right, with Text Label)
                NavItem(
                    label = if (isKurdish) "شیکاری" else "Analysis",
                    icon = Icons.Default.Analytics,
                    isSelected = currentScreen == NavScreen.ANALYSIS,
                    onClick = { onScreenSelected(NavScreen.ANALYSIS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // CENTER ITEM: Prominent Upper Floating Circular Neumorphic Convex (+) Plus Button (Offset Upwards!)
        Box(
            modifier = Modifier
                .offset(y = (-10).dp)
                .size(62.dp)
                .neuConvex(
                    lightShadowColor = neuColors.shadowLight.copy(alpha = 0.2f),
                    darkShadowColor = neuColors.shadowDark,
                    backgroundColor = neuColors.surface,
                    cornerRadius = 31.dp,
                    elevation = 4.dp
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onOpenSaveModal() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = if (isKurdish) "پاشەکەوتکردن" else "Save Project",
                tint = neuColors.textMain,
                modifier = Modifier.size(28.dp)
            )
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
        targetValue = if (isSelected) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 250),
        label = "NavIndicatorElevation"
    )

    Box(
        modifier = modifier
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .then(
                if (isSelected) {
                    Modifier.neuConvex(
                        lightShadowColor = neuColors.shadowLight,
                        darkShadowColor = neuColors.shadowDark,
                        backgroundColor = neuColors.surface,
                        cornerRadius = 18.dp,
                        elevation = elevation
                    )
                } else {
                    Modifier
                }
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) neuColors.textMain else neuColors.textMuted,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) neuColors.textMain else neuColors.textMuted,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
