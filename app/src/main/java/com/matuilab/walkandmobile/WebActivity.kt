package com.matuilab.walkandmobile

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_web.*

class WebActivity(private val webUrl: String) : AppCompatActivity() {
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web)

        web_view.settings.javaScriptEnabled = true
        web_view.loadUrl(webUrl)
    }
}