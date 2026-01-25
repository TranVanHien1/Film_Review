package com.example.danhgiaphim.adapter

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.example.danhgiaphim.R

class LoadingDialog(private val context: Context) {
    private var dialog: AlertDialog? = null

    fun show(message: String = "Vui lòng đợi...") {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null)
        val builder = AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)

        dialog = builder.create()
        dialog?.show()
    }

    fun dismiss() {
        dialog?.dismiss()
    }
}