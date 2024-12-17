package com.gurumi.weather

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.widget.ProgressBar
import com.gurumi.weather.utils.ConvertToGridUtils
import com.gurumi.weather.utils.LocationUtils.checkLocationPermission
import com.gurumi.weather.utils.LocationUtils.getLastLocation
import com.gurumi.weather.utils.LocationUtils.requestLocationPermission
import com.gurumi.weather.utils.TimeUtils.startUpdatingTime
import com.gurumi.weather.utils.WeatherUtils

private lateinit var fusedLocationClient: FusedLocationProviderClient

class MainActivity : AppCompatActivity() {

    lateinit var temp_tv : TextView
    lateinit var tempMorning_tv : TextView
    lateinit var tempDayTime_tv : TextView
    lateinit var humidity_tv : TextView
    lateinit var sky_tv : TextView
    lateinit var rain_tv : TextView
    lateinit var rainType_tv : TextView
    lateinit var progressBar: ProgressBar

    lateinit var dateToday_tv: TextView
    lateinit var timeNow_tv: TextView
    val handler = Handler(Looper.getMainLooper())

    var nx = "55"
    var ny = "127"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (checkLocationPermission(this)) {
            getLastLocation()
        } else {
            requestLocationPermission(this)
        }

        temp_tv = findViewById(R.id.temp_tv)
        tempMorning_tv = findViewById(R.id.tempMorning_tv)
        tempDayTime_tv = findViewById(R.id.tempDayTime_tv)
        humidity_tv = findViewById(R.id.humidity_tv)
        sky_tv = findViewById(R.id.sky_tv)
        rain_tv = findViewById(R.id.rain_tv)
        rainType_tv = findViewById(R.id.rainType_tv)

        // To show date and time
        dateToday_tv = findViewById(R.id.dateToday)
        timeNow_tv = findViewById(R.id.timeNow)

        // progress bar
        progressBar = findViewById(R.id.progressBar)

        startUpdatingTime(dateToday_tv, timeNow_tv)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    private fun getLastLocation() {
        getLastLocation(
            this,
            fusedLocationClient,
            onLocationReceived = { latitude, longitude ->
                val gridCoordinate = ConvertToGridUtils.convertToGrid(latitude, longitude)
                nx = gridCoordinate.first.toString()
                ny = gridCoordinate.second.toString()
                getWeatherData(nx, ny)
            },
            onPermissionDenied = {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun getWeatherData (nx: String, ny: String) {
        progressBar.visibility = ProgressBar.VISIBLE

        WeatherUtils.getWeather(this, nx, ny, progressBar) { temp, tempMin, tempDayTime, humidity, sky, rain, rainType ->
            WeatherUtils.setWeather(temp_tv, tempMorning_tv, tempDayTime_tv, humidity_tv, sky_tv, rain_tv, rainType_tv, temp, tempMin, tempDayTime, humidity, sky, rain, rainType)
        }

        WeatherUtils.setWeeklyWeather(this, nx, ny, progressBar) { dayIndex, date, tempMin, tempMax, rainType ->
            updateUIForDay(dayIndex, date, tempMin, tempMax, rainType)
        }

    }

    private fun updateUIForDay(dayIndex: Int, date: String, tempMin: String, tempMax: String, rainType: String) {
        val rainResult = WeatherUtils.getRainResult(rainType)

        when (dayIndex) {
            0 -> updateDayUI(R.id.day1, R.id.day1_min_max, R.id.day1_rainType, date, tempMin, tempMax, rainResult)
            1 -> updateDayUI(R.id.day2, R.id.day2_min_max, R.id.day2_rainType, date, tempMin, tempMax, rainResult)
            2 -> updateDayUI(R.id.day3, R.id.day3_min_max, R.id.day3_rainType, date, tempMin, tempMax, rainResult)
            else -> {
                Log.d("Error", "Error")
            }
        }
    }

    private fun updateDayUI(dayId: Int, minMaxId: Int, rainTypeId: Int, date: String, tempMin: String, tempMax: String, rainResult: String) {
        val currentCalendar = Calendar.getInstance()
        val day = date.toInt()

        currentCalendar.set(Calendar.DAY_OF_MONTH, day)
        val fullDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(currentCalendar.time)

        if (dayId != R.id.day1) {
            findViewById<TextView>(dayId).apply {
                setOnClickListener {
                    val intent = Intent(this@MainActivity, SelectedWeatherActivity::class.java)
                    intent.putExtra("DATE", fullDate)
                    intent.putExtra("DAY", date)
                    intent.putExtra("NX", nx)
                    intent.putExtra("NY", ny)
                    startActivity(intent)
                }
            }
        }
        findViewById<TextView>(dayId).text = "$date 일"
        findViewById<TextView>(minMaxId).text = "$tempMin°C / $tempMax°C"
        findViewById<TextView>(rainTypeId).text = rainResult
    }
}
