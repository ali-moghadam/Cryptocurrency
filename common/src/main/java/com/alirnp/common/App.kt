package com.alirnp.common

import android.app.Application
import com.alirnp.common.di.appModule
import org.koin.core.context.startKoin

class App : Application()  {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}