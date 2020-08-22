package com.tofukma.serverorderapp.ui.order

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tofukma.serverorderapp.callback.IOrderCallbackListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.OrderModel
import java.util.*
import kotlin.collections.ArrayList


class OrderViewModel : ViewModel(), IOrderCallbackListener {

    private var orderModelList = MutableLiveData<List<OrderModel>>()
    val messsageError = MutableLiveData<String>()
    private val orderCallbackListener:IOrderCallbackListener

    init {
        orderCallbackListener = this
    }
    fun getOrderModelList():MutableLiveData<List<OrderModel>>{
        loadOrder(0)
        return orderModelList
    }

    private fun loadOrder(status: Int) {

        val tempList  : MutableList<OrderModel> = ArrayList()
        val orderRef = FirebaseDatabase.getInstance()
            .getReference(Common.ORDER_REF)
            .orderByChild("orderStatus")
            .equalTo(status.toDouble())
        orderRef.addListenerForSingleValueEvent(object :ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
               orderCallbackListener.onOrderLoadFailed(p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
               for (itemSnapShot in p0.children){
//                   Log.e(TAG,"+1 oder")
                   val orderModel = itemSnapShot.getValue(OrderModel::class.java)
                   orderModel!!.key = itemSnapShot.key
                   tempList.add(orderModel)
               }
                orderCallbackListener.onOrderLoadSuccess(tempList)
            }
        })
    }

    override fun onOrderLoadSuccess(orderModel: List<OrderModel>) {
       if (orderModel.size > 0)
           orderModelList.value = orderModel
    }

    override fun onOrderLoadFailed(message: String) {
        messsageError.value = message
    }


}