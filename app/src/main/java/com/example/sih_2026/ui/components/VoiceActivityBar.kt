package com.example.sih_2026.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun VoiceActivityBar(
    audioLevel: Float, // 0.0 to 1.0
    modifier: Modifier = Modifier,
    activeColor: Color = Color(0xFF818CF8)
) {
    val barCount = 12
    
    Row(
        modifier = modifier.fillMaxWidth().height(40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val randomOffset = remember { Random.nextFloat() * 0.5f }
            val heightMultiplier = (audioLevel + randomOffset).coerceIn(0.1f, 1.0f)
            
            val animatedHeight by animateFloatAsState(
                targetValue = 30.dp.value * heightMultiplier,
                animationSpec = spring(stiffness = Spring.StiffnessLow),
                label = "barHeight"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (audioLevel > 0.05f) activeColor else Color.Gray.copy(alpha = 0.3f))
            )
        }
    }
}
