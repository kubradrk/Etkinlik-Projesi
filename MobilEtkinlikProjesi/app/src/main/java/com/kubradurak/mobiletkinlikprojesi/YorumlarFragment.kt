package com.kubradurak.mobiletkinlikprojesi

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kubradurak.mobiletkinlikprojesi.databinding.FragmentYorumlarBinding
import com.kubradurak.mobiletkinlikprojesi.databinding.YorumItemBinding

class YorumlarFragment : Fragment() {

    companion object {
        private const val ARG_ETKINLIK_ID = "etkinlikId"
    }

    private lateinit var binding: FragmentYorumlarBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentYorumlarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerView ve adapter ayarı
        val yorumAdapter = YorumAdapter() // YorumAdapter'ı burada oluşturuyoruz
        binding.yorumlarRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.yorumlarRecyclerView.adapter = yorumAdapter

        // Yorumları al
        val etkinlikId = arguments?.getString(ARG_ETKINLIK_ID) ?: return
        yorumlariGoruntuleVeCek(etkinlikId, yorumAdapter)
    }

    private fun yorumlariGoruntuleVeCek(etkinlikId: String, yorumAdapter: YorumAdapter) {
        val database = FirebaseDatabase.getInstance().getReference("YorumlarVePuanlar")
        database.child(etkinlikId).child("yorumlar")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val yorumlarList = mutableListOf<Yorum>()
                    for (data in snapshot.children) {
                        val yorum = data.getValue(Yorum::class.java)
                        if (yorum != null) {
                            yorumlarList.add(yorum)
                        }
                    }
                    yorumAdapter.setYorumlar(yorumlarList)  // Adapter'a yeni yorumları ver
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Veri çekilirken hata oluştu: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // Adapter sınıfını ayrı bir sınıf olarak tanımlıyoruz
    class YorumAdapter : RecyclerView.Adapter<YorumAdapter.YorumViewHolder>() {
        private var yorumlarList: List<Yorum> = listOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YorumViewHolder {
            val binding = YorumItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return YorumViewHolder(binding)
        }

        override fun onBindViewHolder(holder: YorumViewHolder, position: Int) {
            val yorum = yorumlarList[position]
            holder.bind(yorum)
        }

        override fun getItemCount(): Int = yorumlarList.size

        fun setYorumlar(yorumlar: List<Yorum>) {
            yorumlarList = yorumlar
            notifyDataSetChanged()  // Listeyi güncelle
        }

        inner class YorumViewHolder(private val binding: YorumItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(yorum: Yorum) {
                binding.yorumTextView.text = yorum.yorum
                binding.puanTextView.text = "Puan: ${yorum.puan}"
            }
        }
    }
}
