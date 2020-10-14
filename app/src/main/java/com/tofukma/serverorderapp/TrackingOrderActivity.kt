package com.tofukma.serverorderapp

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tofukma.serverorderapp.callback.ISingleShippingOrderCallbackListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.ShippingOrderModel
import com.tofukma.serverorderapp.remote.IGoogleAPI
import com.tofukma.serverorderapp.remote.RetrofitGooleAPIServer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.lang.Exception
import java.lang.StringBuilder

class TrackingOrderActivity : AppCompatActivity(), OnMapReadyCallback,
    ISingleShippingOrderCallbackListener {

    private lateinit var mMap: GoogleMap
    private var iSingleShippingOrderCallbackListener:ISingleShippingOrderCallbackListener?=null

    private var shipperMarker: Marker?=null
    private var polylineOptions:PolylineOptions?=null
    private var blackPolylineOptions:PolylineOptions?=null
    private var redPolyline:Polyline?=null
    private var polylineList:List<LatLng> = ArrayList()

    private lateinit var iGoogleAPI: IGoogleAPI;
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tracking_order)

        iGoogleAPI = RetrofitGooleAPIServer.instance!!.create(IGoogleAPI::class.java)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initViews()
    }

    private fun initViews() {
        iSingleShippingOrderCallbackListener = this
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap!!.uiSettings.isZoomControlsEnabled = true
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this,
                R.raw.uber_light_with_label))
            if(!success)
                Log.d("TESTMAP","Failed to load map style")
        }catch ( ex: Resources.NotFoundException){
            Log.d("TESTMAP","NOT found json string for map style")
        }

        checkOrderFromFirebase()
    }

    private fun checkOrderFromFirebase() {
        FirebaseDatabase.getInstance()
            .getReference(Common.SHIPPING_ORDER_REF)
            .child(Common.currentOrderSelected!!.orderNumber!!)
            .addListenerForSingleValueEvent(object:ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists()){
                        val shippingOrderModel = p0.getValue(ShippingOrderModel::class.java)
                        shippingOrderModel!!.key = p0.key

                        iSingleShippingOrderCallbackListener!!.onSingleShippingOrderSuccess(shippingOrderModel)
                    }else{
                        Toast.makeText(this@TrackingOrderActivity,"Order khoong tim thay order", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Toast.makeText(this@TrackingOrderActivity,p0.message, Toast.LENGTH_SHORT).show()
                }

            })
    }

    override fun onSingleShippingOrderSuccess(shippingOrderModel: ShippingOrderModel) {
        val locationOrder = LatLng(shippingOrderModel.orderModel!!.lat,
            shippingOrderModel.orderModel!!.lng)
        val locationShipper = LatLng(shippingOrderModel.currentLat,shippingOrderModel.currentLng)

        //Addbox
        mMap.addMarker(MarkerOptions()
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.box))
            .title(shippingOrderModel!!.orderModel!!.userName)
            .snippet(shippingOrderModel!!.orderModel!!.shippingAddress)
            .position(locationOrder))

        //Add Shipper
        if(shipperMarker == null)
        {
            val height = 80
            val with = 80
            val bitmapDrawable = ContextCompat.getDrawable(this@TrackingOrderActivity, R.drawable.shipp)
            as BitmapDrawable
            val resize = Bitmap.createScaledBitmap(bitmapDrawable.bitmap,with,height, false)

            shipperMarker = mMap.addMarker(MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(resize))
                .title(shippingOrderModel!!.shipperName)
                .snippet(shippingOrderModel!!.shipperPhone)
                .position(locationShipper))

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18.0f))
        }
        else
        {
            shipperMarker!!.position = locationShipper
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18.0f))
        }

        //Draw route
        val to = StringBuilder().append(shippingOrderModel!!.orderModel!!.lat)
            .append(",")
            .append(shippingOrderModel!!.orderModel!!.lng)
            .toString()

        val from = StringBuilder().append(shippingOrderModel!!.currentLat)
            .append(",")
            .append(shippingOrderModel!!.currentLng)
            .toString()

        compositeDisposable.add(iGoogleAPI!!.getDirections("driving", "less_driving",
            from, to ,
            getString(R.string.google_maps_key))!!
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ s->
                try{

                    val jsonObjects = JSONObject(s)
                    val jsonArray = jsonObjects.getJSONArray("routes")
                    for(i in 0 until jsonArray.length())
                    {
                        val route = jsonArray.getJSONObject(i)
                        val poly = route.getJSONObject("overview_polyline")
                        val polyline = poly.getString("points")
                        polylineList = Common.decodePoly(polyline)
                    }

                    polylineOptions = PolylineOptions()
                    polylineOptions!!.color(Color.RED)
                    polylineOptions!!.width(10.0f)
                    polylineOptions!!.startCap(SquareCap())
                    polylineOptions!!.endCap(SquareCap())
                    polylineOptions!!.jointType(JointType.ROUND)
                    polylineOptions!!.addAll(polylineList)
                    redPolyline = mMap.addPolyline(polylineOptions)


                }catch (e: Exception){
                    Log.d("DEBUG",e.message.toString())
                }
            },{ throwable ->
                Toast.makeText(this@TrackingOrderActivity,"Loi"+throwable.message,Toast.LENGTH_SHORT).show()
                throwable.printStackTrace()
            }))
    }
}