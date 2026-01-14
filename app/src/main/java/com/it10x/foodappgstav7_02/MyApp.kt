package com.it10x.foodappgstav7_02

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.it10x.foodappgstav7_02.firebase.ClientIdStore
import com.it10x.foodappgstav7_02.firebase.ClientRegistry

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val clientId = ClientIdStore.get(this) ?: return
        val cfg = ClientRegistry.get(clientId)

        val options = FirebaseOptions.Builder()
            .setApiKey(cfg.apiKey)
            .setApplicationId(cfg.applicationId)
            .setProjectId(cfg.projectId)
            .build()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this, options)
        }
    }
}


