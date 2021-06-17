package io.mju.habux

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import io.mju.habux.databinding.ActivityMainBinding
import io.mju.habux.databinding.ContentMainBinding

const val HABCTL_URL = "http://10.42.0.1:8080"
const val REFRESH_INTERVAL_SECONDS = 5

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var ui: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//
//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)

//        val fab: FloatingActionButton = findViewById(R.id.fab)
//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val drawerLayout: DrawerLayout = ui.drawerLayout
        val navView: NavigationView = ui.navView
        val navController = findNavController(R.id.nav_host_fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_overview, R.id.nav_system, R.id.nav_climate, R.id.nav_geography), drawerLayout)
        navView.setupWithNavController(navController)

        val mainViewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val tick = object: Runnable {
            override fun run() {
                Log.d("Handlers", "tick")
                mainViewModel.requestTelemetry(this@MainActivity)

                handler.postDelayed(this, REFRESH_INTERVAL_SECONDS.toLong() * 1000)
            }
        }
        handler.post(tick)

        mainViewModel.timeSinceLastUpdate.observe(this, Observer { age ->
            if (age != null) {
                ui.textErrorMessage.text = "ERROR: ${mainViewModel.telemetryErrorMessage.value} (data is ${age} s old)"
            } else {
                ui.textErrorMessage.text = ""
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}