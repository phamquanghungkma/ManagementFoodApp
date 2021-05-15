package com.tofukma.serverorderapp.model

class ServerUserModel  {
    var uid:String ?= null
    var name:String ?= null
    var phone:String ?= null
    var isActive:Boolean = false
    var restaurant:String?=null

    constructor()

    constructor(uid: String?, name: String?, phone: String?, isActive: Boolean,restaurant: String?){
        this.uid = uid
        this.name = name
        this.phone = phone
        this.isActive = isActive
        this.restaurant = restaurant

    }


}