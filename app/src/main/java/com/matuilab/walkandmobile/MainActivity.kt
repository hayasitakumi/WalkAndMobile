package com.matuilab.walkandmobile

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*



class MainActivity : AppCompatActivity() {


    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.permission_check_fragment, R.id.camera_fragment), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

//        val navController = findNavController(R.id.nav_host_fragment)
//        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
//        findViewById<Toolbar>(R.id.toolbar)
//                .setupWithNavController(navController, appBarConfiguration)




//        val navHostFragment =
//                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//        val appBarConfiguration = AppBarConfiguration(navController.graph, drawer_layout)
//        findViewById<NavigationView>(R.id.nav_view)
//                .setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d("itemselected", "$item")
//        val navController = findNavController(R.id.nav_host_fragment)
//        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
//    }

//    override fun onResume() {
//        super.onResume()
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar?.let {
//            it.setDisplayHomeAsUpEnabled(true)
//            it.setHomeButtonEnabled(true)
//        } ?: IllegalAccessException("Toolbar cannot be null")
//
//        /** sync drawer */
//        val actionBarDrawerToggle = ActionBarDrawerToggle(
//                this, drawer_layout, toolbar, R.string.drawer_open, R.string.drawer_close)
//        drawer_layout.addDrawerListener(actionBarDrawerToggle)
//        actionBarDrawerToggle.syncState()
//
////        val navView: NavigationView = findViewById(R.id.nav_view)
//        val navController = findNavController(R.id.nav_host_fragment)
//
//        appBarConfiguration = AppBarConfiguration(setOf(R.id.permission_check_navigation, R.id.camera_navigation))
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        nav_view.setupWithNavController(navController)
//
//        // set navigation
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
//                    findNavController(R.id.nav_host_fragment).navigate(R.id.action_camera_to_privacy_policy)
//                }
//            }
//            drawer_layout.closeDrawer(GravityCompat.START)
//            false
//        }
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment)
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
//
//    override fun onBackPressed() {
//        // Do something
//        supportFragmentManager.popBackStack()
//        super.onBackPressed()
////        val intent = Intent(this, MainActivity::class.java)
////        startActivity(intent)
////        finish()
//    }

//    override fun onSupportNavigateUp(): Boolean {
////        val navController = findNavController(R.id.nav_host_fragment)
////        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//        onBackPressed()
//        return true
//    }

//    override fun lockDrawer() {
//        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//    }
//
//    override fun unlockDrawer() {
//        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//    }
////    override fun onSupportNavigateUp()
////            = findNavController(R.id.nav_host_fragment).popBackStack()
//}
//
//interface DrawerInterface {
//    fun lockDrawer()
//    fun unlockDrawer()
}