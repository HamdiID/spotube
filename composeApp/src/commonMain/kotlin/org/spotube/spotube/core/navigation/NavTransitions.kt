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

package org.spotube.spotube.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene

private const val TRANSITION_DURATION_MILLIS = 300
private const val FADE_THROUGH_OUT_MILLIS = 90
private const val FADE_THROUGH_SCALE = 0.94f
private const val SLIDE_FRACTION = 0.25f
private const val BEHIND_SLIDE_FRACTION = 0.1f
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

// NavEntry.contentKey defaults to key.toString()
private val topLevelContentKeys = TOP_LEVEL_ROUTES.map { it.toString() }.toSet()

/**
 * The tab a scene belongs to: the last top-level route in its stack. Switching tabs changes
 * this, while pushing/popping pages inside a tab doesn't.
 */
private fun Scene<NavKey>.tabRoot(): Any? =
    (previousEntries + entries).lastOrNull { it.contentKey in topLevelContentKeys }?.contentKey

private fun AnimatedContentTransitionScope<Scene<NavKey>>.isTabSwitch(): Boolean =
    initialState.tabRoot() != targetState.tabRoot()

/** Material fade-through: the old tab fades out, the new one fades in while scaling up slightly. */
private fun fadeThrough(): ContentTransform {
    val enter = fadeIn(
        tween(TRANSITION_DURATION_MILLIS - FADE_THROUGH_OUT_MILLIS, FADE_THROUGH_OUT_MILLIS, EmphasizedDecelerate)
    ) + scaleIn(
        tween(TRANSITION_DURATION_MILLIS - FADE_THROUGH_OUT_MILLIS, FADE_THROUGH_OUT_MILLIS, EmphasizedDecelerate),
        initialScale = FADE_THROUGH_SCALE
    )
    return enter togetherWith fadeOut(tween(FADE_THROUGH_OUT_MILLIS))
}

/**
 * Pushed/popped pages inside a tab: the page on top slides + fades while the page behind
 * drifts slightly the other way. On pop the leaving page must stay on top (negative target
 * z-index), otherwise the page being revealed is drawn over it and its exit is never seen.
 */
private fun AnimatedContentTransitionScope<Scene<NavKey>>.stackTransition(
    forward: Boolean
): ContentTransform {
    // Spotify switches tabs instantly, with no animation.
    if (isTabSwitch()) return ContentTransform(EnterTransition.None, ExitTransition.None)

    // iOS-style push: the new page slides in from the right edge over the old one,
    // which drifts a third to the left and dims slightly.
    val duration = 380
    val slide = tween<IntOffset>(duration, easing = IosEasing)
    val enter: EnterTransition
    val exit: ExitTransition
    if (forward) {
        enter = slideInHorizontally(slide) { it }
        exit = slideOutHorizontally(slide) { -(it * 0.3f).toInt() } +
            fadeOut(tween(duration, easing = IosEasing), targetAlpha = 0.6f)
    } else {
        enter = slideInHorizontally(slide) { -(it * 0.3f).toInt() } +
            fadeIn(tween(duration, easing = IosEasing), initialAlpha = 0.6f)
        exit = slideOutHorizontally(slide) { it }
    }
    return ContentTransform(enter, exit, targetContentZIndex = if (forward) 0f else -1f)
}

private val IosEasing = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)

val navPushTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    { stackTransition(forward = true) }

val navPopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform =
    { stackTransition(forward = false) }

val navPredictivePopTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.(Int) -> ContentTransform =
    { stackTransition(forward = false) }
