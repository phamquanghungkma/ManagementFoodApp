package com.tofukma.serverorderapp.common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.itextpdf.text.Document
import com.itextpdf.text.Image
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.model.*
import com.tofukma.shippingapp.model.TokenModel
import io.reactivex.Observable
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.File.separator
import java.lang.StringBuilder
import kotlin.random.Random

object Common {

    val FILE_PRINT: String = "last_order_print"
    val RESTAURANT_REF: String = "Restaurant"
    val IMAGE_URL: String="IMAGE_URL"
    val IS_SEND_IMAGE: String="IS_SEND_IMAGE"
    var mostPopularSelected: MostPopularModel?=null
    val MOST_POPULAR: String ="MostPopular"
    var bestDealsSelected: BestDealsModel?=null
    val BEST_DEALS: String="BestDeals"
    val IS_OPEN_ACTIVITY_NEW_ORDER: String?="IsOpenActivityOrder"
    var currentOrderSelected: OrderModel?=null
    val SHIPPING_ORDER_REF: String="ShippingOrder"
    val SHIPPER_REF: String = "Shippers"
    val ORDER_REF: String = "Order"
    var foodSelected: FoodModel ?= null
    var categorySelected: CategoryModel?= null
    const val CATEGORY_REF: String = "Category"
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
            0 -> " đơn mới"
            1 -> " đang Chuyển Hàng"
            2 -> " đã đến nơi"
            -1 -> " đơn bị huỷ "
            else -> " Lỗi đặt hàng"
        }
    fun createOrderNumber(): String {
        return StringBuilder().append(System.currentTimeMillis()).append(Math.abs(Random.nextInt())).toString()

    }
    fun getNewOrderTopic(): String {

        return StringBuilder("/topics/")
            .append(Common.currentServerUser!!.restaurant)
            .append("_")
            .append("new_order")
            .toString()

    }

    fun updateToken(context: Context, token: String,isServerToken:Boolean,isShipperToken:Boolean) {
        if(Common.currentServerUser != null){
            FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REF)
                .child(Common.currentServerUser!!.uid!!)
                .setValue(TokenModel(currentServerUser!!.phone!!,token,isServerToken,isShipperToken))
                .addOnFailureListener { e -> Toast.makeText(context,""+e.message, Toast.LENGTH_SHORT).show() }
        }
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
            Log.d("content",content!!)

        builder.setContentTitle(title!!)
                .setContentText(content!!).setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources,R.drawable.ic_baseline_restaurant_menu_24))
             .setStyle(NotificationCompat.BigTextStyle().bigText(content))


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

    fun getNewsTopic(): String {
        return StringBuilder("/topics/")
            .append(Common.currentServerUser!!.restaurant!!)
            .append("_")
            .append("news")
            .toString()
    }
    fun getBearing(begin: LatLng, end: LatLng): Float {
        val lat = Math.abs(begin.latitude - end.longitude)
        val lng = Math.abs(begin.longitude - end.longitude)

        if(begin.latitude < end.latitude && begin.longitude < end.longitude)
            return Math.toDegrees(Math.atan(lng/lat))
                .toFloat()

        else  if(begin.latitude >=  end.latitude && begin.longitude < end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng/lat))+90).toFloat()

        else if(begin.latitude >=  end.latitude && begin.longitude >= end.longitude)
            return (Math.toDegrees(Math.atan(lng/lat))+180).toFloat()

        else if(begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (90 - Math.toDegrees(Math.atan(lng/lat))+270).toFloat()
        return (-1).toFloat()
    }

    fun getAppPath(context: Context): String {
            val code = context.packageManager.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,context.packageName
            )
        if( code == PackageManager.PERMISSION_GRANTED){
            val dir = File(Environment.getExternalStorageDirectory().toString()
                    +File.separator
                    +context.resources.getString(R.string.app_name)
                    +File.separator)

            if(!dir.exists())
                dir.mkdir()

            return  dir.path +File.separator;
        }
//        val dir = File(Environment.getExternalStorageDirectory().toString()
//        +File.separator
//        +context.resources.getString(R.string.app_name)
//        +File.separator)
//
//        if(!dir.exists())
//            dir.mkdir()
//        return  dir.path +File.separator;
            return "path"
    }

    fun getBitmapFromUrl(context: Context, cartItem: CartItem, document: Document): Observable<CartItem> {
        return Observable.fromCallable {
            val  bitmap = Glide.with(context)
                .asBitmap()
                .load(cartItem.foodImage)
                .submit().get()
            val  image = Image.getInstance(bitmapToByteArray(bitmap))
            image.scaleAbsolute(80.0f, 80.0f)
            document.add(image)
            cartItem
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap?): ByteArray {
            val stream = ByteArrayOutputStream()
            bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    fun formatSizeJsonToString(foodSize: String): String {
        return  if (foodSize.equals("Default")) foodSize else{
            val gson = Gson()
            val sizeMode = gson.fromJson(foodSize,SizeModel::class.java)
            sizeMode.name!!
        }
    }

    fun formatAddonJsonToString(foodAddon: String): String {
        return  if (foodAddon.equals("Default")) foodAddon else{
            val stringBuilder = StringBuilder()
            val gson = Gson()
            val addonModels = gson.fromJson<List<AddonModel>>(foodAddon,object:
                TypeToken<List<AddonModel>>(){}.type)
            for(addon in addonModels)
                stringBuilder.append(addon.name).append(",")
            stringBuilder.substring(0, stringBuilder.length-1)
        }
    }
}