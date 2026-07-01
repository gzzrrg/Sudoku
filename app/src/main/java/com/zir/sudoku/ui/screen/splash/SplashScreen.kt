package com.zir.sudoku.ui.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.zir.sudoku.R
import com.zir.sudoku.ui.theme.SudokuColorPalette
import androidx.compose.ui.graphics.drawscope.clipRect

@Composable
fun SplashScreen(
    palette: SudokuColorPalette,
    onFinished: () -> Unit
) {
    val reveal = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        kotlinx.coroutines.delay(200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.appBackground),
        contentAlignment = Alignment.Center
    ) {
        val splashFont = FontFamily(Font(R.font.splash_font))
        Text(
            text = "SUDOKU",
            fontSize = 52.sp,
            fontFamily = splashFont,
            fontWeight = FontWeight.Bold,
            color = palette.givenNumber,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.drawWithContent {
                val revealWidth = size.width * reveal.value
                clipRect(right = revealWidth) {
                    this@drawWithContent.drawContent()
                }
            }
        )
    }
}
