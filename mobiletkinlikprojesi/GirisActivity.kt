package com.kubradurak.mobiletkinlikprojesi

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.kubradurak.mobiletkinlikprojesi.databinding.ActivityGirisBinding


class GirisActivity : AppCompatActivity() {
    lateinit var binding: ActivityGirisBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGirisBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Kullanıcının oturum açıp açmadığının kontrolü
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this@GirisActivity, MainActivity::class.java))
            finish()
        }

        // Giriş yap butonuna tıklandığında
        binding.girisYapButton.setOnClickListener {
            val girisEmail = binding.girisEmail.text.toString()
            val girisParalo = binding.girisParola.text.toString()
            if (TextUtils.isEmpty(girisEmail)) {
                binding.girisEmail.error = "Lütfen E-mail adresini giriniz."
                return@setOnClickListener
            } else if (TextUtils.isEmpty(girisParalo)) {
                binding.girisParola.error = "Lütfen parolanızı giriniz."
                return@setOnClickListener
            }

            // Giriş bilgilerini doğrulama
            auth.signInWithEmailAndPassword(girisEmail, girisParalo)
                .addOnCompleteListener(this) {
                    if (it.isSuccessful) {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "Giriş hatalı. Lütfen tekrar deneyiniz", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Yeni üyelik sayfasına gitmek için
        binding.girisYeniUyelik.setOnClickListener {
            val intent = Intent(applicationContext, UyeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Parolamı unuttum sayfasına gitmek için
        binding.girisParolaUnuttum.setOnClickListener {
            val intent = Intent(applicationContext, ParaloSifirlaActivitiy::class.java)
            startActivity(intent)
            finish()
        }

        // Google ile giriş işlemi için istemci ayarları
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        // Google ile giriş butonuna tıklama olayı
        binding.googleSignInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Google giriş sonucu işleme
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken!!)
                } else {
                    Toast.makeText(this, "Google ile giriş başarısız", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Google ile giriş başarısız: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Kullanıcı bilgilerini Realtime Database'e kaydetmek için UID'yi al
                    val userId = user?.uid
                    val displayName = user?.displayName
                    val email = user?.email

                    // Realtime Database referansını al
                    val database = FirebaseDatabase.getInstance()
                    val userReference = database.getReference("profil").child(userId!!)

                    // Kullanıcı adı soyadı verisini ekle
                    val userMap = mapOf(
                        "adisoyadi" to displayName // Burada sadece adı soyadı ekliyoruz
                    )

                    // Veriyi Realtime Database'e ekle
                    userReference.setValue(userMap)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Profil başarıyla oluşturuldu", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@GirisActivity, MainActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(this, "Profil oluşturulurken bir hata oluştu", Toast.LENGTH_SHORT).show()
                            }
                        }

                    Toast.makeText(
                        this,
                        "Google ile giriş başarılı: ${user?.displayName}",
                        Toast.LENGTH_LONG
                    ).show()
                    startActivity(Intent(this@GirisActivity, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Firebase ile giriş başarısız", Toast.LENGTH_LONG).show()
                }
            }
    }

    companion object {
        private const val RC_SIGN_IN = 100
    }
}
