package com.mediaplayer.kmp

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

internal class AppContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context {
        AppContext.setUp(context.applicationContext)
        return AppContext.get()
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

internal object AppContext {
    private lateinit var application: Application

    fun setUp(context: Context) {
        application = context as Application
    }

    fun get(): Context {
        return if (AppContext::application.isInitialized.not()) throw Exception("Context is not initialized.")
        else application.applicationContext
    }
}