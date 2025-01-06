package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityBilgiGuncelleBinding

class BilgiGuncelleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBilgiGuncelleBinding
    private lateinit var auth: FirebaseAuth
    private var databaseReference: DatabaseReference? = null
    private var database: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBilgiGuncelleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("profil")

        val currentUser = auth.currentUser

        // Kaydet butonuna tıklanmasıyla güncelleme işlemi yapılacak
        binding.guncelleKaydet.setOnClickListener {
            val yeniAdSoyad = binding.guncelleAd.text.toString()
            val yeniEmail = binding.guncelleEmail.text.toString()
            val yeniParola = binding.guncelleParola.text.toString()

            // Geçerli verilerin olup olmadığını kontrol et
            if (yeniAdSoyad.isNotEmpty() && yeniEmail.isNotEmpty() && yeniParola.isNotEmpty()) {

                // Email güncelleme işlemi
                currentUser?.updateEmail(yeniEmail)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email güncellendi.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Email güncellenemedi.", Toast.LENGTH_SHORT).show()
                        }
                    }

                // Parola güncelleme işlemi
                currentUser?.updatePassword(yeniParola)
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Parola güncellendi.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Parola güncellenemedi.", Toast.LENGTH_SHORT).show()
                        }
                    }

                // Ad-Soyad bilgisini Firebase'e kaydet
                currentUser?.uid?.let { uid ->
                    val userReference = databaseReference?.child(uid)
                    userReference?.child("adisoyadi")?.setValue(yeniAdSoyad)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Bilgiler başarıyla güncellendi", Toast.LENGTH_SHORT).show()
                                finish() // Aktiviteyi bitirerek önceki ekrana dön
                            } else {
                                Toast.makeText(this, "Bilgiler güncellenemedi", Toast.LENGTH_SHORT).show()
                            }
                        }
                }

            } else {
                Toast.makeText(this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onBackPressed() {
        val intent = Intent(this, ProfilActivity::class.java)
        startActivity(intent)
        finish()  // Mevcut Activity'yi sonlandırıyoruz
        super.onBackPressed()  // Varsayılan geri tuşu davranışını sağlıyoruz
    }
}
