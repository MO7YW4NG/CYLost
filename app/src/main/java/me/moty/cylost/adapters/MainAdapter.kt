package me.moty.cylost.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import me.moty.cylost.MainActivity
import me.moty.cylost.R
import me.moty.cylost.Record
import me.moty.cylost.ui.home.HomeFragment
import me.moty.cylost.ui.home.HomeViewModel
import me.moty.cylost.ui.login.LoginViewModel
import me.moty.cylost.ui.login.LoginViewModelFactory
import java.lang.Exception

class MainAdapter(val context: Context) :
    ListAdapter<Record, RecyclerView.ViewHolder>(DiffCallback()) {
    var imgListener: ImgBtnClickListener? = null
    var itemListener: OnRecyclerViewClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ItemViewHolder.from(parent, imgListener, itemListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }


}

interface ImgBtnClickListener {
    fun onItemClick(view: View, pos: Int, item: Record)
}

class ItemViewHolder private constructor(
    private val view: View, private val imgListener: ImgBtnClickListener?,
    private val itemListener: OnRecyclerViewClickListener?
) : RecyclerView.ViewHolder(view) {

    @SuppressLint("SetTextI18n")
    fun bind(item: Record) {
        val img = view.findViewById<ImageView>(R.id.img)
        val date = view.findViewById<TextView>(R.id.date)
        val loc = view.findViewById<TextView>(R.id.location)
        val content = view.findViewById<TextView>(R.id.content)
        val imageButton = itemView.findViewById<ImageButton>(R.id.menu)
        if (item.image1 != null)
            Glide.with(view).load(item.image1)
                .centerCrop().into(img)
        else
            Glide.with(view).load(R.drawable.baseline_image_not_supported_24).centerCrop().into(img)
        if (itemListener != null) {
            itemView.setOnClickListener {
                itemListener.onItemClick(item)
            }
        }
        if (imgListener != null) {
            imageButton.setOnClickListener {
                if (imgListener == null)
                    return@setOnClickListener
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION)
                    imgListener.onItemClick(imageButton, position, item)
            }
        }
        date.text = item.date
        loc.text = "位置: " + item.loc
        content.text = item.content
    }

    companion object {
        fun from(
            parent: ViewGroup,
            imgListener: ImgBtnClickListener?,
            itemListener: OnRecyclerViewClickListener?
        ): RecyclerView.ViewHolder {
            val binding = LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_main, parent, false)
            return ItemViewHolder(binding, imgListener, itemListener)
        }
    }
}

class DiffCallback : DiffUtil.ItemCallback<Record>() {
    override fun areItemsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Record, newItem: Record): Boolean {
        return oldItem == newItem
    }
}

interface OnRecyclerViewClickListener {
    fun onItemClick(record: Record)
}