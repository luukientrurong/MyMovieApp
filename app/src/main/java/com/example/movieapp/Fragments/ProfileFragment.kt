package com.example.movieapp.Fragments

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.movieapp.Activities.LoginActivity
import com.example.movieapp.ViewModels.SharedViewModel.UserSharedViewModel
import com.example.movieapp.R
import com.example.movieapp.Untils.Resource
import com.example.movieapp.ViewModels.ProfileViewModel
import com.example.movieapp.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {
    private lateinit var binding:FragmentProfileBinding
    private val viewModel by viewModels<ProfileViewModel>()
    private val sharedViewModel by viewModels<UserSharedViewModel>()

    // Launcher để chọn ảnh
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.updateImageUser(it)
        } ?: Toast.makeText(requireContext(), "Không chọn được ảnh", Toast.LENGTH_SHORT).show()
    }

    // Launcher để yêu cầu quyền
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            Toast.makeText(requireContext(), "Cần quyền để chọn ảnh", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            sharedViewModel.user.collect{
                when(it){
                    is Resource.Loading -> {binding.progressBarProfile.visibility = View.VISIBLE}
                    is Resource.Success -> {
                        binding.progressBarProfile.visibility = View.GONE
                        Glide.with(this@ProfileFragment).load(it.data!!.imgPath).error(R.drawable.default_avata).into(binding.imgAvatar)
                        binding.tvUserName.text = it.data.userName
                    }
                    is Resource.Error -> {Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                        binding.progressBarProfile.visibility = View.GONE}
                    else -> Unit
                }
            }
        }
        binding.imgEdit.setOnClickListener{
            showDialogEditUserName()
        }
        lifecycleScope.launch {
            viewModel.updaterUserName.collect{
                when(it){
                    is Resource.Success ->{Toast.makeText(requireContext(),"Cập nhật user name thành cong",Toast.LENGTH_SHORT).show()
                    binding.tvUserName.text = it.data
                        binding.progressBarProfile.visibility = View.GONE}
                    is Resource.Error ->{Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                        binding.progressBarProfile.visibility = View.GONE}
                    is Resource.Loading ->{
                        binding.progressBarProfile.visibility = View.VISIBLE
                    }
                    else -> Unit
                }
            }
        }
        binding.imgAvatar.setOnClickListener {
            requestImagePermission()
        }
        lifecycleScope.launch {
            viewModel.updaterUserImg.collect{
                when(it){
                    is Resource.Success ->{Glide.with(this@ProfileFragment).load(it.data).into(binding.imgAvatar)
                    Toast.makeText(requireContext(),"Cập nhật avatar thành công",Toast.LENGTH_SHORT).show()
                        binding.progressBarProfile.visibility = View.GONE}
                    is Resource.Error -> {Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT)
                        binding.progressBarProfile.visibility = View.GONE}
                    is Resource.Loading -> { binding.progressBarProfile.visibility = View.VISIBLE}
                    else -> Unit
                }
            }
        }
        binding.llDoiMatKhau.setOnClickListener{
            showChangePasswordDialog()
        }
        lifecycleScope.launch {
            viewModel.changePassword.collect{
                when(it){
                    is Resource.Success ->{ Toast.makeText(requireContext(),"Doi mat khau thanh cong",Toast.LENGTH_SHORT).show()
                    binding.progressBarProfile.visibility = View.GONE}
                    is Resource.Error ->{
                        binding.progressBarProfile.visibility = View.GONE
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading ->{binding.progressBarProfile.visibility = View.VISIBLE}
                    else -> Unit
                }
            }
        }
        binding.llLogout.setOnClickListener{
            AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc muốn đăng xuất không?")
                .setPositiveButton("Có") { _, _ -> viewModel.logout()
                    Intent(requireActivity(),LoginActivity::class.java).also {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(it)
                        requireActivity().finish()
                    }
                }
                .setNegativeButton("Không") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }
    private fun showDialogEditUserName(){
        val editText = EditText(requireContext())
        editText.setText(binding.tvUserName.text.toString())
        val dialog = AlertDialog.Builder(requireContext())
            .setView(editText)
            .setTitle("User name")
            .setPositiveButton("Sửa",null)
            .setNegativeButton("Cancel"){dialog,_  ->
                dialog.dismiss()
            }
            .create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{
            val username = editText.text.toString()

            if (username.isNotEmpty()) {
                viewModel.updateUserName(userName = username)
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "User name không được để trống", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun requestImagePermission() {
        val permission = if (android.os.Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (requireContext().checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*") // Mở bộ chọn ảnh
    }
    private fun showChangePasswordDialog() {
        // Tạo LinearLayout chứa 2 EditText
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val oldPasswordEditText = EditText(requireContext()).apply {
            hint = "Mật khẩu cũ"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(oldPasswordEditText)

        val newPasswordEditText = EditText(requireContext()).apply {
            hint = "Mật khẩu mới"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        layout.addView(newPasswordEditText)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Đổi mật khẩu")
            .setView(layout)
            .setPositiveButton("Đổi", null)
            .setNegativeButton("Hủy") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString().trim()
            val newPassword = newPasswordEditText.text.toString().trim()

            when {
                oldPassword.isEmpty() -> Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu cũ", Toast.LENGTH_SHORT).show()
                newPassword.isEmpty() -> Toast.makeText(requireContext(), "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show()
                newPassword.length < 6 -> Toast.makeText(requireContext(), "Mật khẩu mới phải dài ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                else -> {
                    viewModel.changePassword(oldPassword, newPassword)
                    dialog.dismiss()
                }
            }
        }
    }

}