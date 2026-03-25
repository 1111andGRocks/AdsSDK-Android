package com.grocks.ads.example

import android.app.Application
import com.grocks.ads.GrocksAds

class ExampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GrocksAds.initialize(this)
    }
}
