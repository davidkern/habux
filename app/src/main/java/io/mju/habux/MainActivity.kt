package io.mju.habux

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Menu
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest

val habctl_url = "http://10.42.0.1:8080"

class MainActivity : AppCompatActivity() {
    private lateinit var textSolarPower: TextView
    private lateinit var textLilPower: TextView
    private lateinit var textBigPower: TextView
    private lateinit var textBatteryVoltage: TextView
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.nav_overview, R.id.nav_system, R.id.nav_climate, R.id.nav_geography), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        textSolarPower = findViewById(R.id.textSolarPower)
        textLilPower = findViewById(R.id.textLilPower)
        textBigPower = findViewById(R.id.textBigPower)
        textBatteryVoltage = findViewById(R.id.textBatteryVoltage)

        object : CountDownTimer(Long.MAX_VALUE, 5000) {
            override fun onTick(millisUntilFinished: Long) {
                requestTelemetry()
            }

            override fun onFinish() {
            }
        }.start()
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

    private fun requestTelemetry() =
            Sys.getInstance(this).networkQueue.add(JsonObjectRequest(
                    Request.Method.GET,
                    habctl_url + "/api",
                    null,
                    { response ->
                        val mpptList = response.getJSONArray("mppt")
                        var batteryVoltage = 0.0
                        var solarPower = 0.0

                        for (i in 0 until mpptList.length()) {
                            val mppt = mpptList.getJSONObject(i)
                            val name = mppt.getString("name")
                            val telemetry = mppt.getJSONObject("telemetry")
                            val panelPower = telemetry.getDouble("panel_power")

                            // solar power is sum of all panels
                            solarPower += panelPower
                            // battery voltage is average of both controllers
                            batteryVoltage = batteryVoltage + telemetry.getDouble("battery_voltage")

                            if (name == "lil") {
                                textLilPower.text = panelPower.toString() + " W"
                            } else if (name == "big") {
                                textBigPower.text = panelPower.toString() + " W"
                            }
                        }

                        // average the voltages
                        batteryVoltage = batteryVoltage / mpptList.length()

                        // set cummulative values
                        textBatteryVoltage.text = "%.2f".format(batteryVoltage) + " V";
                        textSolarPower.text = solarPower.toString() + " W";
                    },
                    { error ->
                        // TODO: Handle error
                    }
            ))

}