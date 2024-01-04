package me.moty.cylost.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide
import me.moty.cylost.R

class UploadPhotoAdapter(context: Context, private val layout: Int, data: ArrayList<Bitmap>) :
    ArrayAdapter<Bitmap>(context, layout, data) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = View.inflate(parent.context, layout, null)
        val item = getItem(position) ?: return view
        val imgPhoto = view.findViewById<ImageView>(R.id.img_photo)
        Glide.with(view).load(item).centerCrop().into(imgPhoto)
        return view
    }
}