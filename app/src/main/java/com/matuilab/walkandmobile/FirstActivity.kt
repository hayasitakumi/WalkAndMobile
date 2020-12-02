package com.matuilab.walkandmobile

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_first.*


class FirstActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        toolbar.setNavigationOnClickListener {
//            Toast.makeText(applicationContext, "your icon was clicked", Toast.LENGTH_SHORT).show()
//        }
        setSupportActionBar(toolbar)
//        supportActionBar?.let {
//            it.setDisplayHomeAsUpEnabled(true)
//            it.setHomeButtonEnabled(true)
//        } ?: IllegalAccessException("Toolbar cannot be null")

        // sync drawer
        // sync drawer
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val actionBarDrawerToggle = ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        /**NavigationDrawerの設定*/
        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar,
                R.string.drawer_open,
                R.string.drawer_close)
        drawer_layout!!.addDrawerListener(toggle)
        toggle.syncState()

        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        appBarConfiguration = AppBarConfiguration(setOf(R.id.permission_check_navigation, R.id.camera_navigation))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
//        findViewById<Toolbar>(R.id.toolbar)
//                .setupWithNavController(navController, appBarConfiguration)


        // set navigation
//        nav_view.setNavigationItemSelectedListener {
//            when (it.itemId) {
//                /** 再生速度 */
//                R.id.nav_settings -> {
//                    Log.d("nav_test", "settings")
//                }
//
//                /** 事前ダウンロードボタン */
//                R.id.nav_download_in_advance -> {
//                    Log.d("nav_test", "download")
//                }
//
//                /** プライバシーポリシーボタン */
//                R.id.nav_privacy_policy -> {
//                    Log.d("nav_test", "privacy_policy")
//
//                }
//            }
//            false
//        }
//        nav_view.setNavigationItemSelectedListener {
//            when (it.itemId) {
//                /** 再生速度 */
//                R.id.nav_settings -> {
//                    Log.d("nav_test", "settings")
//                }
//
//                /** 事前ダウンロードボタン */
//                R.id.nav_download_in_advance -> {
//                    Log.d("nav_test", "download")
//                }
//
//                /** プライバシーポリシーボタン */
//                R.id.nav_privacy_policy -> {
//                    Log.d("nav_test", "privacy_policy")
//
//                }
//            }
//            nav_view.closeDrawer(GravityCompat.START)
//            true
//        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
}