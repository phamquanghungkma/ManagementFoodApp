package com.tofukma.serverorderapp.callback

import com.tofukma.serverorderapp.model.BestDealsModel

interface IBestDealsCallBackListener {
    fun onListBestDealsLoadSuccess(bestDealsModels: List<BestDealsModel>)
    fun onListBestDealsLoadFailed(message:String)
}