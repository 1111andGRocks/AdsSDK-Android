package com.grocks.ads

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

internal object GrocksAdsActivityResolver {
    @Volatile
    private var resumedActivity: WeakReference<Activity>? = null

    @Volatile
    private var installed: Boolean = false

    fun install(application: Application) {
        if (installed) return
        synchronized(this) {
            if (installed) return
            application.registerActivityLifecycleCallbacks(callbacks)
            installed = true
        }
    }

    fun currentResumedActivity(): Activity? = resumedActivity?.get()

    private val callbacks =
        object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {}

            override fun onActivityResumed(activity: Activity) {
                resumedActivity = WeakReference(activity)
            }

            override fun onActivityPaused(activity: Activity) {
                if (resumedActivity?.get() === activity) {
                    resumedActivity = null
                }
            }

            override fun onActivityStopped(activity: Activity) {}

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {
                if (resumedActivity?.get() === activity) {
                    resumedActivity = null
                }
            }
        }
}
