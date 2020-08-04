package com.tofukma.serverorderapp.common

import com.tofukma.serverorderapp.model.CategoryModel
import com.tofukma.serverorderapp.model.ServerUserModel

object Common {

    var categorySelected: CategoryModel?= null
    val CATEGORY_REF: String = "Category"
    val SERVER_REF = "Server"
    var currentServerUser: ServerUserModel?= null
    val FULL_WIDTH_COLUMN: Int = 1
    val DEFAULT_COLUMN_COUNT: Int = 0

}