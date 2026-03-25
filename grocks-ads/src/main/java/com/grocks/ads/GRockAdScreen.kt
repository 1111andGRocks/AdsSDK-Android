package com.grocks.ads

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

private val GrocksBg = Color(0xFF242424)
private val GrocksHeaderBg = Color(0xFF313131)
private val GrocksYellow = Color(0xFFFAC212)
private val GrocksProgressYellow = Color(0xFFFABA0D)

@Composable
internal fun GRockAdRoot(
    onComplete: (Result<Unit>) -> Unit,
    viewModel: GRockADViewModel = viewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME || event == Lifecycle.Event.ON_START) {
                    viewModel.refreshCountdownIfNeeded()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var didCompleteMediation by remember { mutableStateOf(false) }
    fun finishMediationSuccess() {
        if (didCompleteMediation) return
        didCompleteMediation = true
        onComplete(Result.success(Unit))
    }

    val reloadKey = viewModel.reloadKey
    var previousReloadKey by remember { mutableStateOf<UUID?>(null) }
    LaunchedEffect(reloadKey) {
        if (previousReloadKey != null && previousReloadKey != reloadKey) {
            viewModel.resetFromWebReload()
            viewModel.setWebLoading(true)
        }
        previousReloadKey = reloadKey
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(GrocksBg),
    ) {
        key(reloadKey) {
            Box(modifier = Modifier.fillMaxSize()) {
                GrocksAdWebView(
                    startUri = viewModel.startUri,
                    onLoadingStateChange = { viewModel.setWebLoading(it) },
                    onMainFrameNavigation = { viewModel.handleMainFrameNavigation(it) },
                    onMailOrTel = { finishMediationSuccess() },
                    modifier = Modifier.fillMaxSize(),
                )

                if (viewModel.isLoading) {
                    WebLoadingOverlay()
                }

                when (viewModel.currentStep) {
                    GRockADStep.YellowBlocks -> {
                        StepOneArrowOverlay(
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = (-32).dp),
                        )
                    }
                    GRockADStep.SearchResults -> {
                        StepTwoArrowOverlay(
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = (-92).dp),
                        )
                    }
                    else -> {}
                }
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
        ) {
            HeaderSection(
                viewModel = viewModel,
                onCollect = {
                    viewModel.handleCollectTap()
                    finishMediationSuccess()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            if (viewModel.showReward) 162.dp else 164.dp,
                        ),
            )
        }
    }
}

@Composable
private fun HeaderSection(
    viewModel: GRockADViewModel,
    onCollect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.background(GrocksHeaderBg),
    ) {
        if (viewModel.showReward) {
            RewardHeader(onCollect = onCollect)
        } else {
            when (viewModel.currentStep) {
                GRockADStep.Loading ->
                    HeaderCard(
                        stepText = "Step 1 / 3",
                        title = "Loading…",
                        cardWidth = 279.dp,
                        cardHeight = 60.dp,
                    )
                GRockADStep.YellowBlocks ->
                    HeaderCard(
                        stepText = "Step 1 / 3",
                        title = "Tap on any orange box",
                        cardWidth = 279.dp,
                        cardHeight = 60.dp,
                    )
                GRockADStep.SearchResults ->
                    HeaderCard(
                        stepText = "Step 2 / 3",
                        title = "Tap “Visit Website” to continue",
                        cardWidth = 243.dp,
                        cardHeight = 84.dp,
                    )
                GRockADStep.AdSite ->
                    StepThreeHeader(remainingSeconds = viewModel.remainingSeconds)
            }
        }
    }
}

@Composable
private fun HeaderCard(
    stepText: String,
    title: String,
    cardWidth: Dp,
    cardHeight: Dp,
) {
    val shape = RoundedCornerShape(18.dp)
    Column(modifier = Modifier.fillMaxSize()) {
        StepLabelRow(stepText = stepText)
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                        .background(Color.White, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun StepLabelRow(stepText: String) {
    val firstPart = stepText.replace(" / 3", "")
    Column {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(36.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = firstPart,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
            Text(
                text = " / 3",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.3f),
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .height(2.dp)
                    .background(Color.White.copy(alpha = 0.05f)),
        )
    }
}

@Composable
private fun StepThreeHeader(remainingSeconds: Int) {
    val totalSeconds = 10
    val progress = (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat()
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val formatted = "%02d:%02d".format(minutes, seconds)

    Column(modifier = Modifier.fillMaxSize()) {
        Column {
            Text(
                text = "Step 3 / 3",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                textAlign = TextAlign.Center,
            )
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .height(2.dp)
                        .background(Color.White.copy(alpha = 0.05f)),
            )
        }
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Almost done!\nPlease wait $formatted",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ProgressTrack(
                progress = progress.coerceIn(0f, 1f),
                modifier =
                    Modifier
                        .width(255.dp)
                        .height(8.dp),
            )
        }
    }
}

@Composable
private fun ProgressTrack(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val trackW = maxWidth
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White.copy(alpha = 0.28f), RoundedCornerShape(percent = 50)),
        )
        Box(
            modifier =
                Modifier
                    .width(trackW * progress.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(GrocksProgressYellow, RoundedCornerShape(percent = 50)),
        )
    }
}

@Composable
private fun RewardHeader(onCollect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Your reward is ready!",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onCollect,
            modifier =
                Modifier
                    .width(255.dp)
                    .height(50.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = GrocksYellow,
                    contentColor = Color.Black,
                ),
            shape = RoundedCornerShape(50),
        ) {
            Text(
                text = "Collect",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun WebLoadingOverlay() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .background(Color.Black.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
        ) {
            CircularProgressIndicator(color = Color.White)
        }
    }
}

@Composable
private fun StepOneArrowOverlay(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.width(314.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.Top,
    ) {
        repeat(3) {
            GrocksPdfArrow(
                assetPath = "grocks/arrow_down.pdf",
                widthDp = 73.5.dp,
                heightDp = 175.dp,
            )
        }
    }
}

@Composable
private fun StepTwoArrowOverlay(modifier: Modifier = Modifier) {
    GrocksPdfArrow(
        assetPath = "grocks/small_arrow_down.pdf",
        widthDp = 74.dp,
        heightDp = 260.dp,
        modifier = modifier,
    )
}

@Composable
private fun GrocksPdfArrow(
    assetPath: String,
    widthDp: Dp,
    heightDp: Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val wPx = with(density) { widthDp.roundToPx() }
    val hPx = with(density) { heightDp.roundToPx() }
    var bitmap by remember(assetPath, wPx, hPx) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    LaunchedEffect(assetPath, wPx, hPx) {
        bitmap =
            withContext(Dispatchers.IO) {
                renderPdfFromAssets(context, assetPath, wPx, hPx)?.asImageBitmap()
            }
    }
    val bmp = bitmap
    if (bmp != null) {
        Image(
            bitmap = bmp,
            contentDescription = null,
            modifier = modifier.size(widthDp, heightDp),
            contentScale = ContentScale.Fit,
        )
    }
}
