package com.alirnp.watch

import android.app.Application
import com.alirnp.watch.di.appModule
import org.koin.core.context.startKoin

class App : Application()  {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}