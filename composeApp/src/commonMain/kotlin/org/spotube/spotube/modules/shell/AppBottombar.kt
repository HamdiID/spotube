/*
 * Copyright (C) 2026 Kingkor Roy Tirtho and Spotube Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.spotube.spotube.modules.shell

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import org.spotube.spotube.core.ui.spotify.liquidGlass
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composeunstyled.Text
import org.jetbrains.compose.resources.stringResource
import org.spotube.spotube.NavigationItem
import org.spotube.spotube.bottombarTabs
import org.spotube.spotube.core.navigation.NavigationState
import org.spotube.spotube.core.navigation.Navigator
import org.spotube.spotube.core.navigation.Routes
import org.spotube.spotube.core.ui.spotify.Sp
import org.spotube.spotube.core.ui.spotify.SpIcon
import org.spotube.spotube.core.ui.spotify.SpIcons
import org.spotube.spotube.core.ui.spotify.bouncyClick
import org.spotube.spotube.modules.downloads.DownloadBadgeIndicator

@Composable
fun AppBottombar(
    navigator: Navigator,
    navigationState: NavigationState,
    modifier: Modifier = Modifier,
    libraryBadge: @Composable () -> Unit = { DownloadBadgeIndicator() }
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0f),
                    0.5f to Color.Black.copy(alpha = 0.55f),
                    1f to Color.Black.copy(alpha = 0.85f)
                )
            )
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Floating "liquid glass" capsule, like Spotify on iOS 26.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 6.dp)
                .liquidGlass(RoundedCornerShape(30.dp))
                .padding(horizontal = 6.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bottombarTabs.filterIsInstance<NavigationItem.Tab<*>>().forEach { tab ->
                val selected = navigationState.topLevelRoute == tab.route
                SpotifyTab(
                    title = stringResource(tab.title),
                    icon = tabIcon(tab.route, selected) ?: tab.icon,
                    selected = selected,
                    modifier = Modifier.weight(1f),
                    badge = if (tab.route == Routes.Library) libraryBadge else null,
                    onClick = { navigator.navigate(tab.route) }
                )
            }
        }
    }
}

private fun tabIcon(route: Routes, selected: Boolean): ImageVector? = when (route) {
    Routes.Home -> if (selected) SpIcons.HomeFilled else SpIcons.Home
    Routes.Search -> if (selected) SpIcons.SearchBold else SpIcons.Search
    Routes.Library -> if (selected) SpIcons.LibraryFilled else SpIcons.Library
    Routes.Settings -> SpIcons.Settings
    else -> null
}

@Composable
private fun SpotifyTab(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier,
    badge: (@Composable () -> Unit)?,
    onClick: () -> Unit
) {
    val pop by animateFloatAsState(
        targetValue = if (selected) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow),
        label = "tab"
    )
    val tint = if (selected) Sp.Text else Sp.SubText
    val pill by animateColorAsState(
        if (selected) Color.White.copy(alpha = 0.13f) else Color.Transparent,
        tween(220),
        label = "pill"
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(pill)
            .bouncyClick(pressedScale = 0.86f, onClick = onClick)
            .padding(vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.graphicsLayer { scaleX = pop; scaleY = pop },
            contentAlignment = Alignment.TopEnd
        ) {
            SpIcon(icon, contentDescription = title, tint = tint, size = 26.dp)
            if (badge != null) Box(Modifier.padding(start = 18.dp)) { badge() }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = title,
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            ),
            color = tint,
            maxLines = 1
        )
    }
}
