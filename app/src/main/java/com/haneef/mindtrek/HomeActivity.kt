package com.haneef.mindtrek

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.haneef.mindtrek.databinding.ActivityHomeBinding
import com.haneef.mindtrek.util.PrefsManager

class HomeActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val signButton = binding.navView.getHeaderView(0).findViewById<Button>(R.id.signButton)
        signButton.setOnClickListener{
            when((it as Button).text){
                "SIGN OUT" -> {
                    PrefsManager.getInstance(this).clearJwt()
                    PrefsManager.getInstance(this).clearUsername()
                    PrefsManager.getInstance(this).clearUserEmail()
                    refreshProfileOnDrawer()
                }
                "SIGN IN" -> {
                    findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_login)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START);
        }

        setSupportActionBar(binding.appBarHome.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_home)

        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener{
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                refreshProfileOnDrawer()
            }

            override fun onDrawerClosed(drawerView: View) {

            }

            override fun onDrawerStateChanged(newState: Int) {

            }

        })
        binding.navView.getHeaderView(0).findViewById<Button>(R.id.viewProfile).setOnClickListener{
            findNavController(R.id.nav_host_fragment_content_home).navigate(R.id.nav_profile)
        }

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_login
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        refreshProfileOnDrawer()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    private fun refreshProfileOnDrawer() {
        val username = PrefsManager.getInstance(this).getUsername()
        val useremail = PrefsManager.getInstance(this).getUserEmail()

        if ((username == "GUEST" && useremail == "no email")){
            binding.navView.getHeaderView(0).findViewById<Button>(R.id.viewProfile).visibility = View.GONE
            binding.navView.getHeaderView(0).findViewById<Button>(R.id.signButton).text = "SIGN IN"
        }
        else {
            binding.navView.getHeaderView(0).findViewById<Button>(R.id.viewProfile).visibility = View.VISIBLE
            binding.navView.getHeaderView(0).findViewById<Button>(R.id.signButton).text = "SIGN OUT"
        }
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.useremail).text = useremail
        binding.navView.getHeaderView(0).findViewById<TextView>(R.id.username).text = username
        Log.d("PROFILE", "REFRESH")

    }

    override fun onSupportNavigateUp(): Boolean {
        refreshProfileOnDrawer()
        val navController = findNavController(R.id.nav_host_fragment_content_home)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}