package com.rahulraghuwanshi.authflow

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import java.util.regex.Pattern

class SMSReader() : BroadcastReceiver() {

    var otpSmsListener: OTPSMSListener? = null

    fun init(otpSmsListener: OTPSMSListener){
        this.otpSmsListener = otpSmsListener
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
            val bundle: Bundle? = intent.extras
            if (bundle != null) {
                val status: Status = bundle.get(SmsRetriever.EXTRA_STATUS) as Status
                if (status != null) {
                    when (status.statusCode) {
                        CommonStatusCodes.SUCCESS -> {
                            val message = bundle.get(SmsRetriever.EXTRA_SMS_MESSAGE) as String
                            if (message != null) {
                                val pattern = Pattern.compile("\\d{6}")
                                val matcher = pattern.matcher(message)
                                if (matcher.find()) {
                                    val otp = matcher.group(0)

                                    if (otp != null) {
                                        otpSmsListener?.onSuccess(otp)
                                    } else {
                                        otpSmsListener?.onFailure("null otp")
                                    }
                                }
                            }
                        }
                        CommonStatusCodes.TIMEOUT -> {
                            otpSmsListener?.onFailure("timeout")
                        }
                    }
                }
            }
        }
    }

    interface OTPSMSListener {
        fun onSuccess(otp: String)
        fun onFailure(msg: String)
    }
}