package com.kubradurak.mobiletkinlikprojesi

import com.google.android.gms.maps.model.LatLng

data class LatLngWrapper(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0
) {
    // Bu fonksiyon, wrapper sınıfını LatLng nesnesine dönüştürür
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    // Bu fonksiyon, LatLng nesnesini wrapper sınıfına dönüştürür
    companion object {
        fun fromLatLng(latLng: LatLng): LatLngWrapper {
            return LatLngWrapper(latLng.latitude, latLng.longitude)
        }
    }
}
