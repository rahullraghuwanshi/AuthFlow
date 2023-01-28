package com.rahulraghuwanshi.authflow.extension

import android.app.Dialog
import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.rahulraghuwanshi.authflow.R

fun Fragment.progressDialog(): Dialog {
    val dialog = Dialog(this.requireContext())
    dialog.setContentView(R.layout.layout_custom_progress_dialog)
    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    dialog.setCancelable(false)
    return dialog
}

fun Fragment.showToast(msg : String){
    Toast.makeText(this.requireContext(),msg,Toast.LENGTH_LONG).show()
}