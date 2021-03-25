package com.tofukma.serverorderapp.ui.foodlist

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.StringBuilderPrinter
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.SizeAddonEditActivity
import com.tofukma.serverorderapp.adapter.MyFoodListAdapter
import com.tofukma.serverorderapp.callback.IMyButtonCallback
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.common.MySwipeHelper
import com.tofukma.serverorderapp.eventbus.ChangeMenuClick
import com.tofukma.serverorderapp.eventbus.SizeAddonEditEvent
import com.tofukma.serverorderapp.eventbus.ToastEvent
import com.tofukma.serverorderapp.model.FoodModel
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class FoodListFragment : Fragment(){

    private var imageUri: Uri?=null
    private val PICK_IMAGE_REQUEST: Int=1234
    private lateinit var foodListViewModel: FoodListViewModel
    var recycler_food_list : RecyclerView?= null
    var layoutAnimationController: LayoutAnimationController?= null
    var foodModelList :List<FoodModel> =  ArrayList<FoodModel>()


    private var img_food:ImageView ?= null
    private lateinit var storage:FirebaseStorage
    private lateinit var storageReference : StorageReference
    private lateinit var dialog: android.app.AlertDialog


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.food_list_menu, menu)

        //Create search view
        val menuItem = menu.findItem(R.id.action_search)

        val searchManager = activity!!.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menuItem.actionView as androidx.appcompat.widget.SearchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity!!.componentName!!))

        //Event
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(search: String?): Boolean {
                startSearchFood(search!!)
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return false
            }
        })
        // Clear text when click to Clear button
        val closeButton = searchView.findViewById<View>(R.id.search_close_btn) as ImageView
        closeButton.setOnClickListener {
            val ed = searchView.findViewById<View>(R.id.search_src_text) as EditText
            // Clear text
            ed.setText("")
            // Clear query
            searchView.setQuery("",false)
            // Collapse the action View
            searchView.onActionViewCollapsed()
            // Collaps the search widget
            menuItem.collapseActionView()
            // Restore result to original
            foodListViewModel.getMutableFoodModelListData().value = Common.categorySelected!!.foods
        }
    }

    private fun startSearchFood(s: String) {
        val resultFood: MutableList<FoodModel> = ArrayList()
        for (i in Common.categorySelected!!.foods!!.indices)
        {
            val foodModel = Common.categorySelected!!.foods!![i]
            if(foodModel.name!!.toLowerCase().contains(s.toLowerCase()))
            {
                // Here we will save index of search result item
                foodModel.positonInList = i
                resultFood.add(foodModel)
            }
        }
        // Update Search result
        foodListViewModel!!.getMutableFoodModelListData().value = resultFood

    }


    var adapter : MyFoodListAdapter ?= null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        foodListViewModel =
            ViewModelProviders.of(this).get(FoodListViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_list, container, false)
        initViews(root)
        foodListViewModel.getMutableFoodModelListData().observe(viewLifecycleOwner, Observer {
            if(it != null ){
                foodModelList = it
                adapter =  MyFoodListAdapter(requireContext(),it)
                recycler_food_list!!.adapter = adapter
                recycler_food_list!!.layoutAnimation = layoutAnimationController
            }

        })
        return root
    }
    private fun initViews(root: View?) {

        setHasOptionsMenu(true) // Enable option menu on Fragment

        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        recycler_food_list = root!!.findViewById(R.id.recycler_food_list) as RecyclerView
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager = LinearLayoutManager(context)

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)


        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        val swipe = object: MySwipeHelper(context!!, recycler_food_list!!, width/6)
        {
            override fun instantianteMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Xoá",
                    30,
                    0,
                    Color.parseColor("#9b0000"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.foodSelected = foodModelList[pos]
                            val builder = AlertDialog.Builder(context!!)
                            builder.setTitle("Xoá")
                                .setMessage("Bạn có muốn xoá không ?")
                                .setNegativeButton("CANCEL",{dialogInterface, _ -> dialogInterface.dismiss()  })
                                .setPositiveButton("Xoá",{dialogInterface, i ->
                                        val foodModel = adapter!!.getItemAtPosition(pos)
                                        if(foodModel.positonInList == -1)
                                            Common.categorySelected!!.foods!!.removeAt(pos)
                                        else
                                            Common.categorySelected!!.foods!!.removeAt(foodModel.positonInList)
                                        Common.categorySelected!!.foods!!.removeAt(pos)
                                        updateFood(Common.categorySelected!!.foods,true)

                                })

                            val deleteDialog =  builder.create()
                            deleteDialog.show()


                        }
                    })
                )
                buffer.add(MyButton(context!!,
                    "Sửa",
                    30,
                    0,
                    Color.parseColor("#560027"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            val foodModel = adapter!!.getItemAtPosition(pos)
                            if(foodModel.positonInList == -1)
                                showUpdateDialog(pos,foodModel)
                            else
                                showUpdateDialog(foodModel.positonInList,foodModel)
                        }
                    }))

                buffer.add(MyButton(context!!,
                    "Size",
                    30,
                    0,
                    Color.parseColor("#12005e"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {

                            val foodModel = adapter!!.getItemAtPosition(pos)
                            if(foodModel.positonInList == -1)
                                Common.foodSelected = foodModelList!![pos]
                            else
                                Common.foodSelected = foodModel
                            startActivity(Intent(context,SizeAddonEditActivity::class.java))
                            if(foodModel.positonInList == -1)
                                EventBus.getDefault().postSticky(SizeAddonEditEvent(false,pos))
                            else
                                EventBus.getDefault().postSticky(SizeAddonEditEvent(false,foodModel.positonInList))

                        }
                    }))

                buffer.add(MyButton(context!!,
                    "Addon",
                    30,
                    0,
                    Color.parseColor("#333639"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {

                            val foodModel = adapter!!.getItemAtPosition(pos)
                            if(foodModel.positonInList == -1)
                                Common.foodSelected = foodModelList!![pos]
                            else
                                Common.foodSelected = foodModel
                            startActivity(Intent(context,SizeAddonEditActivity::class.java))
                            if(foodModel.positonInList == -1)
                                EventBus.getDefault().postSticky(SizeAddonEditEvent(true,pos))
                            else
                                EventBus.getDefault().postSticky(SizeAddonEditEvent(true,foodModel.positonInList))
                        }
                    }))
            }
        }
    }

    private fun showUpdateDialog(pos: Int , foodModel: FoodModel) {

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Cập nhật")
        builder.setMessage("Sửa thông tin")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_food,null)

        val edt_food_name = itemView.findViewById<View>(R.id.edt_food_name) as EditText
        val edt_food_price = itemView.findViewById<View>(R.id.edt_food_price) as EditText
        val edt_food_description = itemView.findViewById<View>(R.id.edt_food_description) as EditText

        img_food = itemView.findViewById<View>(R.id.img_food_image) as ImageView

        //set data
        edt_food_name.setText(StringBuilder("").append(foodModel.name))
        edt_food_price.setText(StringBuilder("").append(foodModel.price))
        edt_food_description.setText(StringBuilder("").append(foodModel.description))

        Glide.with(context!!).load(foodModel.image).into(img_food!!)

        // set event
        img_food!!.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent,"Lựa chọn ảnh"),PICK_IMAGE_REQUEST)

        }
        builder.setNegativeButton("Hủy",{dialogInterface, _ -> dialogInterface.dismiss()  })
        builder.setPositiveButton("Cập nhật" ) {dialogInterface, i ->
            val updateFood = foodModel
            updateFood.name = edt_food_name.text.toString()
                    updateFood.price = if(TextUtils.isEmpty(edt_food_price.text))
                0
            else
                  edt_food_price.text.toString().toLong()
                //edt_food_price.text.toString().toLong()
            updateFood.description = edt_food_description.text.toString()
            if(imageUri != null){
                dialog.setMessage("Uploading...")
                dialog.show()

                val imageName = UUID.randomUUID().toString()
                val imageFolder = storageReference.child("images/$imageName")
                imageFolder.putFile(imageUri!!)
                    .addOnFailureListener { e ->
                        dialog.dismiss()
                        Toast.makeText(context, ""+e.message, Toast.LENGTH_SHORT).show()
                    }
                    .addOnProgressListener { taskSnapshot ->
                        val progress = 100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount
                        dialog.setMessage("Uploaded $progress%")
                    }
                    .addOnSuccessListener { taskSnapshot ->
                        dialogInterface.dismiss()
                        imageFolder.downloadUrl.addOnSuccessListener { uri ->
                            dialog.dismiss()
                            updateFood.image = uri.toString()
                            Common.categorySelected!!.foods!![pos] = updateFood
                            updateFood(Common.categorySelected!!.foods!!,false)
                        }
                    }
            }
            else{
                Common.categorySelected!!.foods!![pos] = updateFood
                updateFood(Common.categorySelected!!.foods!!,false)
            }
        }

        builder.setView(itemView)
        val updateDialog = builder.create()
        updateDialog.show()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.data != null)
            {
                imageUri = data.data
                img_food!!.setImageURI(imageUri)
            }
        }
    }

    private fun updateFood(foods: MutableList<FoodModel>?,isDelete:Boolean) {
        val updateData = HashMap<String,Any>()
        updateData["foods"] = foods!!


        FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener { e -> Toast.makeText(context!!,""+ e.message,Toast.LENGTH_LONG).show() }
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    foodListViewModel.getMutableFoodModelListData()
                 EventBus.getDefault().postSticky(ToastEvent(!isDelete,true))

                }
            }
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }
}