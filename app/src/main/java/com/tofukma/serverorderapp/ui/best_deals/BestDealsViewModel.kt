package com.tofukma.serverorderapp.ui.best_deals

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tofukma.serverorderapp.callback.IBestDealsCallBackListener
import com.tofukma.serverorderapp.callback.ICategoryCallBackListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.BestDealsModel
import com.tofukma.serverorderapp.model.CategoryModel

class BestDealsViewModel : ViewModel(), IBestDealsCallBackListener {
    private var bestDealsListMutable : MutableLiveData<List<BestDealsModel>>?= null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private  val bestDealsCallBackListener: IBestDealsCallBackListener

    init {
        bestDealsCallBackListener = this
    }


    fun getBestDealsList():MutableLiveData<List<BestDealsModel>>{
        if (bestDealsListMutable == null)
        {
            bestDealsListMutable = MutableLiveData()
        }
        loadBestDeals()
        return this.bestDealsListMutable!!
    }

     fun loadBestDeals() {
        val tempList = ArrayList<BestDealsModel>()
        val bestDealsRef = FirebaseDatabase.getInstance().getReference(Common.BEST_DEALS)
        bestDealsRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                bestDealsCallBackListener.onListBestDealsLoadFailed((error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot.children){
                    val model = itemSnapShot.getValue<BestDealsModel>(BestDealsModel::class.java)
                    model!!.key = itemSnapShot.key!!
                    tempList.add(model)
                }
                bestDealsCallBackListener.onListBestDealsLoadSuccess(tempList)
            }
        })
    }

    fun getMessageError():MutableLiveData<String>{

        return messageError
    }

    override fun onListBestDealsLoadSuccess(bestDealsModels: List<BestDealsModel>) {
        bestDealsListMutable!!.value = bestDealsModels
    }

    override fun onListBestDealsLoadFailed(message: String) {
        messageError.value = message
    }
}