package com.example.surtefacilsv

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

object DialogHelper {

    /**
     * Muestra un diálogo de éxito
     */
    fun showSuccessDialog(
        context: Context,
        title: String = "Pedido realizado con éxito",
        buttonText: String = "Volver",
        onDismiss: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_success)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val btnAction = dialog.findViewById<Button>(R.id.btnDialogAction)

        tvTitle.text = title
        btnAction.text = buttonText

        btnAction.setOnClickListener {
            dialog.dismiss()
            onDismiss?.invoke()
        }

        dialog.show()
    }

    /**
     * Muestra un diálogo de error
     */
    fun showErrorDialog(
        context: Context,
        title: String = "No hay conexión a Internet",
        buttonText: String = "Volver",
        onDismiss: (() -> Unit)? = null
    ) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_error)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val tvTitle = dialog.findViewById<TextView>(R.id.tvDialogTitle)
        val btnAction = dialog.findViewById<Button>(R.id.btnDialogAction)

        tvTitle.text = title
        btnAction.text = buttonText

        btnAction.setOnClickListener {
            dialog.dismiss()
            onDismiss?.invoke()
        }

        dialog.show()
    }

    /**
     * Muestra un diálogo genérico personalizado
     */
    fun showCustomDialog(
        context: Context,
        title: String,
        isSuccess: Boolean,
        buttonText: String = "Volver",
        onDismiss: (() -> Unit)? = null
    ) {
        if (isSuccess) {
            showSuccessDialog(context, title, buttonText, onDismiss)
        } else {
            showErrorDialog(context, title, buttonText, onDismiss)
        }
    }
}