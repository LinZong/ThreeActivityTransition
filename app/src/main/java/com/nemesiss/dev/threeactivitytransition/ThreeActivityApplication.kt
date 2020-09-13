package com.nemesiss.dev.threeactivitytransition

import android.app.Application
import android.content.Context
import me.weishu.reflection.Reflection

class ThreeActivityApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Reflection.unseal(base)
    }
}