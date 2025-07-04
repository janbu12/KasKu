package com.android.kasku.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.android.kasku.R
import com.android.kasku.navigation.AppRoutes
import com.android.kasku.ui.theme.KasKuTheme
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    onAnimationFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 1f,
        animationSpec = tween(
            durationMillis = 1000
        ), label = "alphaAnimation"
    )

    val translateYAnim by animateFloatAsState(
        targetValue = if (startAnimation) -200f else 0f,
        animationSpec = tween(
            durationMillis = 1000
        ), label = "translateYAnimation"
    )

    LaunchedEffect(key1 = true) {
        delay(2000) // Tahan SplashScreen selama 2 detik sebelum animasi dimulai
        startAnimation = true // Mulai animasi fade out dan geser
        delay(1000) // Tunggu animasi selesai
        onAnimationFinished() // <-- Panggil callback setelah animasi selesai
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .graphicsLayer(
                alpha = alphaAnim,
                translationY = translateYAnim
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.kasku_1),
                contentDescription = "KasKu Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun SplashScreenPreview() {
    KasKuTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kasku_1),
                    contentDescription = "KasKu Logo",
                    modifier = Modifier.size(150.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}