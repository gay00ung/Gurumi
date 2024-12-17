package com.gurumi.weather.utils

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object TimeUtils {
    val handler = Handler(Looper.getMainLooper())

    fun startUpdatingTime(dateToday: TextView, timeNow: TextView) {
        handler.post(object : Runnable {
            override fun run() {
                val currentDate = SimpleDateFormat(
                    "yyyy년 MM월 dd일",
                    Locale.getDefault()).format(Calendar.getInstance().time
                    )
                val currentTime = SimpleDateFormat(
                    "HH시 mm분",
                    Locale.getDefault()).format(Calendar.getInstance().time
                    )

                dateToday.text = currentDate
                timeNow.text = currentTime

                handler.postDelayed(this, 1000)
            }
        })
    }
}