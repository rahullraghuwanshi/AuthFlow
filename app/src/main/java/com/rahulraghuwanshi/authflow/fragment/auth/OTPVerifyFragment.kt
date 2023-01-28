package com.rahulraghuwanshi.authflow.fragment.auth

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.rahulraghuwanshi.authflow.R
import com.rahulraghuwanshi.authflow.databinding.FragmentOTPVerifyBinding
import com.rahulraghuwanshi.authflow.extension.progressDialog
import com.rahulraghuwanshi.authflow.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OTPVerifyFragment : Fragment() {

    private var _binding: FragmentOTPVerifyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by navGraphViewModels(R.id.nav_graph)

    private lateinit var dialog: Dialog

    private lateinit var verificationId: String

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOTPVerifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args: OTPVerifyFragmentArgs by navArgs()
        verificationId = args.verificationToken

        viewModel.otpViewEvents.observe(viewLifecycleOwner) {
            when (it) {
                is OTPViewEvents.ShowToast -> {
                    showToast(it.msg)
                }
                is OTPViewEvents.VerifyOTP -> {
                    verifyPhoneNumberWithCode(it.otp)
                }
                is OTPViewEvents.ShowLoginScreen -> {
                    navigateToLogin()
                }
            }
        }

        initView()
    }

    private fun navigateToLogin() {
        findNavController().navigate(R.id.action_OTPVerifyFragment_to_loginFragment)
    }

    private fun verifyPhoneNumberWithCode(code: String) {
        dialog.show()
        if (code.length == 6) {
            // [START verify_with_code]
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
            // [END verify_with_code]
        } else {
            dialog.dismiss()
            showToast("Enter Valid Otp")
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                dialog.dismiss()
                if (task.isSuccessful) {
                    showToast("Login success")
                    navigateToHomeScreen()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("TAG", "signInWithCredential:success")
                    //  val user = task.result?.user
                } else {
                    showToast("Please enter valid OTP")
                    // Sign in failed, display a message and update the UI
                    Log.w("TAG", "signInWithCredential:failure", task.exception)

                    // Update UI
                }
            }
    }

    private fun navigateToHomeScreen() {
        val action = OTPVerifyFragmentDirections.actionOTPVerifyFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun initView() {
        dialog = progressDialog()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            this.viewModel = this@OTPVerifyFragment.viewModel

            txtNumber.text = viewModel?.number?.value
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}