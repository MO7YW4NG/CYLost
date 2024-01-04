package me.moty.cylost.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import me.moty.cylost.R
import me.moty.cylost.databinding.FragmentUploadBinding
import me.moty.cylost.ui.login.LoginViewModel
import me.moty.cylost.ui.login.LoginViewModelFactory

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val findItem = root.findViewById<Button>(R.id.findItem)
        val lostItem = root.findViewById<Button>(R.id.lostItem)

        val navController = requireActivity().findNavController(R.id.fragment)
        val loginViewModel: LoginViewModel = ViewModelProvider(
            requireActivity(),
            LoginViewModelFactory()
        )[LoginViewModel::class.java]
//        findItem.setOnClickListener {
//            if (!loginViewModel.isLogged())
//                navController.navigate(R.id.navigation_my, null, navOptions {
//                    this.popUpTo(R.id.navigation_upload) {
//                        this.inclusive = true
//                    }
//                    this.launchSingleTop = true
//                    this.restoreState = true
//                })
//            else
//                navController.navigate(R.id.action_navigation_upload_to_navigation_upload_find)
//        }
//        lostItem.setOnClickListener {
        if (!loginViewModel.isLogged()) {
            Toast.makeText(requireContext(), "請先登入!", Toast.LENGTH_SHORT).show()
            navController.navigate(R.id.navigation_my, null, navOptions {
                this.popUpTo(R.id.navigation_upload) {
                    this.inclusive = true
                    this.saveState = true
                }
                this.launchSingleTop = true
                this.restoreState = true
            })
        } else
            navController.navigate(
                R.id.action_navigation_upload_to_navigation_upload_lost,
                null,
                navOptions {
                    this.popUpTo(R.id.navigation_upload) {
                        this.inclusive = true
                        this.saveState = true
                    }
                    this.launchSingleTop = true
                    this.restoreState = true
                })
//        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
