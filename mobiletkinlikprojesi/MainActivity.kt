package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityMainBinding
import androidx.appcompat.app.AlertDialog


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private lateinit var etkinlikListesi: MutableList<Etkinlik>
    private lateinit var adapter: EtkinlikAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Firebase Database referansı
        database = FirebaseDatabase.getInstance().reference.child("Etkinlik")
        etkinlikListesi = mutableListOf()

        // RecyclerView ayarları
        adapter = EtkinlikAdapter(etkinlikListesi) { etkinlik ->
            // Tıklanan etkinlik bilgisi burada işlenir
            val fragment = EtkinlikDetayFragment.newInstance(etkinlik)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment) // Fragment kapsayıcı id'si
                .addToBackStack(null)
                .commit()
        }

        binding.etkinlikRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.etkinlikRecyclerView.adapter = adapter

        // Arama fonksiyonu
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Arama kutusu boşsa, verileri Firebase'den çek
                    veriCekFirebase()
                } else {
                    // Arama metnine göre filtrele
                    val filteredList = etkinlikListesi.filter { etkinlik ->
                        etkinlik.baslık.contains(newText, ignoreCase = true) ||
                                etkinlik.tarih.contains(newText, ignoreCase = true) ||
                                etkinlik.yer.contains(newText, ignoreCase = true)
                    }
                    adapter.updateList(filteredList)
                }
                return true
            }
        })

        binding.filterButton.setOnClickListener {
            val kategoriler = arrayOf("Konser", "Stand-up", "Tiyatro", "Konferans")
            var selectedKategori: String? = null

            AlertDialog.Builder(this)
                .setTitle("Kategori Seçin")
                .setSingleChoiceItems(kategoriler, -1) { _, which ->
                    selectedKategori = kategoriler[which] // Seçilen kategoriyi alıyoruz
                }
                .setPositiveButton("Filtrele") { _, _ ->
                    veriCekFirebase { // Veriler çekildikten sonra çalışacak bir callback ekledik
                        if (selectedKategori != null) {
                            val filteredList = etkinlikListesi.filter { it.kategori == selectedKategori }
                            adapter.updateList(filteredList) // Filtrelenmiş listeyi göster
                        } else {
                            adapter.updateList(etkinlikListesi) // Eğer kategori seçilmediyse tüm listeyi göster
                        }
                    }
                }
                .setNegativeButton("İptal", null)
                .show()
        }
        // Verileri Firebase'den çek
        veriCekFirebase()


        binding.mainProfilButton.setOnClickListener {
            val intent = Intent(applicationContext, ProfilActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.mainMapButton.setOnClickListener {
            val intent = Intent(applicationContext, MapsActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.mainBildirimButton.setOnClickListener {
            val intent = Intent(applicationContext, Bildirimler::class.java)
            startActivity(intent)
            finish()
        }
        binding.mainFavoriButton.setOnClickListener {
            val intent = Intent(applicationContext, FavorilerActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Firebase'den veri çekme fonksiyonu
    private fun veriCekFirebase(onComplete: (() -> Unit)? = null) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                etkinlikListesi.clear()
                for (etkinlikSnapshot in snapshot.children) {
                    val etkinlik = etkinlikSnapshot.getValue(Etkinlik::class.java)
                    if (etkinlik != null) {
                        etkinlikListesi.add(etkinlik)
                    }
                }
                adapter.notifyDataSetChanged()
                onComplete?.invoke() // Veriler çekildikten sonra callback'i çalıştır
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Veri çekme hatası: ${error.message}")
            }
        })
    }
}