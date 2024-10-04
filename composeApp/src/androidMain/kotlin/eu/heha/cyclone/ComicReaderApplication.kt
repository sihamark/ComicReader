package eu.heha.cyclone

import android.app.Application

class ComicReaderApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        CycloneApp.initialize()
    }
}