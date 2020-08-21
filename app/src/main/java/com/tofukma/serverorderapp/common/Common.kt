package com.tofukma.serverorderapp.common

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import com.tofukma.serverorderapp.model.CategoryModel
import com.tofukma.serverorderapp.model.FoodModel
import com.tofukma.serverorderapp.model.ServerUserModel

object Common {

    var foodSelected: FoodModel ?= null
    var categorySelected: CategoryModel?= null
    val CATEGORY_REF: String = "Category"
    val SERVER_REF = "Server"
    var currentServerUser: ServerUserModel?= null
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0

    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder, TextView.BufferType.SPANNABLE)
    }

}