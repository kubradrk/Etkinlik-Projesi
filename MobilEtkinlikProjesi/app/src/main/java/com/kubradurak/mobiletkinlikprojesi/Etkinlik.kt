package com.kubradurak.mobiletkinlikprojesi

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable
import java.util.Date

data class Etkinlik(
    val id: String = "",
    val baslık: String = "",
    val tarih: String = "",
    val aciklama: String = "",
    //val date: Date = Date(),
    val kategori: String = "",
    val yer: String = "",
    val konum: Konum = Konum()
) : Serializable

data class Konum(
    val lat: Double = 0.0,
    val lng: Double = 0.0
)
