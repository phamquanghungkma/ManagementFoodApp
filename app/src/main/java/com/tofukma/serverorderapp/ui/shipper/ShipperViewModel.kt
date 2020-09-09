package com.tofukma.serverorderapp.ui.shipper

import android.app.AlertDialog
import android.widget.Button
import android.widget.RadioButton
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tofukma.serverorderapp.callback.IShipperLoadCallbackListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.CategoryModel
import com.tofukma.serverorderapp.model.OrderModel
import com.tofukma.serverorderapp.model.ShipperMOdel

class ShipperViewModel : ViewModel(), IShipperLoadCallbackListener {

    private var shipperListMutable: MutableLiveData<List<ShipperMOdel>> ?= null
    private  var messageError: MutableLiveData<String> = MutableLiveData()
    private val shipperCallbackListener : IShipperLoadCallbackListener

    init {
        shipperCallbackListener = this

    }
    fun getShipperList():MutableLiveData<List<ShipperMOdel>>{
        if (shipperListMutable == null)
        {
            shipperListMutable = MutableLiveData()
            loadShipper()
        }
        return shipperListMutable!!
    }

     fun loadShipper() {
        val tempList = ArrayList<ShipperMOdel>()
         val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
         shipperRef.addListenerForSingleValueEvent(object : ValueEventListener{
             override fun onCancelled(p0: DatabaseError) {
                shipperCallbackListener.onShipperLoadFailed(p0.message)
             }

             override fun onDataChange(snapshot: DataSnapshot) {
                 // neu lay du lieu thanh cong
                 for (itemSnapShot in snapshot.children){
                     val model = itemSnapShot.getValue<ShipperMOdel>(ShipperMOdel::class.java)
                     model!!.key = itemSnapShot.key
                     tempList.add(model)
                 }
                 shipperCallbackListener.onShipperLoadSuccess(tempList)
             }

         })
    }

    fun getMessageError():MutableLiveData<String>{

        return messageError
    }

    override fun onShipperLoadSuccess(shipperList: List<ShipperMOdel>) {
        shipperListMutable!!.value = shipperList
    }

    override fun onShipperLoadFailed(message: String) {
            messageError.value = message
    }

    override fun onShipperLoadSuccess(
        pos: Int,
        oderModel: OrderModel?,
        shipperList: List<ShipperMOdel>?,
        dialog: AlertDialog,
        ok: Button?,
        cancel: Button?,
        rdi_shipping: RadioButton?,
        rdi_shipped: RadioButton?,
        rdi_cancelled: RadioButton?,
        rdi_delete: RadioButton?,
        rdi_restore_placed: RadioButton?
    ) {
        //do nothing
    }
}