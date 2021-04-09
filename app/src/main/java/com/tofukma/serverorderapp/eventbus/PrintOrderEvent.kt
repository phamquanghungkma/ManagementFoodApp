package com.tofukma.serverorderapp.eventbus

import com.tofukma.serverorderapp.model.OrderModel

class PrintOrderEvent(var path: String, var orderModel: OrderModel) {
}