package com.example.finddust.data.models.monitoringstation


import com.google.gson.annotations.SerializedName

data class MonitoringStation(
    @SerializedName("addr")
    val addr: String?,
    @SerializedName("stationName")
    val stationName: String?,
    @SerializedName("tm")
    val tm: Double?
)