package com.company.dilnoza.permission.source.local

import android.content.Context
import com.company.dilnoza.permission.extension.FloatPreference
import com.company.dilnoza.permission.extension.IntPreference

class LocalStorage private constructor(context: Context) {
    companion object {
        lateinit var instance: LocalStorage; private set
        fun init(context: Context) {
            instance = LocalStorage(context)
        }
    }

    private val pref = context.getSharedPreferences("LocalStorage", Context.MODE_PRIVATE)
    var countMedia: Int by IntPreference(pref)
    var sizMedia: Float by FloatPreference(pref)
    var countFile: Int by IntPreference(pref)
    var sizeFile: Float by FloatPreference(pref)
}