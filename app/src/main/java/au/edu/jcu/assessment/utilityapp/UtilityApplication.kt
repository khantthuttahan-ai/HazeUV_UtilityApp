package au.edu.jcu.assessment.utilityapp

import android.app.Application
import au.edu.jcu.assessment.utilityapp.di.AppContainer
import au.edu.jcu.assessment.utilityapp.di.DefaultAppContainer

/**
 * Custom Application class (registered in AndroidManifest.xml).
 * It creates the dependency container once, and it lives as long as the app does.
 */
class UtilityApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
