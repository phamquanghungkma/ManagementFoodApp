package com.tofukma.serverorderapp.eventbus

import com.tofukma.serverorderapp.model.AddonModel
import com.tofukma.serverorderapp.model.SizeModel

class UpdateAddonModel {
    var addonModelList:List<AddonModel>?=null
    constructor(){}
    constructor(addonModelList: List<AddonModel>?){
        this.addonModelList = addonModelList
    }
}