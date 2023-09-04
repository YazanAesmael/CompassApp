package com.yaxan.way.presentation.home_screen

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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

    val direction = when {
        currentDegree >= 337.5 || currentDegree < 22.5 -> Cardinal.NORTH
        currentDegree >= 22.5 && currentDegree < 67.5 -> Cardinal.NORTHEAST
        currentDegree >= 67.5 && currentDegree < 112.5 -> Cardinal.EAST
        currentDegree >= 112.5 && currentDegree < 157.5 -> Cardinal.SOUTHEAST
        currentDegree >= 157.5 && currentDegree < 202.5 -> Cardinal.SOUTH
        currentDegree >= 202.5 && currentDegree < 247.5 -> Cardinal.SOUTHWEST
        currentDegree >= 247.5 && currentDegree < 292.5 -> Cardinal.WEST
        currentDegree >= 292.5 && currentDegree < 337.5 -> Cardinal.NORTHWEST
        else -> null
    }

    val letter = direction?.letter ?: ""

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
            .background(darkBlue),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${currentDegree.toInt()}° $letter",
                color = Color.White,
                fontSize = 32.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = "Arrow Down", tint = white)
        }
        Spacer(modifier = Modifier.height(32.dp))
        CompassView(
            heading = currentDegree,
            modifier = Modifier
                .padding(top = 64.dp)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Composable
fun CompassView(heading: Float, modifier: Modifier) {
    val arrowPosColor = red
    val arrowNegColor = blue

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
            arrowPath.lineTo(size.width / 2 + 16, size.height / 2)
            arrowPath.lineTo(size.width / 2 - 16, size.height / 2)
            arrowPath.lineTo(size.width / 2, (size.height * .9).toFloat())


            rotate(-heading) {
                Cardinal.values().forEach {
                    rotate(it.degree.toFloat()) {
                        if (it.letter.length == 1) {
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    it.letter,
                                    size.width / 2,
                                    -(size.width / 2 - 95),
                                    Paint().apply {
                                        textSize = 70f
                                        color = Color.White.toArgb()
                                        textAlign = Paint.Align.CENTER
                                    }
                                )
                            }
                        }
                    }
                }

                for (i in 0..350 step 10) {
                    rotate(i.toFloat()) {
                        drawLine(
                            color = if (i == 0) red else white,
                            start = Offset(
                                x = size.width / 2,
                                ((size.height / 2) - size.width) + (if (i == 0) 35 else 25)
                            ),
                            end = Offset(
                                x = size.width / 2,
                                (size.height / 2) - size.width - (if (i == 0) 35 else 0)
                            ),
                            strokeWidth = if (i == 0) 8f else 5f,
                        )

                        if (i % 30 == 0) {
                            drawContext.canvas.nativeCanvas.apply {
                                drawText(
                                    i.toString(),
                                    size.width / 2,
                                    -(size.width / 2 + 40),
                                    Paint().apply {
                                        textSize = 40f
                                        color = Color.White.toArgb()
                                        textAlign = Paint.Align.CENTER
                                    }
                                )
                            }
                        }
                    }
                }

                drawPath(arrowPath, SolidColor(arrowNegColor))
                rotate(180f) {
                    drawPath(arrowPath, SolidColor(arrowPosColor))
                }
            }
        }
    }
}

fun Float.toRadians(): Float {
    return (this * PI / 180).toFloat()
}
