package com.yaxan.way.presentation.home_screen

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.yaxan.way.common.Cardinal
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.math.PI

val white = Color(0xFFF7F7FF)
val blue = Color(0xFFBDD5EA)
val darkBlue = Color(0xFF577399)
val gray = Color(0xFF495867)
val red = Color(0xFFFE5F55)
val green = Color(0xFF9BFE55)

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    var currentDegree by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val heading by viewModel.heading.collectAsState()

    DisposableEffect(Unit) {
        val dataManager = SensorDataManager(context)
        dataManager.init()

        val job = scope.launch {
            dataManager.data
                .receiveAsFlow()
                .onEach { currentDegree = it }
                .collect {}
        }
        onDispose {
            dataManager.cancel()
            job.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .background(blue, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
        ) {
            Text(
                text = "$currentDegree°",
                color = red,
                fontSize = 32.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .align(Alignment.TopCenter),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            CompassView(
                heading = currentDegree,
                modifier = Modifier
                    .padding(top = 64.dp)
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        }
    }
}

@Composable
fun CompassView(heading: Float, modifier: Modifier) {
    val circleColor = MaterialTheme.colorScheme.onBackground
    val arrowPosColor = red
    val arrowNegColor = darkBlue

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .width(100.dp)
                .height(100.dp)
        ) {
            drawCircle(
                color = white,
                style = Stroke(15f),
                center = Offset(x = size.width / 2, y = size.height / 2),
                radius = size.width
            )

            val arrowPath = Path()
            arrowPath.moveTo(size.width / 2, (size.height * .9).toFloat())
            arrowPath.lineTo(size.width / 2 + 30, size.height / 2)
            arrowPath.lineTo(size.width / 2 - 30, size.height / 2)
            arrowPath.lineTo(size.width / 2, (size.height * .9).toFloat())

            rotate(-heading) {
                Cardinal.values().forEach {
                    rotate(it.degree.toFloat()) {
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(
                                it.letter,
                                size.width / 2,
                                -(size.width / 2 + 40),
                                Paint().apply {
                                    textSize = 50f
                                    color = if (it.letter == "N") red.toArgb() else gray.toArgb()
                                    textAlign = Paint.Align.CENTER
                                }
                            )
                        }
                    }
                }

                for (i in 0..350 step 10) {
                    rotate(i.toFloat()) {
                        drawLine(
                            color = white,
                            start = Offset(x = size.width / 2, ((size.height / 2) - size.width) + 25),
                            end = Offset(x = size.width / 2, (size.height / 2) - size.width),
                            strokeWidth = 5f,
                        )
                    }
                }

                drawPath(arrowPath, SolidColor(arrowNegColor))
                rotate(180f) {
                    drawPath(arrowPath, SolidColor(arrowPosColor))
                }
            }

            drawLine(
                color = gray,
                start = Offset(x = size.width / 2, ((size.height / 2) - size.width) + 30),
                end = Offset(x = size.width / 2, ((size.height / 2) - size.width) - 30),
                strokeWidth = 10f,
            )
        }
    }
}

fun Float.toRadians(): Float {
    return (this * PI / 180).toFloat()
}
