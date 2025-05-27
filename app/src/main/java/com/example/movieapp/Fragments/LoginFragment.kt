package com.example.movieapp.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.movieapp.Activities.MovieActivity
import com.example.movieapp.R
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.LoginViewModel
import com.example.movieapp.databinding.FragmentLoginBinding
import com.github.razir.progressbutton.hideProgress
import com.github.razir.progressbutton.showProgress
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment:Fragment(R.layout.fragment_login){
    lateinit var binding: FragmentLoginBinding
    val viewModel by viewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvDangKy.setOnClickListener{
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.loginFragment, true) // Xóa LoginFragment khỏi stack
                .build()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment,null,navOptions)
        }
        binding.apply {
            btnLogin.setOnClickListener{
                if(edtEmail.text.trim().toString().isEmpty()){
                    edtEmail.requestFocus()
                    edtEmail.error = "Không được để trống"
                }else if(edtPass.text.trim().toString().isEmpty()){
                    edtPass.requestFocus()
                    edtPass.error ="Không được để trống"
                }
                else{

                viewModel.loginWithEmailAndPassword(edtEmail.text.toString(),edtPass.text.toString())
                }
            }
        }
        lifecycleScope.launch {
            viewModel.login.collect{
                when(it){
                    is Resource.Loading ->{
                        binding.btnLogin.showProgress{
                            progressColor = ContextCompat.getColor(requireContext(),R.color.white)
                        }
                    }
                    is Resource.Success ->{
                        binding.btnLogin.hideProgress("LOGIN")
                        Intent(requireActivity(),MovieActivity::class.java).also {

                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            startActivity(it)
                            requireActivity().finish()
                        }
                    }
                    is Resource.Error ->{
                        binding.btnLogin.hideProgress("LOGIN")
                        Snackbar.make(requireView(),it.message.toString(),Snackbar.LENGTH_SHORT).show()
                    }
                    else ->Unit
                }
            }
        }
        binding.apply {
            tvQuenMK.setOnClickListener{
                showDialogResetPassword()
            }
        }
        lifecycleScope.launch {
            viewModel.resetPassword.collect{
                when(it){
                    is Resource.Loading -> Unit
                    is Resource.Success ->{
                        Snackbar.make(requireView(),"Vui lòng kiểm tra email để đặt lại mật khẩu",Snackbar.LENGTH_SHORT).show()
                    }
                    is Resource.Error->{
                        Snackbar.make(requireView(),it.message.toString(),Snackbar.LENGTH_SHORT).show()
                    }
                    else-> Unit
                }
            }
        }
    }
    private fun showDialogResetPassword(){
        val dialog =BottomSheetDialog(requireContext())
        val viewDialog = layoutInflater.inflate(R.layout.rerest_password_dialog,null)
        dialog.setContentView(viewDialog)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
        val btnSend = viewDialog.findViewById<Button>(R.id.buttonSendResetPassword)
        val email = viewDialog.findViewById<EditText>(R.id.edEmilResetPassword)
        val btnCancel = viewDialog.findViewById<Button>(R.id.buttonCancelResetPassword)
        btnSend.setOnClickListener{
            viewModel.resetPassword(email.text.trim().toString())
            dialog.dismiss()
        }
        btnCancel.setOnClickListener{
            dialog.dismiss()
        }

    }
}
