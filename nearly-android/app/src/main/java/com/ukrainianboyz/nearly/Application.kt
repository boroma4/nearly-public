package com.ukrainianboyz.nearly

import android.app.Application
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.MobileAds
import com.ukrainianboyz.nearly.service.notifications.NotificationService
import com.ukrainianboyz.nearly.service.signalR.SignalRClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class Application: Application() {

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(applicationContext) { }
        val appLifecycleObserver = AppLifecycleObserver()
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        ApplicationPrefs.initEncryptedPrefs(applicationContext)
        NotificationService.startService(applicationContext)
    }

    override fun onTerminate() {
        SignalRClient.disconnectFromHub()
        super.onTerminate()
    }

     class AppLifecycleObserver : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onEnterForeground() {
            Log.d(TAG,"foreground ")
            ApplicationPrefs.isInForeground = true
            GlobalScope.launch(Dispatchers.IO) {
                SignalRClient.tryReconnect()
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onEnterBackground() {
            Log.d(TAG,"background ")
            ApplicationPrefs.isInForeground = false
            // safe to disconnect only when there is no active call
            if(!SignalRClient.onCall){
                SignalRClient.disconnectFromHub()
            }

        }

        companion object {
            val TAG = AppLifecycleObserver::class.java.name
        }
    }
}