package com.matuilab.walkandmobile

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.joanzapata.pdfview.PDFView
import kotlinx.android.synthetic.main.activity_privacy_policy.*


class PrivacyPolicyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)

//        val pdfView = findViewById<PDFView>(R.id.privacy_policy_pdfview)
        privacy_policy_pdfview.fromAsset("privacypolicy.pdf")
                .defaultPage(1)
                .showMinimap(false)
                .enableSwipe(true)
                .load()

        setSupportActionBar(privacy_policy_toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeButtonEnabled(true)
        } ?: IllegalAccessException("Toolbar cannot be null")
    }

    override fun onBackPressed() {
        // Do something
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}