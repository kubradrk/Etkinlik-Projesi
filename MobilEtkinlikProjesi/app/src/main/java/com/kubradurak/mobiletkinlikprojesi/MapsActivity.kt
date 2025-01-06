package com.kubradurak.mobiletkinlikprojesi

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
        // Firebase Realtime Database referansı
        database = FirebaseDatabase.getInstance().reference.child("Etkinlik")

        // FusedLocationProviderClient ile kullanıcı konumunu almak için
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Konum izninin verilip verilmediğini kontrol et
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // İzin verilmemişse, kullanıcıdan izin iste
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            // Harita fragment'ını başlat
            startMapFragment()
        }
    }

    private fun startMapFragment() {
        // Harita fragment'ını başlat
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Konum izni kontrolü
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            // Kullanıcının mevcut konumunu al ve haritayı o konuma yönlendir
            getUserLocation().addOnSuccessListener { location ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                } ?: Toast.makeText(this, "Mevcut konum alınamadı", Toast.LENGTH_SHORT).show()
            }
        } else {
            // İzin verilmemişse, izin iste
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }

        // Firebase'den etkinlik verilerini gerçek zamanlı olarak al
        getEtkinlikVerileriniAl()

        // Marker tıklama dinleyicisi
        mMap.setOnMarkerClickListener { marker ->
            val etkinlikKonumu = marker.position
            getEtkinlikBilgisi(etkinlikKonumu)
            true
        }
    }

    private fun getEtkinlikVerileriniAl() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val etkinlikler = mutableListOf<LatLng>()

                for (etkinlikSnapshot in dataSnapshot.children) {
                    val lat = etkinlikSnapshot.child("konum").child("lat").getValue(Double::class.java)
                    val lng = etkinlikSnapshot.child("konum").child("lng").getValue(Double::class.java)

                    if (lat != null && lng != null) {
                        val etkinlikKonumu = LatLng(lat, lng)
                        etkinlikler.add(etkinlikKonumu)

                        val baslik = etkinlikSnapshot.child("baslik").getValue(String::class.java) ?: "Bilinmiyor"
                        val marker = mMap.addMarker(MarkerOptions().position(etkinlikKonumu).title(baslik))
                        marker?.tag = etkinlikSnapshot.key
                    }
                }

                if (etkinlikler.isNotEmpty()) {
                    val firstEvent = etkinlikler.first()
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstEvent, 10f))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapsActivity, "Veri alınırken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getEtkinlikBilgisi(konum: LatLng) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var etkinlikBulundu = false

                for (etkinlikSnapshot in dataSnapshot.children) {
                    val lat = etkinlikSnapshot.child("konum").child("lat").getValue(Double::class.java)
                    val lng = etkinlikSnapshot.child("konum").child("lng").getValue(Double::class.java)

                    if (lat != null && lng != null) {
                        val etkinlikKonumu = LatLng(lat, lng)

                        if (Math.abs(etkinlikKonumu.latitude - konum.latitude) < 0.01 &&
                            Math.abs(etkinlikKonumu.longitude - konum.longitude) < 0.01) {

                            val baslik = etkinlikSnapshot.child("baslık").getValue(String::class.java) ?: "Bilinmiyor"
                            val tarih = etkinlikSnapshot.child("tarih").getValue(String::class.java) ?: "Tarih bilinmiyor"
                            val yer = etkinlikSnapshot.child("yer").getValue(String::class.java) ?: "Yer bilinmiyor"
                            val kategori = etkinlikSnapshot.child("kategori").getValue(String::class.java) ?: "Kategori bilinmiyor"
//                            val hakkinda = etkinlikSnapshot.child("hakkinda").getValue(String::class.java) ?: "Bilgi yok"
                            val aciklama = etkinlikSnapshot.child("aciklama").getValue(String::class.java) ?: "Bilgi yok"

                            val builder = AlertDialog.Builder(this@MapsActivity)
                            builder.setTitle(baslik)
                            builder.setMessage("Kategori: $kategori\nYer: $yer\nTarih: $tarih\n\n$aciklama")

                            // Yön tarifi butonunu ekliyoruz
                            builder.setPositiveButton("Tamam") { dialog, _ -> dialog.dismiss() }

                            // Yön tarifi butonunu ekleyelim
                            builder.setNeutralButton("Yön Tarifi") { _, _ ->
                                showDirections(etkinlikKonumu)  // Yön tarifi göster
                            }

                            builder.create().show()

                            etkinlikBulundu = true
                            break
                        }
                    }
                }

                if (!etkinlikBulundu) {
                    Toast.makeText(this@MapsActivity, "Etkinlik bulunamadı", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MapsActivity, "Veri alınırken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Kullanıcının mevcut konumunu al
    @SuppressLint("MissingPermission")
    private fun getUserLocation(): Task<Location> {
        return fusedLocationClient.lastLocation
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // İzin verildiyse harita fragment'ını başlat
                startMapFragment()
            } else {
                Toast.makeText(this, "Konum izni verilmedi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Yön tarifini almak için
    private fun showDirections(etkinlikKonumu: LatLng) {
        // Konum izni verilmiş mi diye kontrol et
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // İzin verilmişse, mevcut konumu al
            getUserLocation().addOnSuccessListener { location ->
                location?.let {
                    // Mevcut konum bilgilerini alıyoruz
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    // Mevcut konum üzerinde bir işaretçi (marker) ekliyoruz
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Mevcut Konum"))

                    // Kamerayı mevcut konuma yakınlaştırıyoruz
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                    // Yön tarifini almak için Google Maps'ı açıyoruz
                    val uri = "google.navigation:q=${etkinlikKonumu.latitude},${etkinlikKonumu.longitude}&mode=d"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                    intent.setPackage("com.google.android.apps.maps")
                    startActivity(intent)
                } ?: Toast.makeText(this, "Mevcut konum alınamadı", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Konum izni verilmemişse, izin iste
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        }
    }
}

