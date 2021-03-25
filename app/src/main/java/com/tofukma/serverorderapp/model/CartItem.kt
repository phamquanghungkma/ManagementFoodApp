package com.tofukma.serverorderapp.model

import androidx.annotation.NonNull

class CartItem {


    var foodId:String =""


    var foodName:String?=null


    var foodImage:String?=null


    var foodPrice:Double=0.0


    var foodQuantity:Int=0


    var foodAddon:String?=null


    var foodSize:String?=null


    var userPhone:String?=""



    var foodExtraPrice:Double = 0.0


    var uid:String?=""

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if(other !is CartItem)
            return false
        val cartItem = other as CartItem?
        return cartItem!!.foodId ==this.foodId &&
                cartItem.foodAddon == this.foodAddon &&
                cartItem.foodSize == this.foodSize

    }
}