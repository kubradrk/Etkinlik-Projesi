package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityUyeBinding

class UyeActivity : AppCompatActivity() {
    lateinit var binding: ActivityUyeBinding
    private lateinit var auth:FirebaseAuth
    var databaseReference:DatabaseReference?=null
    var database:FirebaseDatabase?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding=ActivityUyeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth=FirebaseAuth.getInstance()
        database=FirebaseDatabase.getInstance()
        databaseReference=database?.reference!!.child("profil")
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Kaydet butonu ile üye ekleme
        binding.uyeKaydet.setOnClickListener {
            var uyead=binding.uyeAd.text.toString()
            var uyemail=binding.uyeemail.text.toString()
            var uyeparola=binding.uyeParola.text.toString()
            if(TextUtils.isEmpty(uyead))
            {
                binding.uyeAd.error="Lütfen adınızı ve soyadınızı giriniz"
                return@setOnClickListener
            }
            else if(TextUtils.isEmpty(uyemail))
            {
                binding.uyeAd.error="Lütfen E-mail adresinizi giriniz"
                return@setOnClickListener
            }
            else if(TextUtils.isEmpty(uyeparola))
            {
                binding.uyeAd.error="Lütfen parolanızı giriniz"
                return@setOnClickListener
            }
            //E-mail,parola,kullanıcı bilgilerini veri tabanına ekleme
            auth.createUserWithEmailAndPassword(binding.uyeemail.text.toString(),binding.uyeParola.text.toString())
                .addOnCompleteListener(this) {task ->
                    if(task.isSuccessful) {
                        //Şu anki kullanıcı bilgilerini alalım
                        var currentUser = auth.currentUser
                        //Kullanıcı id alıp o id ile ad soyad kaydetme
                        var currentUserDb=currentUser?.let { it1 -> databaseReference?.child(it1.uid) }
                        currentUserDb?.child("adisoyadi")?.setValue(binding.uyeAd.text.toString())
                        Toast.makeText(this@UyeActivity,"Kayıt Başarılı",Toast.LENGTH_LONG).show()
                    }
                    else{
                        Toast.makeText(this@UyeActivity,"Kayıt Hatalı",Toast.LENGTH_LONG).show()
                    }
                }
        }
        //Giriş sayfasına yönlendirme butonu
        binding.uyegirisYapButton.setOnClickListener {
            intent= Intent(applicationContext,GirisActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}