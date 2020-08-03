package com.tofukma.serverorderapp.callback

import com.tofukma.serverorderapp.model.CategoryModel

interface ICategoryCallBackListener {
    fun onCategoryLoadSuccess(categoriesList:List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)

}