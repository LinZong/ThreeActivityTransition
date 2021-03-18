package com.nemesiss.dev.threeactivitytransition

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        first_imageview.setOnClickListener { openSecondActivity() }


    }


    private fun openSecondActivity() {
        startActivity(
                Intent(this, SecondActivity::class.java),
                ActivityOptionsCompat.makeSceneTransitionAnimation(this, first_imageview, "image").toBundle()
        )
    }

    fun toFragmentDemo(view: View) {
        startActivity(Intent(this, FmSharedElementsActivity::class.java))
        finish()
    }
}
