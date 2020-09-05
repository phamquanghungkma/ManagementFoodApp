package com.tofukma.serverorderapp.callback

import com.tofukma.serverorderapp.model.ShipperMOdel

interface IShipperLoadCallbackListener {
    fun onShipperLoadSuccess(shipperList:List<ShipperMOdel>)
    fun onShipperLoadFailed(message:String)
}