package com.grocks.ads

import android.app.Activity
import android.app.Application
import androidx.activity.ComponentActivity

/**
 * Публичная точка входа SDK (аналог iOS `GrocksAds`).
 */
public object GrocksAds {

    @Volatile
    private var pendingResult: ((Result<Unit>) -> Unit)? = null

    /**
     * Регистрирует отслеживание foreground Activity для overload [showAd] без явной Activity.
     */
    public fun initialize(application: Application) {
        GrocksAdsActivityResolver.install(application)
    }

    public fun setApiKey(apiKey: String) {
        GrocksAdsConfiguration.apiKey = apiKey
    }

    /**
     * Показ полноэкранной рекламы. После успешного завершения сценария или ошибки вызывается [onResult].
     */
    public fun showAd(activity: ComponentActivity, onResult: (Result<Unit>) -> Unit) {
        showAdInternal(activity, onResult)
    }

    /**
     * Требует предварительный [initialize]; иначе [onResult] с [GrocksAdsPresentationError.NoPresenter].
     */
    public fun showAd(onResult: (Result<Unit>) -> Unit) {
        val activity = GrocksAdsActivityResolver.currentResumedActivity() as? ComponentActivity
        if (activity == null) {
            onResult(
                Result.failure(
                    GrocksAdsPresentationException(GrocksAdsPresentationError.NoPresenter),
                ),
            )
            return
        }
        showAdInternal(activity, onResult)
    }

    private fun showAdInternal(activity: Activity, onResult: (Result<Unit>) -> Unit) {
        if (pendingResult != null) {
            onResult(Result.failure(IllegalStateException("Другой показ рекламы уже активен.")))
            return
        }
        pendingResult = onResult
        activity.startActivity(android.content.Intent(activity, GrocksAdActivity::class.java))
    }

    internal fun deliverResult(result: Result<Unit>) {
        val cb = pendingResult
        pendingResult = null
        cb?.invoke(result)
    }
}
