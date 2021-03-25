package com.tofukma.serverorderapp.eventbus

import com.tofukma.serverorderapp.model.SizeModel

class UpdateSizeModel {
    var sizeModeList:List<SizeModel>?=null
    constructor(){}
    constructor(sizeMdelList: List<SizeModel>?){
        this.sizeModeList = sizeModeList
    }
}