package com.grocks.ads

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

internal enum class GRockADStep {
    Loading,
    YellowBlocks,
    SearchResults,
    AdSite,
}

private object GRockADContentURLBuilder {
    private const val BASE = "https://adsshowapp.info/test"

    fun makeStartUri(packageName: String): Uri {
        return Uri.parse(BASE).buildUpon()
            .appendQueryParameter("bundleId", packageName)
            .build()
    }
}

internal class GRockADViewModel(application: Application) : AndroidViewModel(application) {

    private object Constants {
        const val COUNTDOWN_DURATION = 10
    }

    var mainFrameRedirectCount by mutableIntStateOf(0)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var showReward by mutableStateOf(false)
        private set

    val currentStep: GRockADStep
        get() = when {
            isLoading -> GRockADStep.Loading
            mainFrameRedirectCount <= 2 -> GRockADStep.YellowBlocks
            mainFrameRedirectCount <= 3 -> GRockADStep.SearchResults
            else -> GRockADStep.AdSite
        }

    var remainingSeconds by mutableIntStateOf(Constants.COUNTDOWN_DURATION)
        private set

    var reloadKey by mutableStateOf(UUID.randomUUID())
        private set

    val startUri: Uri = GRockADContentURLBuilder.makeStartUri(application.packageName ?: "unknown")

    private var lastMainFrameNormalizedURL: String? = null
    private var adCountdownStarted = false
    private var countdownJob: Job? = null
    private var countdownDeadlineMs: Long? = null

    fun resetFromWebReload() {
        mainFrameRedirectCount = 0
        lastMainFrameNormalizedURL = null
        adCountdownStarted = false
        showReward = false
        countdownJob?.cancel()
        countdownJob = null
        countdownDeadlineMs = null
        remainingSeconds = Constants.COUNTDOWN_DURATION
    }

    fun setWebLoading(loading: Boolean) {
        isLoading = loading
    }

    fun handleCollectTap() {
        countdownJob?.cancel()
        countdownJob = null
    }

    fun handleMainFrameNavigation(uri: Uri?) {
        android.util.Log.d("GrocksAds", "Navigation: [${uri}]")
        if (uri == null) return

        val normalized = normalizedUri(uri)
        if (normalized == lastMainFrameNormalizedURL) return
        lastMainFrameNormalizedURL = normalized
        mainFrameRedirectCount += 1
        android.util.Log.d("GrocksAds", "Redirect Count: [$mainFrameRedirectCount]")

        if (mainFrameRedirectCount >= 4 && !adCountdownStarted && !showReward) {
            adCountdownStarted = true
            startCountdown()
        }
    }

    fun refreshCountdownIfNeeded() {
        if (countdownDeadlineMs == null || showReward) return
        syncRemainingSecondsWithDeadline()

        if (countdownDeadlineMs != null && (countdownJob == null || !countdownJob!!.isActive)) {
            startCountdownLoop()
        }
    }

    private fun startCountdown() {
        android.util.Log.d("GrocksAds", "Start Countdown")
        countdownJob?.cancel()
        remainingSeconds = Constants.COUNTDOWN_DURATION
        countdownDeadlineMs = System.currentTimeMillis() + remainingSeconds * 1000L
        startCountdownLoop()
    }

    private fun startCountdownLoop() {
        countdownJob?.cancel()
        countdownJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(1000)
                    syncRemainingSecondsWithDeadline()
                }
            }
    }

    private fun syncRemainingSecondsWithDeadline() {
        val deadline = countdownDeadlineMs ?: return
        val secondsLeft = kotlin.math.max(0, kotlin.math.ceil((deadline - System.currentTimeMillis()) / 1000.0).toInt())

        if (secondsLeft > 0) {
            remainingSeconds = secondsLeft
        } else {
            countdownJob?.cancel()
            countdownJob = null
            countdownDeadlineMs = null
            remainingSeconds = 0
            showReward = true
        }
    }

    private fun normalizedUri(uri: Uri): String {
        val builder = uri.buildUpon().fragment(null)
        return builder.build().toString()
    }

    override fun onCleared() {
        countdownJob?.cancel()
        super.onCleared()
    }
}
