package com.company.dilnoza.permission.app

import android.app.Application
import com.company.dilnoza.permission.source.local.LocalStorage

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        LocalStorage.init(this)
    }

    companion object {
        lateinit var instance: App
    }
}