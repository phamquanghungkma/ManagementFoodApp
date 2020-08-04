package com.tofukma.serverorderapp.eventbus

import com.tofukma.serverorderapp.model.CategoryModel

class CategoryClick(var isSuccess:Boolean, var category: CategoryModel) {
}