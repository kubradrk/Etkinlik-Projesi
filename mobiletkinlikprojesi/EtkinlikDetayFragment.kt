package com.kubradurak.mobiletkinlikprojesi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kubradurak.mobiletkinlikprojesi.databinding.FragmentEtkinlikDetayBinding

class EtkinlikDetayFragment : Fragment() {

    private var _binding: FragmentEtkinlikDetayBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_ETKINLIK = "etkinlik"
        private const val ARG_ETKINLIK_ID = "etkinlikId"

        fun newInstance(etkinlik: Etkinlik): EtkinlikDetayFragment {
            val fragment = EtkinlikDetayFragment()
            val args = Bundle()
            args.putSerializable(ARG_ETKINLIK, etkinlik)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEtkinlikDetayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ARG_ETKINLIK anahtarı ile Etkinlik nesnesini arguments'tan alıyoruz
        val etkinlik = arguments?.getSerializable(ARG_ETKINLIK) as? Etkinlik

        etkinlik?.let {
            // Etkinlik detaylarını arayüze atıyoruz
            binding.baslikTextView.text = it.baslık
            binding.tarihTextView.text = it.tarih
            binding.yerTextView.text = it.yer
            binding.kategoriTextView.text = it.kategori
            binding.aciklamaTextWiew.text = it.aciklama // Burada açıklama ekleniyor
        }

        // Favorilere Ekle Butonu Tıklama Olayı
        binding.detayFavEkle.setOnClickListener {
            etkinlik?.let { etkinlikDetayi ->
                favorilereEkle(etkinlikDetayi)
            }
        }

        // Katılma Butonu Tıklama Olayı
        binding.detayKatil.setOnClickListener {
            etkinlik?.let { etkinlikDetayi ->
                etkinlikKatil(etkinlikDetayi)
            }
        }

        // Yorum Ekleme Butonu Tıklama Olayı
        binding.yorumEkleButton.setOnClickListener {
            val yorum = binding.yorumEditText.text.toString()
            val puan = binding.puanRatingBar.rating.toInt()

            if (yorum.isNotEmpty() && puan > 0) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (etkinlik != null && userId != null) {
                    yorumVePuanEkle(etkinlik.id, userId, yorum, puan)
                } else {
                    Toast.makeText(requireContext(), "Lütfen giriş yapın ve etkinliği seçin.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Lütfen yorumunuzu yazın ve puan verin.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.yorumlariGoruntuleButton.setOnClickListener {
            val etkinlikId = etkinlik?.id
            if (etkinlikId != null) {
                // YorumlarFragment'ına etkinlik ID'sini gönder
                val yorumlarFragment = YorumlarFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_ETKINLIK_ID, etkinlikId) // ARG_ETKINLIK_ID'yi kullanıyoruz
                    }
                }
                // Fragment değişimini yap
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, yorumlarFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), "Etkinlik bilgisi alınamadı.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun favorilereEkle(etkinlik: Etkinlik) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("Favoriler")
            database.child(userId).child(etkinlik.id).setValue(etkinlik)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Etkinlik favorilere eklendi!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Lütfen giriş yapın!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun etkinlikKatil(etkinlik: Etkinlik) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("KatidiğimEtkinlikler")
            database.child(userId).child(etkinlik.id).setValue(etkinlik)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Etkinliğe katıldın!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Lütfen giriş yapın!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun yorumVePuanEkle(etkinlikId: String, kullaniciId: String, yorum: String, puan: Int) {
        val database = FirebaseDatabase.getInstance().getReference("YorumlarVePuanlar")
        val yorumVerisi = mapOf(
            "yorum" to yorum,
            "puan" to puan
        )
        database.child(etkinlikId).child("yorumlar").child(kullaniciId).setValue(yorumVerisi)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Yorum ve puan başarıyla eklendi!", Toast.LENGTH_SHORT).show()
                ortalamaPuaniGuncelle(etkinlikId)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Ortalama Puanı Güncelleme
    private fun ortalamaPuaniGuncelle(etkinlikId: String) {
        val database = FirebaseDatabase.getInstance().getReference("YorumlarVePuanlar")
        database.child(etkinlikId).child("yorumlar").get().addOnSuccessListener { snapshot ->
            var toplamPuan = 0
            var yorumSayisi = 0
            snapshot.children.forEach { child ->
                val puan = child.child("puan").getValue(Int::class.java) ?: 0
                toplamPuan += puan
                yorumSayisi++
            }
            if (yorumSayisi > 0) {
                val ortalamaPuan = toplamPuan.toDouble() / yorumSayisi
                database.child(etkinlikId).child("ortalamaPuan").setValue(ortalamaPuan)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
