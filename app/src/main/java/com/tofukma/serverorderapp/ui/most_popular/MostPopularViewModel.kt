package com.tofukma.serverorderapp.ui.most_popular

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tofukma.serverorderapp.callback.IMostPopularCallbackListen
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.BestDealsModel
import com.tofukma.serverorderapp.model.MostPopularModel

class MostPopularViewModel : ViewModel(), IMostPopularCallbackListen {
    private var mostPopularListMutable : MutableLiveData<List<MostPopularModel>>?= null
    private var messageError: MutableLiveData<String> = MutableLiveData()
    private  val mostPopularCallbackListen: IMostPopularCallbackListen

    init {
        mostPopularCallbackListen = this

    }

    fun getMostPopularList():MutableLiveData<List<MostPopularModel>>{
        if (mostPopularListMutable == null)
        {
            mostPopularListMutable = MutableLiveData()
        }
        loadMostPopulars()
        return this.mostPopularListMutable!!
    }

    private fun loadMostPopulars() {
        val tempList = ArrayList<MostPopularModel>()
        val mostPopularRef = FirebaseDatabase.getInstance().getReference(Common.MOST_POPULAR)
        mostPopularRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                mostPopularCallbackListen.onListMostPopularLoadFailed((error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot.children){
                    val model = itemSnapShot.getValue<MostPopularModel>(MostPopularModel::class.java)
                    model!!.key = itemSnapShot.key!!
                    tempList.add(model)
                }
                mostPopularCallbackListen.onListMostPopularLoadSuccess(tempList)
            }
        })
    }

    fun getMessageError():MutableLiveData<String>{
        return messageError
    }

    override fun onListMostPopularLoadSuccess(mostPopularModel: List<MostPopularModel>) {
        mostPopularListMutable!!.value = mostPopularModel
    }

    override fun onListMostPopularLoadFailed(message: String) {
        messageError.value = message
    }
}