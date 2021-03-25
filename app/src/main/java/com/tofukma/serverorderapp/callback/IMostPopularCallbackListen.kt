package com.tofukma.serverorderapp.callback

import com.tofukma.serverorderapp.model.MostPopularModel

interface IMostPopularCallbackListen {
    fun onListMostPopularLoadSuccess(mostPopularModel: List<MostPopularModel>)
    fun onListMostPopularLoadFailed(message:String)
}