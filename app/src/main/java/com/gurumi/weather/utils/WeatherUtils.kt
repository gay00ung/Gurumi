package com.gurumi.weather.utils

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.gurumi.weather.model.ITEM
import com.gurumi.weather.model.WEATHER
import com.gurumi.weather.network.WeatherInterface
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object WeatherUtils {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: WeatherInterface by lazy {
        retrofit.create(WeatherInterface::class.java)
    }

    fun setWeather(
        temp_tv: TextView,
        tempMorning_tv: TextView,
        tempDayTime_tv: TextView,
        humidity_tv: TextView,
        sky_tv: TextView,
        rain_tv: TextView,
        rainType_tv: TextView,
        temp: String,
        tempMorning: String,
        tempDayTime: String,
        humidity: String,
        sky: String,
        rain: String,
        rainType: String
    ) {
        temp_tv.text = "$temp°C"
        tempMorning_tv.text = "$tempMorning°C"
        tempDayTime_tv.text = "$tempDayTime°C"
        humidity_tv.text = "$humidity%"

        val skyResult = when(sky) {
            "1" -> "맑음 ☀️"
            "3" -> "구름 많음 ⛅️"
            "4" -> "흐림 ☁️"
            else -> "Error"
        }
        sky_tv.text = skyResult

        rain_tv.text = "$rain%"

        val rainResult = getRainResult(rainType)
        rainType_tv.text = rainResult
    }

    fun getWeather(context: Context, nx: String, ny: String, progressBar: ProgressBar,
                   callback: (String, String, String, String, String, String, String) -> Unit) {
        val cal = Calendar.getInstance()
        var base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        val time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time)
        var base_time = getTime(time)

        if(base_time >= "2000") { // PM8 기준
            cal.add(Calendar.DATE, -1)
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }

        val fcst_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        val fcst_time = SimpleDateFormat("HHmm", Locale.getDefault()).format(cal.time)

        val call = apiService.getWeather(1, 10000, "JSON", base_date, base_time, fcst_date, fcst_time, nx, ny)
        call.enqueue(object: Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                progressBar.visibility = ProgressBar.GONE
                if(response.isSuccessful) {
                    val it: List<ITEM> = response.body()!!.response.body.items.item
                    var temp = ""
                    var tempMin = ""
                    var tempDayTime = ""
                    var humidity = ""
                    var sky = ""
                    var rain = ""
                    var rainType = ""
                    for (item in it) {
                        when (item.category) {
                            "TMP" -> temp = item.fcstValue
                            "TMN" -> tempMin = item.fcstValue
                            "TMX" -> tempDayTime = item.fcstValue
                            "REH" -> humidity = item.fcstValue
                            "SKY" -> sky = item.fcstValue
                            "POP" -> rain = item.fcstValue
                            "PTY" -> rainType = item.fcstValue
                            else -> continue
                        }
                    }
                    callback(temp, tempMin, tempDayTime, humidity, sky, rain, rainType)
                    Toast.makeText(context, "${it[0].fcstDate}, ${it[0].fcstTime}의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context, "날씨 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getSelectedWeather(context: Context, nx: String, ny: String, progressBar: ProgressBar, fcstDate: String,
                   callback: (String, String, String, String, String, String) -> Unit) {
        val cal = Calendar.getInstance()
        var base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        val base_time = "0200"

        val fcst_date = fcstDate
        val fcst_time = SimpleDateFormat("HHmm", Locale.getDefault()).format(cal.time)

        val call = apiService.getWeather(1, 10000, "JSON", base_date, base_time, fcst_date, fcst_time, nx, ny)
        call.enqueue(object: Callback<WEATHER> {
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                progressBar.visibility = ProgressBar.GONE
                if(response.isSuccessful) {
                    val it: List<ITEM> = response.body()!!.response.body.items.item
                    val filteredItems = it.filter { it.fcstDate == fcst_date }
                    if (filteredItems.isEmpty()) {
                        Toast.makeText(context, "선택한 날짜에 대한 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    var tempMin = ""
                    var tempDayTime = ""
                    var humidity = ""
                    var sky = ""
                    var rain = ""
                    var rainType = ""

                    for (item in filteredItems) {
                        when (item.category) {
                            "TMN" -> tempMin = item.fcstValue
                            "TMX" -> tempDayTime = item.fcstValue
                            "REH" -> humidity = item.fcstValue
                            "SKY" -> sky = item.fcstValue
                            "POP" -> rain = item.fcstValue
                            "PTY" -> rainType = item.fcstValue
                            else -> continue
                        }
                    }
                    callback(tempMin, tempDayTime, humidity, sky, rain, rainType)
                    Toast.makeText(context, "${fcstDate}, ${fcst_time}의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                progressBar.visibility = ProgressBar.GONE
                Toast.makeText(context, "날씨 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun setSelectedWeather(
        tempMorning_tv: TextView,
        tempDayTime_tv: TextView,
        humidity_tv: TextView,
        sky_tv: TextView,
        rain_tv: TextView,
        rainType_tv: TextView,
        tempMorning: String,
        tempDayTime: String,
        humidity: String,
        sky: String,
        rain: String,
        rainType: String
    ) {
        tempMorning_tv.text = "$tempMorning°C"
        tempDayTime_tv.text = "$tempDayTime°C"
        humidity_tv.text = "$humidity%"

        val skyResult = when(sky) {
            "1" -> "맑음 ☀️"
            "3" -> "구름 많음 ⛅️"
            "4" -> "흐림 ☁️"
            else -> "Error"
        }
        sky_tv.text = skyResult

        rain_tv.text = "$rain%"

        val rainResult = getRainResult(rainType)
        rainType_tv.text = rainResult
    }


    fun setWeeklyWeather(context: Context, nx: String, ny: String, progressBar: ProgressBar,
                         updateUI: (Int, String, String, String, String) -> Unit) {
        progressBar.visibility = View.VISIBLE
        val base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Calendar.getInstance().time)
        val base_time = "0200"

        for (i in 0..2) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DATE, i)
            val fcstDate = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
            val currentFcstDate = fcstDate
            val currentIndex = i
            val dayOfMonth = SimpleDateFormat("dd", Locale.getDefault()).format(cal.time)

            val call = apiService.getWeather(1, 10000, "JSON", base_date, base_time, fcstDate, base_time, nx, ny)

            call.enqueue(object : Callback<WEATHER> {
                override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                    progressBar.visibility = ProgressBar.GONE
                    if (response.isSuccessful) {
                        val it: List<ITEM> = response.body()!!.response.body.items.item
                        var tempMin = ""
                        var tempMax = ""
                        var rainType = ""
                        val filteredItems = it.filter { it.fcstDate == currentFcstDate }
                        for (item in filteredItems) {
                            when (item.category) {
                                "TMN" -> tempMin = item.fcstValue
                                "TMX" -> tempMax = item.fcstValue
                                "PTY" -> if (rainType.isEmpty()) rainType = item.fcstValue
                                else -> continue
                            }
                        }
                        updateUI(currentIndex, dayOfMonth, tempMin, tempMax, rainType)
                    }
                }

                override fun onFailure(call: Call<WEATHER>, t: Throwable) {
                    Toast.makeText(context, "날씨 정보를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = ProgressBar.GONE
                }
            })
        }
    }

    fun getRainResult(rainType: String): String {
        return when(rainType) {
            "0" -> "❌"
            "1" -> "비 ☔️"
            "2" -> "비 ☔️/눈 ❄️"
            "3" -> "눈 ❄️"
            "4" -> "소나기 🌧️"
            "5" -> "빗방울 💧"
            "6" -> "빗방울 💧/눈날림 🌨️"
            "7" -> "눈날림 🌨️"
            else -> "Error"
        }
    }

    fun getTime(time : String) : String {
        var result = ""
        when(time) {
            in "00".."02" -> result = "2000"    // 00~02
            in "03".."05" -> result = "2300"    // 03~05
            in "06".."08" -> result = "0200"    // 06~08
            in "09".."11" -> result = "0500"    // 09~11
            in "12".."14" -> result = "0800"    // 12~14
            in "15".."17" -> result = "1100"    // 15~17
            in "18".."20" -> result = "1400"    // 18~20
            else -> result = "1700"             // 21~23
        }
        return result
    }
}