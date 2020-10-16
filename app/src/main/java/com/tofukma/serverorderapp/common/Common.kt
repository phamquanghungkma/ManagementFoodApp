package com.tofukma.serverorderapp.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.model.*
import com.tofukma.shippingapp.model.TokenModel
import java.lang.StringBuilder
import kotlin.random.Random

object Common {

    var bestDealsSelected: BestDealsModel?=null
    val BEST_DEALS: String="BestDeals"
    val IS_OPEN_ACTIVITY_NEW_ORDER: String?="IsOpenActivityOrder"
    var currentOrderSelected: OrderModel?=null
    val SHIPPING_ORDER_REF: String="ShippingOrder"
    val SHIPPER_REF: String = "Shippers"
    val ORDER_REF: String = "Order"
    var foodSelected: FoodModel ?= null
    var categorySelected: CategoryModel?= null
    val CATEGORY_REF: String = "Category"
    val SERVER_REF = "Server"
    var currentServerUser: ServerUserModel?= null
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0
    const val TOKEN_REF =  "TOKENS"
    const val NOTI_TITLE = "title"
    const val NOTI_CONTENT = "content"


    fun setSpanString(welcome: String, name: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun setPanStringColor(welcome: String, name: String?, txtUser: TextView?, color: Int) {
        val builder = SpannableStringBuilder()
        builder.append(welcome)
        val txtSpannable = SpannableString(name)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        txtSpannable.setSpan(ForegroundColorSpan(color), 0, name!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser!!.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun convertStatusToString(orderStatus: Int): String? =
        when(orderStatus){
            0 -> "Đặt hàng"
            1 -> "Đang Chuyển Hàng"
            2 -> "Đến nơi"
            -1 -> "Hủy đơn hàng"
            else -> "Lỗi đặt hàng"
        }
    fun createOrderNumber(): String {
        return StringBuilder().append(System.currentTimeMillis()).append(Math.abs(Random.nextInt())).toString()

    }
    fun getNewOrderTopic(): String {

        return StringBuilder("/topics/new_order").toString()

    }

    fun updateToken(context: Context, token: String,isServerToken:Boolean,isShipperToken:Boolean) {
        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REF)
            .child(Common.currentServerUser!!.uid!!)
            .setValue(TokenModel(currentServerUser!!.phone!!,token,isServerToken,isShipperToken))
            .addOnFailureListener { e -> Toast.makeText(context,""+e.message, Toast.LENGTH_SHORT).show() }
    }

    fun showNotification(context: Context, id: Int, title: String?, content: String?, intent: Intent?) {
        var pendingIntent  : PendingIntent?= null
        if(intent != null)
            pendingIntent = PendingIntent.getActivity(context,id,intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val NOTIFICATION_CHANNEL_ID = "com.tofukma.orderapp"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >=  Build.VERSION_CODES.O)
        {
            val notificationChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,"OrderApp",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = " Order App "
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = (Color.RED)
            notificationChannel.vibrationPattern = longArrayOf(0,1000,500,1000)

            notificationManager.createNotificationChannel(notificationChannel)

        }
        //// Apply the layouts to the notification
        val builder = NotificationCompat.Builder(context,NOTIFICATION_CHANNEL_ID)

        builder.setContentTitle(title!!)
                .setContentText(content!!).setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_baseline_restaurant_menu_24))

        if(pendingIntent != null)
            builder.setContentIntent(pendingIntent)

        val notification = builder.build()

        notificationManager.notify(id,notification)

    }

    fun decodePoly(encoded: String): List<LatLng> {
        val poly:MutableList<LatLng> = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len)
        {
            var b:Int
            var shift = 0
            var result = 0
            do{
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift +=5
            }while ( b >= 0x20)
            val dlat = if(result and 1 !=0 ) (result shr 1 ).inv() else result shr 1
            lat +=dlat
            shift = 0
            result = 0
            do{
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift +=5
            }while ( b >= 0x20)
            val dlng = if(result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng
            val p = LatLng(lat.toDouble() / 1E5,lng.toDouble()/1E5)
            poly.add(p)
        }
        return poly
    }

}