package com.example.movieapp.Fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movieapp.Models.User

import com.example.movieapp.R
import com.example.movieapp.Untils.RegisterValidation
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.RegisterViewModel
import com.example.movieapp.databinding.FragmentLoginBinding
import com.example.movieapp.databinding.FragmentRegisterBinding
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@AndroidEntryPoint
class RegisterFragment : Fragment(R.layout.fragment_register){
    lateinit var binding: FragmentRegisterBinding
    private val viewModel by viewModels<RegisterViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDangNhapRegister.setOnClickListener{
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.registerFragment, true) // Xóa LoginFragment khỏi stack
                .build()
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment,null,navOptions)
        }
        binding.apply {
            btnRegister.setOnClickListener{
                if (edtPass.text.toString()!=edtXacNhanPass.text.toString()){
                    Snackbar.make(requireView(),"2 mật khẩu không giống nhau",Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }else
                {
                    var user = User(
                        edtUsername.text.trim().toString(),
                        edtEmailRegister.text.trim().toString()
                    )
                    val pass = edtPass.text.trim().toString()
                    viewModel.createAccoutWihtEmailAndPassowrd(user,pass)
                }

            }
        }
        lifecycleScope.launch {
            viewModel.register.collect{
                when(it){
                    is Resource.Loading ->{
                        binding.btnRegister.showProgress{
                            progressColor = ContextCompat.getColor(requireContext(),R.color.white)
                        }
                    }
                    is Resource.Success -> {
                        binding.btnRegister.hideProgress("Đăng ký thành công")
                    }
                    is Resource.Error ->{
                        Snackbar.make(requireView(),it.message.toString(),Snackbar.LENGTH_SHORT).show()
                    }
                    else->Unit
                }
            }
        }
        lifecycleScope.launch {
            viewModel.validation.collect{
                if(it.email is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edtEmailRegister.apply {
                            requestFocus()
                            error = it.email.message
                        }
                    }
                }
                if(it.password is RegisterValidation.Failed){
                    withContext(Dispatchers.Main){
                        binding.edtPass.apply {
                            requestFocus()
                            error = it.password.message
                        }
                    }
                }
            }
        }

    }
}
