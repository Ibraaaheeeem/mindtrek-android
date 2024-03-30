package com.haneef.mindtrek.util

import android.app.AlertDialog
import android.content.Context

class AlertUtils(private val context: Context) {

    fun showAlert(title: String, message: String, positiveButtonTitle: String = "OK", onPositiveButtonClick: (() -> Unit)? = null) {
        val builder = AlertDialog.Builder(context)

        // Set dialog title and message
        builder.setTitle(title)
        builder.setMessage(message)

        // Set the positive button and its click listener
        builder.setPositiveButton(positiveButtonTitle) { dialog, _ ->
            dialog.dismiss()
            onPositiveButtonClick?.invoke()
        }

        // Create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }
}
