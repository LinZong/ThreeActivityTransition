package com.nemesiss.dev.threeactivitytransition

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.ChangeImageTransform
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class FmSharedElementsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fm_shared_elements)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fm_container, Fragment1())
            .commit()
    }
}

class Fragment1 : Fragment() {
    lateinit var iv: ImageView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fm_a, container, false)
            .apply {
                iv = findViewById(R.id.first_imageview)
                iv.setOnClickListener { goToNextFragment() }
            }
    }

    private fun goToNextFragment() {
        fragmentManager
            ?.beginTransaction()
            ?.addSharedElement(iv, "fm_image")
            ?.replace(R.id.fm_container, Fragment2())
            ?.addToBackStack("fm2")
            ?.commit()
    }
}

class Fragment2 : Fragment() {
    lateinit var iv: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = AutoTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fm_b, container, false).apply {
            iv = findViewById(R.id.second_imageview)
            iv.setOnClickListener { goToNextFragment() }
        }
    }

    private fun goToNextFragment() {
        fragmentManager
            ?.beginTransaction()
            ?.addSharedElement(iv, "fm_image")
            ?.replace(R.id.fm_container, Fragment3())
            ?.addToBackStack("fm3")
            ?.commit()
    }
}

class Fragment3 : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = AutoTransition()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fm_c, container, false)
    }
}