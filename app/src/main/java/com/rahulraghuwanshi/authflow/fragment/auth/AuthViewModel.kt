package com.rahulraghuwanshi.authflow.fragment.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rahulraghuwanshi.authflow.custom.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    // Used to post transient events to the View
    private val _mobileViewEvents = SingleLiveEvent<MobileViewEvents>()
    val mobileViewEvents: LiveData<MobileViewEvents> = _mobileViewEvents

    private val _otpViewEvents = SingleLiveEvent<OTPViewEvents>()
    val otpViewEvents: LiveData<OTPViewEvents> = _otpViewEvents


    val number = MutableLiveData<String>()
    val otp = MutableLiveData<String>()


    fun onGoogleLoginButtonClick() {
        _mobileViewEvents.value = MobileViewEvents.GoogleLogin
    }

    fun onSendOTPButtonClick() {
        if (number.value?.trim()?.length == 10) {
            _mobileViewEvents.value = MobileViewEvents.SendOTP(number.value.toString())
        } else {
            _mobileViewEvents.value = MobileViewEvents.ShowToast("Please Enter Valid Number")
        }
    }

    fun onVerifyOTPButtonClick() {
        _otpViewEvents.value = OTPViewEvents.VerifyOTP(otp.value.toString())
    }

    fun onEditPhoneClick() {
        _otpViewEvents.value = OTPViewEvents.ShowLoginScreen
    }

}

sealed class MobileViewEvents {
    object GoogleLogin : MobileViewEvents()
    data class SendOTP(val number: String) : MobileViewEvents()
    data class ShowToast(val msg: String) : MobileViewEvents()
}

sealed class OTPViewEvents {
    data class VerifyOTP(val otp: String) : OTPViewEvents()
    data class ShowToast(val msg: String) : OTPViewEvents()
    object ShowLoginScreen : OTPViewEvents()
}
