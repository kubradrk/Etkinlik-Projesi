package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityBildirimlerBinding

class Bildirimler : AppCompatActivity() {

    lateinit var binding: ActivityBildirimlerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBildirimlerBinding.inflate(layoutInflater) // Doğru binding kullanımı
        setContentView(binding.root)

        // System insets ayarları
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Button tıklama işlemleri
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
    }

    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()  // Mevcut Activity'yi sonlandırıyoruz
        super.onBackPressed()  // Varsayılan geri tuşu davranışını sağlıyoruz
    }
}
