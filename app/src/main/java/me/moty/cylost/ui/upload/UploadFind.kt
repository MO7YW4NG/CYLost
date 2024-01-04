package me.moty.cylost.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import me.moty.cylost.R
import me.moty.cylost.databinding.FragmentUploadBinding


class UploadFind : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val upload = requireActivity().findViewById<Button>(R.id.upload)
        val cancel = requireActivity().findViewById<Button>(R.id.cancel)

        upload.setOnClickListener{
//            startActivity(intent)
        }
        cancel.setOnClickListener{

        }
        return root
    }
}

