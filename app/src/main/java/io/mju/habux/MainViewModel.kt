package io.mju.habux

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONException

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

    // Orientation data
    private val _accelerometer = MutableLiveData<DoubleArray>()
    private val _gyrometer = MutableLiveData<DoubleArray>()
    private val _magnetometer = MutableLiveData<DoubleArray>()

    val accelerometer: LiveData<DoubleArray>
        get() = _accelerometer

    val gyrometer: LiveData<DoubleArray>
        get() = _gyrometer

    val magnetometer: LiveData<DoubleArray>
        get() = _magnetometer

    // Temperature data
    private val _underCounterTemperature = MutableLiveData<Double>()

    val underCounterTemp: LiveData<Double>
        get() = _underCounterTemperature

    fun requestTelemetry(context: Context) {
        Sys.getInstance(context).networkQueue.add(JsonObjectRequest(
                Request.Method.GET,
                HABCTL_URL + "/api",
                null,
                { response ->
                    var batteryVoltage = 0.0
                    var solarPower = 0
                    var updatedBig = false
                    var updatedLil = false
                    var updatedImu = false

                    // process mppt devices
                    val mpptList = response.getJSONArray("mppt")
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
                            updatedLil = true
                        } else if (name == "big") {
                            this._bigPower.value = panelPower;
                            updatedBig = true
                        }
                    }

                    // process imu devices
                    var imuList = response.getJSONArray("imu")
                    for (i in 0 until imuList.length()) {
                        val imu = imuList.getJSONObject(i)
                        val name = imu.getString("name")

                        if (name == "hab") {
                            val telemetry = imu.getJSONObject("telemetry")

                            // read accelerometer
                            try {
                                val accelerometer = telemetry.getJSONArray("accelerometer")
                                val accel = doubleArrayOf(
                                    accelerometer.getDouble(0),
                                    accelerometer.getDouble(1),
                                    accelerometer.getDouble(2))

                                this._accelerometer.value = accel
                            } catch (e: JSONException) {
                                this._accelerometer.value = null
                            }

                            // read gyrometer
                            try {
                                val gyrometer = telemetry.getJSONArray("gyrometer")
                                val gyro = doubleArrayOf(
                                    gyrometer.getDouble(0),
                                    gyrometer.getDouble(1),
                                    gyrometer.getDouble(2))

                                this._gyrometer.value = gyro
                            } catch (e: JSONException) {
                                this._gyrometer.value = null
                            }

                            // read magnetometer
                            try {
                                val magnetometer = telemetry.getJSONArray("magnetometer")
                                val mag = doubleArrayOf(
                                    magnetometer.getDouble(0),
                                    magnetometer.getDouble(1),
                                    magnetometer.getDouble(2))

                                this._magnetometer.value = mag
                            } catch (e: JSONException) {
                                this._magnetometer.value = null
                            }

                            // read temperature
                            try {
                                val temp = telemetry.getDouble("temperature")

                                this._underCounterTemperature.value = temp
                            } catch (e: JSONException) {
                                this._underCounterTemperature.value = null
                            }

                            updatedImu = true
                        }
                    }

                    // save accumulated values
                    this._batteryVoltage.value = batteryVoltage / mpptList.length()
                    this._solarPower.value = solarPower;

                    // clear errors
                    this._secondsSinceLastUpdate.value = null;
                    this._telemetryErrorMessage.value = "";

                    // clear unread data
                    if (!updatedBig) {
                        this._bigPower.value = null
                    }

                    if (!updatedLil) {
                        this._lilPower.value = null
                    }

                    if (!updatedImu) {
                        this._accelerometer.value = null
                        this._gyrometer.value = null
                        this._magnetometer.value = null
                        this._underCounterTemperature.value = null
                    }
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
