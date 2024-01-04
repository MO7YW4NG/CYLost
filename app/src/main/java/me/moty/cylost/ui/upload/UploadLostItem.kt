package me.moty.cylost.ui.upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import me.moty.cylost.MainActivity
import me.moty.cylost.R
import me.moty.cylost.RecordType
import me.moty.cylost.adapters.UploadPhotoAdapter
import me.moty.cylost.databinding.FragmentUploadLostItemBinding
import me.moty.cylost.ui.home.HomeFragment
import me.moty.cylost.ui.home.HomeViewModel
import me.moty.cylost.ui.login.LoginViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.stream.Collectors


class UploadLostItem : Fragment(), OnMapReadyCallback, OnMyLocationClickListener,
    OnMyLocationButtonClickListener, GoogleMap.OnMarkerDragListener {
    private var items: ArrayList<Bitmap> = ArrayList()
    private lateinit var adapter: UploadPhotoAdapter
    private var pin: LatLng? = null
    private var _binding: FragmentUploadLostItemBinding? = null
    private lateinit var map: GoogleMap

    private val loginViewModel: LoginViewModel by activityViewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    fun retreieveId(): String {
        val new = SimpleDateFormat(
            "yyyyMMdd",
            Locale.TAIWAN
        ).format(Calendar.getInstance(Locale.TAIWAN).time)
        var count = 1
        MainActivity.appContext.getDatebase()
            .rawQuery("SELECT id FROM Record WHERE id LIKE '${new}%' ORDER BY id", null).let {
                if (it.count == 0) {
                    return@let
                }
                it.moveToLast()
                val id = it.getString(0)
                count = id.substring(8, 10).toInt()
                if (count < 50)
                    count = 50;
                else
                    count++
            }
        return new.toString() + String.format("%02d", count)
    }

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadLostItemBinding.inflate(inflater, container, false)
        val root: View = binding.root
        if (savedInstanceState != null) {
            for (i in 1 until 4) {
                if (savedInstanceState.containsKey("image$i")) {
                    val bytes = savedInstanceState.getByteArray("image$i")!!
                    items.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
                }
            }
            if (savedInstanceState.containsKey("pin"))
                pin = savedInstanceState.getParcelable("pin", LatLng::class.java)
        }
        mapSetup(root)
        setListener(root)
        return root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        var index = 1
        items.forEach {
            val outputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outState.putByteArray("image$index", outputStream.toByteArray())
            index++
        }
        outState.putParcelable("pin", pin)
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_LONG).show()
    }

    private fun mapSetup(root: View) {
        val map = binding.map
        val hint = binding.hint
        map.visibility = View.GONE
        hint.visibility = View.GONE
        if (enableMyLocation()) {
            map.visibility = View.VISIBLE
            hint.visibility = View.VISIBLE
            val mapFragment = SupportMapFragment.newInstance()
            mapFragment.getMapAsync(this)
            requireActivity().supportFragmentManager
                .beginTransaction()
                .add(R.id.map, mapFragment)
                .commit()
        }
    }

    private fun setListener(view: View) {
        val content = binding.editContent
        val loc = binding.editLocation
        val date = binding.editDate
        val type = binding.type
        var selectedType: String = ""
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN)
        date.setText(sdf.format(calendar.time))

        val navController = requireActivity().findNavController(R.id.fragment)

        val upload = binding.upload
        val cancel = binding.cancel
        val gridView = binding.photoGrid
        val size = binding.sizeHint

        val bitmap = BitmapFactory.decodeResource(resources, android.R.drawable.ic_menu_camera)
        if (items.size < 1)
            items.add(bitmap)
        adapter =
            UploadPhotoAdapter(this.requireContext(), R.layout.adapter_upload_photo, items)
        gridView.adapter = adapter

        type.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position != 0)
                    selectedType = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        var index = 0
        val takePhoto =
            registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
                it?.let {
                    size.text = "(${items.size}/3)"
                    if (items.size == 3)
                        items.removeAt(index)
                    items.add(index, it)
                    adapter.notifyDataSetChanged()
                }
            }

        val getPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let {
                size.text = "(${items.size}/3)"
                if (items.size == 3)
                    items.removeAt(index)
                items.add(index, getBitmapFromUri(it))
                adapter.notifyDataSetChanged()
            }
        }
        gridView.setOnItemClickListener { _, view, position, _ ->
            val menu = PopupMenu(requireContext(), view)
            menu.menuInflater.inflate(R.menu.retrieve, menu.menu)
            menu.setOnMenuItemClickListener {
                index = position
                when (it.itemId) {
                    R.id.option1 ->
                        takePhoto.launch(null)

                    R.id.option2 ->
                        getPhoto.launch("image/*")
                }
                true
            }
            menu.show()
        }

        val datePicker = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            date.setText(sdf.format(calendar.time))
        }

        date.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) return@setOnFocusChangeListener
            v.clearFocus()
            DatePickerDialog(
                requireContext(),
                datePicker,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        upload.setOnClickListener {
            if (content.length() < 1 || loc.length() < 1 || selectedType.isEmpty() || date.length() < 1) {
                showToast("欄位請勿空白")
                return@setOnClickListener
            }
            items.removeAll { it == bitmap }
            val id = retreieveId()
            val r =
                me.moty.cylost.Record(
                    id,
                    loginViewModel.getRepo().user?.stuId!!.toInt(),
                    content.text.toString().trim(),
                    loc.text.toString().trim(),
                    date.text.toString().trim(),
                    RecordType.values().toMutableList().stream()
                        .filter { s -> s.display.equals(selectedType, true) }.findFirst()
                        .orElse(null),
                    pin,
                    if (items.size >= 1) items[0] else null,
                    if (items.size >= 2) items[1] else null,
                    if (items.size >= 3) items[2] else null
                )
            homeViewModel.insert(r)
            HomeFragment.updated = true
            showToast("上傳成功!")
            navController.popBackStack()
        }

        val array = RecordType.values().toMutableList().stream().map { type -> type.display }
            .collect(Collectors.toList())
        array.add(0, "選擇物品類型")
        val spinnerAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, array)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        type.adapter = spinnerAdapter

        cancel.setOnClickListener {
            navController.popBackStack()
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        val resolver: ContentResolver = requireActivity().contentResolver
        val inputStream = resolver.openInputStream(uri)
        return BitmapFactory.decodeStream(inputStream)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.isMyLocationEnabled = true
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity());
        val locationRequest =
            LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10000)
                .build()
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation ?: return
                val loc: LatLng = pin
                    ?: LatLng(
                        locationResult.lastLocation!!.latitude,
                        locationResult.lastLocation!!.longitude
                    )
                map.addMarker(
                    MarkerOptions()
                        .position(loc)
                        .draggable(true)
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 17f))
                fusedLocationProviderClient.flushLocations()
                fusedLocationProviderClient.removeLocationUpdates(this)
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        map.setOnMarkerDragListener(this)
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun enableMyLocation(): Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    }

    override fun onMyLocationClick(location: Location) {
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMarkerDrag(marker: Marker) {}

    override fun onMarkerDragEnd(marker: Marker) {
        pin = LatLng(marker.position.latitude, marker.position.longitude)
    }

    override fun onMarkerDragStart(marker: Marker) {}

}