package com.friendzone.android.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AppBackground(content: @Composable BoxScope.() -> Unit) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFFF7F3EF),
            Color(0xFFF2E9E2),
            Color(0xFFECE0D8)
        )
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        content = content
    )
}


