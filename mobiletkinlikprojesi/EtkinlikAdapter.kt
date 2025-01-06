package com.kubradurak.mobiletkinlikprojesi

import android.graphics.Color
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat



class EtkinlikAdapter(
    //private val etkinlikListesi: List<Etkinlik>,
    private var etkinlikListesi: MutableList<Etkinlik>,

    private val itemClickListener: (Etkinlik) -> Unit // Tıklama işlevi için parametre ekledik
) : RecyclerView.Adapter<EtkinlikAdapter.EtkinlikViewHolder>() {

    class EtkinlikViewHolder(private val container: LinearLayout) : RecyclerView.ViewHolder(container) {
        private val baslıkTextView = TextView(container.context).apply {
            textSize = 22f
            setTextColor(Color.WHITE)
        }
        /*      private val tarihTextView = TextView(container.context).apply {
                  textSize = 14f
                  setTextColor(Color.DKGRAY)
              }
              private val konumTextView = TextView(container.context).apply {
                  textSize = 14f
                  setTextColor(Color.DKGRAY)
              }
              private val kategoriTextView = TextView(container.context).apply {
                  textSize = 14f
                  setTextColor(Color.DKGRAY)
              }*/

        init {
            container.orientation = LinearLayout.VERTICAL
            container.addView(baslıkTextView)
            /*            container.addView(tarihTextView)
                        container.addView(konumTextView)
                        container.addView(kategoriTextView)*/
        }

        fun bind(etkinlik: Etkinlik, itemClickListener: (Etkinlik) -> Unit){
            baslıkTextView.text = "Etkinlik: ${etkinlik.baslık}"
            /*            tarihTextView.text = "Tarih: ${etkinlik.tarih}"
                        konumTextView.text = "Yer: ${etkinlik.konum}"
                        kategoriTextView.text = "Kategori: ${etkinlik.kategori}"*/

            // Tıklama olayını tanımladık
            itemView.setOnClickListener {
                itemClickListener(etkinlik) // Tıklanan etkinliği dışarıya döndürüyoruz
            }
        }
    }
    fun updateList(newList: List<Etkinlik>) {
        etkinlikListesi.clear()
        etkinlikListesi.addAll(newList)
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtkinlikViewHolder {
        val container = LinearLayout(parent.context)
        return EtkinlikViewHolder(container)
    }

    override fun onBindViewHolder(holder: EtkinlikViewHolder, position: Int) {
        val etkinlik = etkinlikListesi[position]
        holder.bind(etkinlik,itemClickListener)
    }

    override fun getItemCount(): Int = etkinlikListesi.size
}