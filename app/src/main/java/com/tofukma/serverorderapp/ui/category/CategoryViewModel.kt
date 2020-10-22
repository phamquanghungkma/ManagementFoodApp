package com.tofukma.serverorderapp.ui.category

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tofukma.serverorderapp.callback.ICategoryCallBackListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.CategoryModel

class CategoryViewModel : ViewModel(), ICategoryCallBackListener {

    private var categoriesListMutable : MutableLiveData<List<CategoryModel>> ?= null
    private var messageError:MutableLiveData<String> = MutableLiveData()
    private  val categoryCallBackListener: ICategoryCallBackListener

    init {
        categoryCallBackListener = this
    }

    fun getCategoryList():MutableLiveData<List<CategoryModel>>{
        if (categoriesListMutable == null)
        {
            categoriesListMutable = MutableLiveData()
            loadCategory()
        }
        return categoriesListMutable!!
    }
    fun getMessageError():MutableLiveData<String>{

        return messageError
    }

    override fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>) {
        categoriesListMutable!!.value = categoriesList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError!!.value = message

    }

    fun loadCategory() {
        val tempList = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.CATEGORY_REF)
        //Log.d("REF",categoryRef.toString())
        categoryRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                categoryCallBackListener.onCategoryLoadFailed((error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapShot in snapshot.children){
                    val model = itemSnapShot.getValue<CategoryModel>(CategoryModel::class.java)
                    model!!.menu_id = itemSnapShot.key
                    tempList.add(model)
                }
                categoryCallBackListener.onCategoryLoadSuccess(tempList)
            }
        })

    }

}