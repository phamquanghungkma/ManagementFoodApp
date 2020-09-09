package com.tofukma.serverorderapp.callback

import android.app.AlertDialog
import android.widget.Button
import android.widget.RadioButton
import com.tofukma.serverorderapp.model.OrderModel
import com.tofukma.serverorderapp.model.ShipperMOdel

interface IShipperLoadCallbackListener {
    fun onShipperLoadSuccess(shipperList:List<ShipperMOdel>)
    fun onShipperLoadSuccess(pos:Int,oderModel:OrderModel?,
                             shipperList: List<ShipperMOdel>?,
                             dialog: AlertDialog,
                             ok:Button?,cancel:Button?,
                             rdi_shipping:RadioButton?,
                             rdi_shipped:RadioButton?,
                             rdi_cancelled:RadioButton?,
                             rdi_delete:RadioButton?,
                             rdi_restore_placed:RadioButton?)

    fun onShipperLoadFailed(message:String)
}