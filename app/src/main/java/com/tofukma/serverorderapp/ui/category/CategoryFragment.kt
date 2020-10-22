package com.tofukma.serverorderapp.ui.category

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.adapter.MyCategoriesAdapter
import com.tofukma.serverorderapp.callback.IMyButtonCallback
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.common.MySwipeHelper
import com.tofukma.serverorderapp.eventbus.ToastEvent
import com.tofukma.serverorderapp.model.CategoryModel
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.layout_update_category.*
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CategoryFragment : Fragment() {

    private val PICK_IMAGE_REQUEST: Int = 1234
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var dialog: AlertDialog
    private  lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyCategoriesAdapter?= null
    private var recycler_menu: RecyclerView?= null

    internal var categoryModels: List<CategoryModel> = ArrayList<CategoryModel>()
    internal lateinit var storage:FirebaseStorage
    internal lateinit var storageReference:StorageReference
    private var imageUri: Uri?=null
    internal  lateinit var  img_category:ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        categoryViewModel =
            ViewModelProviders.of(this).get(CategoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_category, container, false)
        initViews(root)

        categoryViewModel.getMessageError().observe(this, Observer {

            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        categoryViewModel.getCategoryList().observe(this, Observer {
            dialog.dismiss()
            categoryModels = it
            adapter = MyCategoriesAdapter(context!!,categoryModels)
            recycler_menu!!.adapter = adapter
            recycler_menu!!.layoutAnimation = layoutAnimationController
        })
        return root
    }
    private fun initViews(root:View) {

        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
        recycler_menu = root.findViewById(R.id.recycler_menu) as RecyclerView
        recycler_menu!!.setHasFixedSize(true)
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(1,RecyclerView.VERTICAL)
        val layoutManager = LinearLayoutManager(context)

//        recycler_menu!!.layoutManager = layoutManager
        recycler_menu!!.layoutManager = staggeredGridLayoutManager
        recycler_menu!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))

        val swipe = object: MySwipeHelper(context!!, recycler_menu!!, 200)
        {
            override fun instantianteMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Delete",
                    30,
                    0,
                    Color.parseColor("#333639"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.categorySelected = categoryModels[pos];
                            showDeleteDialog();
                        }
                    }))

                buffer.add(MyButton(context!!,
                    "Update",
                    30,
                    0,
                    Color.parseColor("#560027"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Common.categorySelected = categoryModels[pos];
                            showUpdateDialog();
                        }
                    }))
            }
        }

    }

    private fun showDeleteDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Delete Category")
        builder.setMessage("Do you really want to delete this category")

        builder.setNegativeButton("CANCEL"){ dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton("Delete"){dialogInterface, _ ->

            deleteDialog()
        }

        val deleteDialog = builder.create()
        deleteDialog.show()

    }

    private fun deleteDialog() {
        FirebaseDatabase.getInstance()
            .getReference(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .removeValue()
            .addOnFailureListener { e -> Toast.makeText(context, ""+e.message,Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { task ->
                categoryViewModel!!.loadCategory()
                EventBus.getDefault().postSticky(ToastEvent(false,false))
            }
    }

    private fun showUpdateDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(context!!)
        builder.setTitle("Update Category")
        builder.setMessage("Please fill information")

        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_update_category, null)
        val edt_category_name = itemView.findViewById<View>(R.id.edit_category_name) as EditText
        img_category = itemView.findViewById<View>(R.id.img_category) as ImageView

        // Set data
        edt_category_name.setText(Common.categorySelected!!.name)
        Glide.with(context!!).load(Common.categorySelected!!.image).into(img_category)
        
        // Set event
        img_category.setOnClickListener { view ->
            val  intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"),PICK_IMAGE_REQUEST)
        }

        builder.setNegativeButton("CANCEL"){ dialogInterface, _ -> dialogInterface.dismiss() }
        builder.setPositiveButton("UPDATE"){dialogInterface, _ ->

            val updateDate = HashMap<String, Any>()
            updateDate["name"] = edt_category_name.text.toString()
            if(imageUri != null)
            {
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
                            updateDate["image"] = uri.toString()
                            updateCategory(updateDate)
                        }
                    }
            }
            else
            {
                updateCategory(updateDate)
            }
        }

        builder.setView(itemView)
        val updateDialog = builder.create()
        updateDialog.show()
    }
    private fun updateCategory(updateDate: java.util.HashMap<String, Any>) {
        FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.CATEGORY_REF)
            .child(Common.categorySelected!!.menu_id!!)
            .updateChildren(updateDate)
            .addOnFailureListener { e -> Toast.makeText(context, ""+e.message,Toast.LENGTH_SHORT).show() }
            .addOnCompleteListener { task ->
                categoryViewModel!!.loadCategory()
               EventBus.getDefault().postSticky(ToastEvent(true,false))
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if(data != null && data.data != null)
            {
                imageUri = data.data
                img_category.setImageURI(imageUri)
            }
        }
    }
    private fun deleteCategory(){
        FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.CATEGORY_REF)
            .removeValue()
            .addOnFailureListener{e->Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()}
            .addOnCompleteListener{task ->
                categoryViewModel!!.loadCategory()
                EventBus.getDefault().postSticky(ToastEvent(false,false))
            }
    }
}