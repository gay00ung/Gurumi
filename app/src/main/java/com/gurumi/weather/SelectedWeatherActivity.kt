package com.gurumi.weather

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.gurumi.weather.utils.WeatherUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private lateinit var fusedLocationClient: FusedLocationProviderClient

class SelectedWeatherActivity : AppCompatActivity() {

    lateinit var title_tv : TextView

    lateinit var tempMorning_tv : TextView
    lateinit var tempDayTime_tv : TextView
    lateinit var humidity_tv : TextView
    lateinit var sky_tv : TextView
    lateinit var rain_tv : TextView
    lateinit var rainType_tv : TextView
    lateinit var progressBar: ProgressBar

    val handler = Handler(Looper.getMainLooper())

    var nx = "55"
    var ny = "127"

    var selectedDate: String = "날짜 없음"
    var selectedDay: String = "날짜 없음"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_weather)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        selectedDate = intent.getStringExtra("DATE") ?: "날짜 없음"
        Log.d("!!!!!!1", "Selected Date: $selectedDate")
        selectedDay = intent.getStringExtra("DAY") ?: "날짜 없음"

        title_tv = findViewById(R.id.titleText)

        tempMorning_tv = findViewById(R.id.tempMorning_tv)
        tempDayTime_tv = findViewById(R.id.tempDayTime_tv)
        humidity_tv = findViewById(R.id.humidity_tv)
        sky_tv = findViewById(R.id.sky_tv)
        rain_tv = findViewById(R.id.rain_tv)
        rainType_tv = findViewById(R.id.rainType_tv)

        // progress bar
        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = ProgressBar.VISIBLE
        WeatherUtils.getSelectedWeather(this, nx, ny, progressBar, selectedDate) { tempMin, tempDayTime, humidity, sky, rain, rainType ->
            WeatherUtils.setSelectedWeather(tempMorning_tv, tempDayTime_tv, humidity_tv, sky_tv, rain_tv, rainType_tv, tempMin, tempDayTime, humidity, sky, rain, rainType)
        }

        WeatherUtils.setWeeklyWeather(this, nx, ny, progressBar) { dayIndex, date, tempMin, tempMax, rainType ->
            updateUIForDay(dayIndex, date, tempMin, tempMax, rainType)
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
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
                title_tv.text = "☀️ $selectedDay 일의 날씨 정보 입니다. 🌤️"
                setOnClickListener {
                    val intent =
                        Intent(this@SelectedWeatherActivity, SelectedWeatherActivity::class.java)
                    intent.putExtra("DATE", fullDate)
                    intent.putExtra("DAY", date)
                    startActivity(intent)
                }
            }
        } else {
            findViewById<TextView>(dayId).apply {
                setOnClickListener {
                    val intent = Intent(this@SelectedWeatherActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        findViewById<TextView>(dayId).text = "$date 일"
        findViewById<TextView>(minMaxId).text = "$tempMin°C / $tempMax°C"
        findViewById<TextView>(rainTypeId).text = rainResult
    }
}