package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityFavorilerBinding
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavorilerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavorilerBinding
    private lateinit var database: DatabaseReference
    private lateinit var favoriListesi: MutableList<Etkinlik>
    private lateinit var adapter: EtkinlikAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavorilerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainProfilButton.setOnClickListener {
            val intent = Intent(applicationContext, ProfilActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.mainMapButton.setOnClickListener {
            val intent = Intent(applicationContext, HaritaActivity::class.java)
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
        // Firebase referansı ve kullanıcı kimliği
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            // Kullanıcı oturum açmamışsa
            Log.e("FavorilerActivity", "Kullanıcı oturum açmamış.")
            finish()
            return
        }

        database = FirebaseDatabase.getInstance().reference.child("Favoriler").child(userId)
        favoriListesi = mutableListOf()

        // RecyclerView ayarları
        adapter = EtkinlikAdapter(favoriListesi) { etkinlik ->
            // Favorilerde bir etkinliğe tıklandığında yapılacak işlemler
            // Örneğin etkinlik detayına yönlendirme
        }
        binding.favorilerRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.favorilerRecyclerView.adapter = adapter

        // Firebase'den favorileri çek
        favorileriCek()
    }

    private fun favorileriCek(onComplete: (() -> Unit)? = null) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    favoriListesi.clear() // Önce listeyi temizle

                    for (etkinlikSnapshot in snapshot.children) {
                        // Firebase'den alınan veriyi kontrol et
                        val etkinlik = etkinlikSnapshot.getValue(Etkinlik::class.java)
                        if (etkinlik != null) {
                            favoriListesi.add(etkinlik)
                        }
                    }

                    // UI'yı ana thread üzerinde güncelle
                    runOnUiThread {
                        adapter.notifyDataSetChanged() // RecyclerView verilerini güncelle
                    }

                    onComplete?.invoke() // Callback fonksiyonunu çalıştır

                } catch (e: Exception) {
                    Log.e("Firebase", "Veri çekme işlemi sırasında hata oluştu: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Veri çekme hatası: ${error.message}")
            }
        })
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Mevcut Activity'yi sonlandırıyoruz
        super.onBackPressed()  // Varsayılan geri tuşu davranışını sağlıyoruz
    }

}
