package com.gurumi.weather.model

data class WEATHER(val response: RESPONSE)

data class RESPONSE(val header: HEADER, val body: BODY)

data class HEADER(val resultCode: Int, val resultMessage: String)

data class BODY(val dataType: String, val items: ITEMS)

data class ITEMS(val item: List<ITEM>)

data class ITEM(
    val category: String,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String
)
