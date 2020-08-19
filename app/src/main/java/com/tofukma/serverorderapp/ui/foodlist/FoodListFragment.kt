package com.tofukma.serverorderapp.ui.foodlist

import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.StringBuilderPrinter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.tofukma.serverorderapp.adapter.MyFoodListAdapter
import com.tofukma.serverorderapp.callback.IMyButtonCallback
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.common.MySwipeHelper
import com.tofukma.serverorderapp.model.FoodModel
import dmax.dialog.SpotsDialog
import java.util.ArrayList

class FoodListFragment : Fragment(){

    private lateinit var foodListViewModel: FoodListViewModel
    var recycler_food_list : RecyclerView?= null
    var layoutAnimationController: LayoutAnimationController?= null
    var foodModelList :List<FoodModel> =  ArrayList<FoodModel>()


    private var img_food:ImageView ?= null
    private lateinit var storage:FirebaseStorage
    private lateinit var storageReference : StorageReference
    private lateinit var dialog: android.app.AlertDialog





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

        dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        recycler_food_list = root!!.findViewById(R.id.recycler_food_list) as RecyclerView
        recycler_food_list!!.setHasFixedSize(true)
        recycler_food_list!!.layoutManager = LinearLayoutManager(context)

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)


        (activity as AppCompatActivity).supportActionBar!!.title = Common.categorySelected!!.name

        val swipe = object: MySwipeHelper(context!!, recycler_food_list!!, 300)
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
                                        Common.categorySelected!!.foods!!.removeAt(pos)
                                        updateFood(Common.categorySelected!!.foods)

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

                            showUpdateDialog(pos)


                        }
                    }))
            }
        }
    }

    private fun showUpdateDialog(pos: Int) {

        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Cập nhật")
        builder.setMessage("Sửa thông tin")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_food,null)

        val edt_food_name = itemView.findViewById<View>(R.id.edt_food_name) as EditText
        val edt_food_price = itemView.findViewById<View>(R.id.edt_food_price) as EditText
        val edt_food_description = itemView.findViewById<View>(R.id.edt_food_description) as EditText

        img_food = itemView.findViewById<View>(R.id.img_food_image) as ImageView

        //set data
        edt_food_name.setText(StringBuilder("").append(Common.categorySelected.foods!![pos].name))
        edt_food_price.setText(StringBuilder("").append(Common.categorySelected.foods!![pos].price))
        edt_food_description.setText(StringBuilder("").append(Common.categorySelected.foods!![pos].description))

        Glide.with(context!!).load(Common.categorySelected.foods!![pos].image).into(img_food!!)

        // set event
        img_food!!.setOnClickListener{
            


        }



    }

    private fun updateFood(foods: MutableList<FoodModel>?) {
        val updateData = HashMap<String,Any>()
        updateData["foods"] = foods!!


        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .updateChildren(updateData)
            .addOnFailureListener { e -> Toast.makeText(context!!,""+ e.message,Toast.LENGTH_LONG).show() }
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    foodListViewModel.getMutableFoodModelListData()
                    Toast.makeText(context!!,"Xoá thành công ",Toast.LENGTH_LONG).show()

                }
            }
    }
}