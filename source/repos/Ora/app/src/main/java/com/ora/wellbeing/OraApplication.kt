package com.ora.wellbeing

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Classe Application pour l'app Ora avec configuration Hilt
 */
@HiltAndroidApp
class OraApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configuration de Timber pour les logs
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.d("OraApplication created")
    }
}