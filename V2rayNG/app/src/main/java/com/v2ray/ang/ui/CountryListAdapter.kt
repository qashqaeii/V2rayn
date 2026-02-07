package com.v2ray.ang.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.v2ray.ang.R
import com.v2ray.ang.databinding.ItemCountryBinding
import com.v2ray.ang.dto.ServersCache
import com.v2ray.ang.handler.MmkvManager
import java.util.Locale

/**
 * آداپتر ساده برای نمایش لیست کشورها/سرورها در Bottom Sheet انتخاب مکان.
 */
class CountryListAdapter(
    private var items: List<ServersCache>,
    private val onCountrySelected: (guid: String) -> Unit
) : RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {

    fun updateList(newItems: List<ServersCache>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCountryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val aff = MmkvManager.decodeServerAffiliationInfo(item.guid)
        val flagEmoji = aff?.flag?.toFlagEmoji().orEmpty()
        holder.binding.tvFlag.text = if (flagEmoji.isNotEmpty()) flagEmoji else "🌍"
        holder.binding.tvName.text = item.profile.remarks.ifEmpty { item.guid.take(8) }
        holder.binding.root.setOnClickListener {
            onCountrySelected(item.guid)
        }
    }

    override fun getItemCount() = items.size

    private fun String.toFlagEmoji(): String {
        val code = trim()
        if (code.isEmpty()) return ""
        if (code.codePoints().count() > 1 && code.any { Character.getType(it) == Character.OTHER_SYMBOL.toInt() }) {
            return code
        }
        val cc = code.uppercase(Locale.US)
        if (cc.length != 2 || !cc.all { it in 'A'..'Z' }) return ""
        val base = 0x1F1E6
        val first = base + (cc[0].code - 'A'.code)
        val second = base + (cc[1].code - 'A'.code)
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }

    class ViewHolder(val binding: ItemCountryBinding) : RecyclerView.ViewHolder(binding.root)
}
