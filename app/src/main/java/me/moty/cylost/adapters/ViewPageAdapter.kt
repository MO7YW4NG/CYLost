package me.moty.cylost.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import me.moty.cylost.R
import java.util.Objects

class ViewPageAdapter(
    val context: Context,
    private val images: Array<Bitmap?>,
    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
) : PagerAdapter() {

    override fun getCount(): Int {
        return if (images.filterNotNull().isEmpty()) 1 else images.filterNotNull().size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == (`object` as LinearLayout)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = inflater.inflate(R.layout.viewpager_item, container, false)
        val imgView = itemView.findViewById<ImageView>(R.id.imageViewMain)
        if (images.filterNotNull().isEmpty())
            imgView.setImageResource(R.drawable.baseline_image_not_supported_24)
        else
            imgView.setImageBitmap(images[position])
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }

}