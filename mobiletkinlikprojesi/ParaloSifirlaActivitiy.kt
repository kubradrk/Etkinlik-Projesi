package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityParaloSifirlaActivitiyBinding
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityProfilBinding

class ParaloSifirlaActivitiy : AppCompatActivity() {
    lateinit var binding: ActivityParaloSifirlaActivitiyBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        val binding=ActivityParaloSifirlaActivitiyBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        auth=FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.parolaButton.setOnClickListener {
            var parolaEmail=binding.parolaEmail.text.toString().trim()
            if(TextUtils.isEmpty(parolaEmail)){
                binding.parolaEmail.error="Lütfen E-mail adresinizi yazınız"
            }
            else{
                auth.sendPasswordResetEmail(parolaEmail)
                    .addOnCompleteListener(this){sifirlama->
                        if(sifirlama.isSuccessful){
                            binding.parolaTextMesaj.text="E-mail adresinize sıfırlama bağlantısı gönderildi, kontrol ediniz."
                        }
                        else{
                            binding.parolaTextMesaj.text="Sıfırlama isteği başarısız."
                        }
                    }
            }
        }
        //Giriş sayfasıne yönlendirme
        binding.parolaGirisButton.setOnClickListener{
            intent= Intent(applicationContext,GirisActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}