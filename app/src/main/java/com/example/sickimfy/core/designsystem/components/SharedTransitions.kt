package com.example.sickimfy.core.designsystem.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal to expose the SharedTransitionScope implicitly down the composable tree.
 * Prevents prop-drilling of transition parameters to deeply nested cards and player layouts.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * CompositionLocal to expose the AnimatedVisibilityScope implicitly down the composable tree.
 */
val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }