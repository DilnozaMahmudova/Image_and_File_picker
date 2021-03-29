package com.company.dilnoza.permission.ui.dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import com.company.dilnoza.permission.R
import kotlinx.android.synthetic.main.settings.*

class FileDialog(context: Context, private val currentCount:Int, private val currentSize:Float) :AlertDialog(context) {
    @SuppressLint("InflateParams")
    private val contentView = LayoutInflater.from(context).inflate(R.layout.settings, null, false)
    private var listener: ((Int, Float) -> Unit)? = null

    init {
        setView(contentView)
        setButton(BUTTON_POSITIVE, "OK") { _, _ ->
        }
        setButton(BUTTON_NEGATIVE, "Cancel") { _, _ -> }
         setOnShowListener {
            val button = this.getButton(BUTTON_POSITIVE)
            button.setOnClickListener {
                contentView.apply {
                    if (input_max_count.text.isNullOrEmpty()) {
                        input_layout_max_count.error = "Maydonni to'ldiring!"
                        input_max_count.requestFocus()
                        return@setOnClickListener
                    }
                    if (input_max_size.text.isNullOrEmpty()) {
                        input_layout_max_size.error = "Maydonni to'ldiring!"
                        input_max_size.requestFocus()
                        return@setOnClickListener
                    }
                    val count = input_max_count.text.toString().toInt()
                    if (count < currentCount) {
                        input_layout_max_count.error = "Joriy media sonidan ko'proq son kiriting!"
                        input_max_count.requestFocus()
                        return@setOnClickListener
                    }
                    val size = input_max_size.text.toString().toFloat()
                    if (size < currentSize) {
                        input_layout_max_size.error =
                            "Joriy media o'lchamidan ko'proq son kiriting!"
                        input_max_size.requestFocus()
                        return@setOnClickListener
                    }
                    listener?.invoke(count,size)
                    dismiss()
                }
            }
        }
    }
    fun setOnClickListener(block: ((Int, Float) -> Unit)?) {
        listener = block
    }
}