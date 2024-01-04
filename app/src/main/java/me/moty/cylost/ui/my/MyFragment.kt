package me.moty.cylost.ui.my

import android.database.sqlite.SQLiteException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.moty.cylost.MainActivity
import me.moty.cylost.R
import me.moty.cylost.Record
import me.moty.cylost.RecordType
import me.moty.cylost.adapters.ImgBtnClickListener
import me.moty.cylost.adapters.MainAdapter
import me.moty.cylost.adapters.OnRecyclerViewClickListener
import me.moty.cylost.data.LoginRepository
import me.moty.cylost.databinding.FragmentMyBinding
import me.moty.cylost.ui.home.HomeFragment
import me.moty.cylost.ui.home.InfoFragment
import me.moty.cylost.ui.login.LoginViewModel
import me.moty.cylost.ui.login.LoginViewModelFactory

class MyFragment : Fragment() {

    private var _binding: FragmentMyBinding? = null
    private lateinit var loginViewModel: LoginViewModel
    private val binding get() = _binding!!
    private var adapter: MainAdapter = MainAdapter(MainActivity.appContext)
    private lateinit var loginRepository: LoginRepository


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val navController = requireActivity().findNavController(R.id.fragment)
        loginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory()
        )[LoginViewModel::class.java]

        if (!loginViewModel.isLogged()) {
            navController.navigate(R.id.action_navigation_my_to_navigation_login, null, navOptions {
                popUpTo(R.id.navigation_my) {
                    this.inclusive = true
                    this.saveState = true
                }
                this.launchSingleTop = true
                this.restoreState = true
            })
            return root
        }
        loginRepository = loginViewModel.getRepo()
        val recycler = binding.recyclerView
        val noRecords = binding.noRecords
        val welcome = binding.welcome
        val logout = binding.logout
        val manager = LinearLayoutManager(requireContext())

        adapter.imgListener = object : ImgBtnClickListener {
            override fun onItemClick(view: View, pos: Int, item: Record) {
                val menu = PopupMenu(MainActivity.appContext, view)
                menu.menuInflater.inflate(R.menu.info, menu.menu)
                menu.setOnMenuItemClickListener {
                    try {
                        if (!loginViewModel.isLogged())
                            return@setOnMenuItemClickListener false
                        when (it.itemId) {
                            R.id.delete -> {
                                val isOwner =
                                    loginViewModel.getRepo().user!!.stuId == item.owner.toString()
                                if (isOwner) {
                                    MainActivity.appContext.getDatebase()
                                        .execSQL(
                                            "DELETE FROM Record WHERE id = ${item.id};"
                                        )
                                    HomeFragment.updated = true
                                    updateAdapter()
                                }
                                true
                            }

                            else -> true
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                menu.show()
            }
        }
        welcome.text = welcome.text.toString() + loginRepository.user!!.stuId
        manager.orientation = LinearLayoutManager.VERTICAL
        recycler.layoutManager = manager
        updateAdapter()
        recycler.adapter = adapter

        adapter.itemListener = object : OnRecyclerViewClickListener {
            override fun onItemClick(record: Record) {
                val info = InfoFragment(record)
                info.show(requireActivity().supportFragmentManager, "InfoDialogFragment")
            }
        }

        logout.setOnClickListener {
            loginViewModel.logout()
            navController.popBackStack()
        }

        if (adapter.currentList.size == 0)
            noRecords.visibility = View.VISIBLE

        return root
    }

    private fun updateAdapter() {
        val list = mutableListOf<Record>()
        try {
            MainActivity.appContext.getDatebase()
                .rawQuery(
                    "SELECT * FROM Record WHERE owner = '${loginRepository.user?.stuId}' ORDER BY id DESC",
                    null
                ).use { q ->
                    q.moveToFirst()
                    for (i in 0 until q.count) {
                        val id = q.getInt(0).toString()
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
                        q.moveToNext()
                    }
                }
        } catch (e: SQLiteException) {
            e.printStackTrace()
        }
        adapter.submitList(list)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}