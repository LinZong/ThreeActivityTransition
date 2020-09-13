package com.nemesiss.dev.threeactivitytransition

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import kotlinx.android.synthetic.main.activity_second.*

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        second_imageview.setOnClickListener { openThirdActivity() }
    }
    private fun openThirdActivity() {
        startActivity(
            Intent(this, ThirdActivity::class.java),
            ActivityOptionsCompat.makeSceneTransitionAnimation(this, second_imageview, "image").toBundle()
        )
    }
    override fun onBackPressed() {
        SharedElementUtils.setPendingExitSharedElements(this, arrayListOf("image"))
        supportFinishAfterTransition()
    }
}
