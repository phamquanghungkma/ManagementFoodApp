package com.tofukma.serverorderapp.callback

import com.tofukma.serverorderapp.model.CategoryModel
import com.tofukma.serverorderapp.model.OrderModel

interface IOrderCallbackListener {
    fun onOrderLoadSuccess(orderModel:List<OrderModel>)
    fun onOrderLoadFailed(message:String)

}