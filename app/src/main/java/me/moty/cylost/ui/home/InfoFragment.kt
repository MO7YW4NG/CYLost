package me.moty.cylost.ui.home

import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import me.moty.cylost.R
import me.moty.cylost.Record
import me.moty.cylost.adapters.ViewPageAdapter
import me.moty.cylost.databinding.FragmentInfoBinding

class InfoFragment(private val record: Record) : DialogFragment(), OnMapReadyCallback {
    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: GoogleMap
    override fun onStart() {
        val params = dialog!!.window!!.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val img = binding.viewPagerMain
        val type = binding.type
        val content = binding.content
        val loc = binding.location
        val date = binding.date
        val mapFragment = binding.mapFragment

        if (record.pin == null)
            mapFragment.visibility = View.GONE
        else {
            val supportMapFragment = SupportMapFragment.newInstance()
            supportMapFragment.getMapAsync(this)
            this.childFragmentManager
                .beginTransaction()
                .add(R.id.mapFragment, supportMapFragment)
                .commit()
        }

        date.text = "日期：" + record.date
        type.text = "類型：" + record.type.display
        content.text = record.content
        loc.text = "位置：" + record.loc

        val adapter =
            ViewPageAdapter(requireContext(), arrayOf(record.image1, record.image2, record.image3))
        img.adapter = adapter

        return root
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.addMarker(
            MarkerOptions()
                .position(record.pin!!)
        )
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(record.pin!!, 17f))
    }

}