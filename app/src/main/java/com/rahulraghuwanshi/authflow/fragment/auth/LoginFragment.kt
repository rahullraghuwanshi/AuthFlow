package com.rahulraghuwanshi.authflow.fragment.auth

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.rahulraghuwanshi.authflow.R
import com.rahulraghuwanshi.authflow.databinding.FragmentLoginBinding
import com.rahulraghuwanshi.authflow.extension.progressDialog
import com.rahulraghuwanshi.authflow.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var googleSignInClient: GoogleSignInClient

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var dialog: Dialog

    private val viewModel: AuthViewModel by navGraphViewModels(R.id.nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.mobileViewEvents.observe(viewLifecycleOwner) {
            when (it) {
                is MobileViewEvents.GoogleLogin -> {
                    dialog.show()
                    signInUsingGoogle()
                }
                is MobileViewEvents.SendOTP -> {
                    sendOTP(it.number)
                }
                is MobileViewEvents.ShowToast -> {
                    showToast(it.msg)
                }
            }
        }

        initView()
    }

    private fun initView() {
        dialog = progressDialog()

        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            this.viewModel = this@LoginFragment.viewModel
        }
    }

    private fun signInUsingGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        dialog.dismiss()
        launcher.launch(signInIntent)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                dialog.show()
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                handleGoogleSignInResult(task)
            } else {
                Log.d(TAG, "Some unexpected error occurred!!")
            }
        }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        dialog.dismiss()
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                //   val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                navigateToHomeScreen()
            }
        } catch (e: ApiException) {
            e.printStackTrace()
            showToast("Please try again!!")
        }
    }


    private fun sendOTP(number: String) {
        dialog.show()
        val code = binding.countryPicker.defaultCountryCode.toString()
        val mobileNumber = "+$code$number"

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(mobileNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted:$credential")
            //   signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Log.w(TAG, "onVerificationFailed", e)
            dialog.dismiss()
            showToast("Something is wrong!!")
            e.printStackTrace()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Log.d(TAG, "onCodeSent:$verificationId")
            dialog.dismiss()
            navigateToOTPScreen(verificationId)
        }
    }

    private fun navigateToOTPScreen(verificationId: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToOTPVerifyFragment(verificationId)
        findNavController().navigate(action)
    }

    private fun navigateToHomeScreen() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}