package com.tofukma.serverorderapp.callback

import com.tofukma.serverorderapp.model.ShippingOrderModel

interface ISingleShippingOrderCallbackListener {
    fun onSingleShippingOrderSuccess(shippingOrderModel: ShippingOrderModel)
}