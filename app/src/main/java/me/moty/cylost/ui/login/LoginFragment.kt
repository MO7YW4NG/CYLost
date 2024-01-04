package me.moty.cylost.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import me.moty.cylost.MainActivity
import me.moty.cylost.R
import me.moty.cylost.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val loginViewModel: LoginViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val stu = root.findViewById<EditText>(R.id.ID)
        val pwd = root.findViewById<EditText>(R.id.password)
        val btn = root.findViewById<Button>(R.id.loginBtn)
        val pro = root.findViewById<ProgressBar>(R.id.progressBar)

        val shared = MainActivity.sharedPreferences
        val check = root.findViewById<CheckBox>(R.id.checkBox)
        if (shared.contains("remember")) {
            check.isChecked = true
            stu.setText(shared.getString("stuid", ""))
            pwd.setText(shared.getString("pwd", ""))
        }
        pro.visibility = View.GONE

        loginViewModel.getLoginFromState()
            .observe(this.requireActivity(), Observer<LoginFormState>() {
                if (!isAdded)
                    return@Observer
                btn.isEnabled = it.isDataValid
                if (it.usernameError != null)
                    stu.error = getString(it.usernameError)
                if (it.passwordError != null)
                    pwd.error = getString(it.passwordError)
            })
        val navController = requireActivity().findNavController(R.id.fragment)
        loginViewModel.getLoginResult().observe(this.requireActivity(), Observer<LoginResult>() {
            if (!isAdded)
                return@Observer
            pro.visibility = View.GONE
            if (it.error != null)
                showLoginFailed()
            if (it.success != null) {
                navController.navigate(R.id.navigation_my)
            }
        })
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                loginViewModel.loginDataChanged(stu.text.toString(), pwd.text.toString())
            }

        }
        pwd.addTextChangedListener(textWatcher)
        pwd.setOnEditorActionListener { _, actionId, _ ->
            if (stu.text.isEmpty() || pwd.text.isEmpty())
                false
            if (actionId == EditorInfo.IME_ACTION_DONE)
                loginViewModel.login(stu.text.toString(), pwd.text.toString(), check.isChecked)
            false
        }
        stu.addTextChangedListener(textWatcher)
        btn.setOnClickListener {
            if (stu.text.isEmpty() || pwd.text.isEmpty())
                return@setOnClickListener
            pro.visibility = View.VISIBLE
            loginViewModel.login(stu.text.toString(), pwd.text.toString(), check.isChecked)
        }
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoginFailed() {
        Toast.makeText(this.requireContext(), R.string.login_failed, Toast.LENGTH_SHORT).show()
    }
}