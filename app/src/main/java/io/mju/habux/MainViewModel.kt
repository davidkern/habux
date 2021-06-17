package io.mju.habux

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest

class MainViewModel : ViewModel() {
    // Errors
    private val _secondsSinceLastUpdate = MutableLiveData<Int>()
    private val _telemetryErrorMessage = MutableLiveData<String>()

    val timeSinceLastUpdate: LiveData<Int>
        get() = _secondsSinceLastUpdate

    val telemetryErrorMessage: LiveData<String>
        get() = _telemetryErrorMessage

    // MPPT data
    private val _solarPower = MutableLiveData<Int>()
    private val _lilPower = MutableLiveData<Int>()
    private val _bigPower = MutableLiveData<Int>()
    private val _batteryVoltage = MutableLiveData<Double>()

    val solarPower: LiveData<Int>
        get() = _solarPower

    val lilPower: LiveData<Int>
        get() = _lilPower

    val bigPower: LiveData<Int>
        get() = _bigPower

    val batteryVoltage: LiveData<Double>
        get() = _batteryVoltage

    fun requestTelemetry(context: Context) {
        Sys.getInstance(context).networkQueue.add(JsonObjectRequest(
                Request.Method.GET,
                HABCTL_URL + "/api",
                null,
                { response ->
                    val mpptList = response.getJSONArray("mppt")
                    var batteryVoltage = 0.0
                    var solarPower = 0

                    // process mppt devices
                    for (i in 0 until mpptList.length()) {
                        val mppt = mpptList.getJSONObject(i)
                        val name = mppt.getString("name")
                        val telemetry = mppt.getJSONObject("telemetry")
                        val panelPower = telemetry.getInt("panel_power")

                        // solar power is sum of all panels
                        solarPower += panelPower
                        // battery voltage is average of both controllers
                        batteryVoltage = batteryVoltage + telemetry.getDouble("battery_voltage")

                        if (name == "lil") {
                            this._lilPower.value = panelPower;
                        } else if (name == "big") {
                            this._bigPower.value = panelPower;
                        }
                    }

                    // save accumulated values
                    this._batteryVoltage.value = batteryVoltage / mpptList.length()
                    this._solarPower.value = solarPower;

                    // clear errors
                    this._secondsSinceLastUpdate.value = null;
                    this._telemetryErrorMessage.value = "";
                },
                { error ->
                    this._telemetryErrorMessage.value = error.message

                    val time = this._secondsSinceLastUpdate.value
                    if (time != null) {
                        this._secondsSinceLastUpdate.value = time + REFRESH_INTERVAL_SECONDS;
                    } else {
                        this._secondsSinceLastUpdate.value = REFRESH_INTERVAL_SECONDS;
                    }
                }
        ))
    }
}
