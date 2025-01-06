package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityProfilBinding

class ProfilActivity : AppCompatActivity() {

    lateinit var binding: ActivityProfilBinding
    private lateinit var auth: FirebaseAuth
    var databaseReference: DatabaseReference? = null
    var database: FirebaseDatabase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityProfilBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        databaseReference = database?.reference!!.child("profil")

        val currentUser = auth.currentUser

        // Kullanıcı bilgilerini göster
        binding.profilEmail.text = "E-mail: " + currentUser?.email
        currentUser?.uid?.let { uid ->
            val userReference = databaseReference?.child(uid)
            userReference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val adSoyad = snapshot.child("adisoyadi").value?.toString() ?: "Veri bulunamadı"
                    binding.profilAdSoyad.text = "Tam adınız: $adSoyad"
                }

                override fun onCancelled(error: DatabaseError) {
                    // Hata durumunda yapılacak işlemler
                }
            })
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bildirim Switch İşlevi
        binding.profilBildirimSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch etkinleştirildiğinde mesaj göster
                Toast.makeText(
                    this,
                    "Uygulamamızdan bildirim alacaksınız.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Switch devre dışı bırakıldığında mesaj göster
                Toast.makeText(
                    this,
                    "Uygulama bildirimleri devre dışı bırakıldı.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Çıkış yap
        binding.profilCikis.setOnClickListener {
            auth.signOut()
            val intent = Intent(this@ProfilActivity, GirisActivity::class.java)
            startActivity(intent)
            finish()
        }
        // Bilgilerimi Güncelle butonuna tıklama işlevi
        binding.ProfilGuncelle.setOnClickListener {
            // Bilgi Güncelleme Activity'sine yönlendir
            val intent = Intent(this, BilgiGuncelleActivity::class.java)
            startActivity(intent)
        }

        // Hesap sil
        binding.profilSil.setOnClickListener {
            currentUser?.delete()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(applicationContext, "Hesabınız silindi", Toast.LENGTH_LONG).show()
                    auth.signOut()
                    startActivity(Intent(this@ProfilActivity, GirisActivity::class.java))
                    finish()
                }
            }
        }

        // Alt menü butonları
        binding.mainProfilButton.setOnClickListener {
            startActivity(Intent(this, ProfilActivity::class.java))
            finish()
        }
        binding.mainMapButton.setOnClickListener {
            startActivity(Intent(this, HaritaActivity::class.java))
            finish()
        }
        binding.mainBildirimButton.setOnClickListener {
            startActivity(Intent(this, Bildirimler::class.java))
            finish()
        }
        binding.mainFavoriButton.setOnClickListener {
            startActivity(Intent(this, FavorilerActivity::class.java))
            finish()
        }
    }

    // Geri tuşu ile MainActivity'ye dön
    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
