package com.alirnp.cleammvvm

import android.app.Application
import com.alirnp.cryptocurrency.di.appModule
import org.koin.core.context.startKoin

class App : Application()  {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModule)
        }
    }
}