package me.moty.cylost.ui.home

import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.moty.cylost.MainActivity
import me.moty.cylost.Record
import me.moty.cylost.RecordType
import me.moty.cylost.adapters.MainAdapter
import me.moty.cylost.adapters.OnRecyclerViewClickListener
import me.moty.cylost.databinding.FragmentHomeBinding
import java.time.LocalDate
import java.util.EnumMap


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by activityViewModels()

    companion object {
        var updated: Boolean = true
    }

    private var adapter: MainAdapter = MainAdapter(MainActivity.appContext)
    var page = 1
    private var dataSize: Int = 0

    private var filterFragment: FilterFragment = FilterFragment(this)

    var condition: EnumMap<FilterTag, String> =
        EnumMap(FilterTag::class.java)

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val root: View = binding.root
        val recycler = binding.recyclerView
        val editText = binding.search
        val btn = binding.filter
        val nested = binding.nested
        val loading = binding.loading

        val manager = LinearLayoutManager(requireContext())

        manager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = manager

        this.lifecycleScope.launch {
            dataSize =
                retrieveDataSize()
            val shared = MainActivity.sharedPreferences
            if (dataSize == 0 || (shared.contains("lastFetch") && LocalDate.now().dayOfMonth
                        != shared.getInt("lastFetch", 0))) {
                val editor: SharedPreferences.Editor = shared.edit()
                editor.putInt("lastFetch", LocalDate.now().dayOfMonth)
                editor.apply()
                homeViewModel.fetchData {
                    dataSize += it
                    lifecycleScope.launch {
                        updateAdapter {}
                    }
                }
            }
        }
        recycler.setItemViewCacheSize(25)
        recycler.adapter = adapter
        recycler.isNestedScrollingEnabled = false

        adapter.itemListener = object : OnRecyclerViewClickListener {
            override fun onItemClick(record: Record) {
                val info = InfoFragment(record)
                info.show(requireActivity().supportFragmentManager, "InfoDialogFragment")
            }
        }
        nested.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (page == (dataSize / 25))
                return@setOnScrollChangeListener
            if (scrollY == nested.getChildAt(0).measuredHeight - v.measuredHeight) {
                if (loading.visibility != View.VISIBLE) {
                    loading.visibility = View.VISIBLE
                    page++
                    Thread.sleep(300)
                    this.lifecycleScope.launch {
                        updateAdapter {
                            loading.visibility = View.GONE
                        }
                    }
                }
            }
        }

        if (updated) {
            this.lifecycleScope.launch {
                page = 1
                updateAdapter(null)
                updated = false
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            this.lifecycleScope.launch {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (editText.text.isNotEmpty())
                        condition[FilterTag.KEYWORD] = "content LIKE '${editText.text}%'"
                    else if (condition.containsKey(FilterTag.KEYWORD))
                        condition.remove(FilterTag.KEYWORD)
                    updateAdapter(
                        null
                    )
                }
            }
            false
        }

        btn.setOnClickListener {
            if (filterFragment.isAdded)
                return@setOnClickListener
            filterFragment.show(requireActivity().supportFragmentManager, "FilterDialogFragment")
        }
        return root
    }

    fun query(): String {
        return (
                "SELECT * FROM Record ${
                    if (condition.size > 0)
                        "WHERE " +
                                condition.values.joinToString(
                                    " AND "
                                ) else ""
                } ORDER BY id DESC LIMIT ${page * 25}"
                )
    }

    suspend fun retrieveDataSize(): Int = withContext(Dispatchers.IO) {
        MainActivity.appContext.getDatebase().rawQuery("SELECT COUNT(*) FROM Record", null)
            .use {
                it.moveToFirst()
                it.getInt(0)
            }
    }

    fun updateAdapterOutside() {
        lifecycleScope.launch {
            updateAdapter {}
        }
    }

    private suspend fun updateAdapter(callback: (() -> Unit)?) = withContext(Dispatchers.IO) {
        val list = mutableListOf<Record>()
        try {
            MainActivity.appContext.getDatebase()
                .rawQuery(query(), null)
                .use { q ->
                    try {
                        while (q.moveToNext()) {
                            val id = q.getInt(0).toString()
                            val img = decodeImagesAsync(q)
                            val pinString = q.getString(6)
                            val pin =
                                if (pinString == null) null else LatLng(
                                    pinString.split(";")[0].toDouble(),
                                    pinString.split(";")[1].toDouble()
                                )
                            list.add(
                                Record(
                                    id,
                                    q.getInt(1),
                                    q.getString(2),
                                    q.getString(3),
                                    q.getString(4),
                                    RecordType.valueOf(q.getString(5)), pin, img[0], img[1], img[2]
                                )
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        withContext(Dispatchers.Main) {
            adapter.submitList(list, callback)
        }
    }

    private suspend fun decodeImagesAsync(q: Cursor): Array<Bitmap?> =
        withContext(Dispatchers.Default) {
            val img = arrayOfNulls<Bitmap>(3)
            for (j in 7 until 10) {
                val imgBytes = q.getBlob(j)
                if (imgBytes != null)
                    img[j - 7] = BitmapFactory.decodeByteArray(
                        imgBytes,
                        0,
                        imgBytes.size
                    )
            }
            img
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class FilterTag {
    KEYWORD, TYPE, DATE
}