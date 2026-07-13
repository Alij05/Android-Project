package com.example.musicapp.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Defines the shape styles for the application.
val MusicAppShapes = Shapes(
    // Small corners, used for components like chips or small buttons.
    small = RoundedCornerShape(4.dp),

    // Medium corners, the most common size, used for cards, buttons, etc.
    medium = RoundedCornerShape(8.dp),

    // Large corners, used for larger components like bottom sheets or dialogs.
    large = RoundedCornerShape(16.dp)
)
