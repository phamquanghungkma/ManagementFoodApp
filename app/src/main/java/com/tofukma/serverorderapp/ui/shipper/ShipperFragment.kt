package com.tofukma.serverorderapp.ui.shipper

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders.of
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.adapter.MyCategoriesAdapter
import com.tofukma.serverorderapp.adapter.MyShipperAdapter
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.eventbus.ChangeMenuClick
import com.tofukma.serverorderapp.eventbus.UpdateActiveEvent
import com.tofukma.serverorderapp.model.CategoryModel
import com.tofukma.serverorderapp.model.ShipperMOdel
import dmax.dialog.SpotsDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.stream.Stream.of

class ShipperFragment : Fragment() {

    private lateinit var dialog: AlertDialog
    private  lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyShipperAdapter?= null
    private var recycler_shipper: RecyclerView?= null
    internal var shipperModels: List<ShipperMOdel> = ArrayList<ShipperMOdel>()

    companion object {
        fun newInstance() = ShipperFragment()
    }

    private lateinit var viewModel: ShipperViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView =  inflater.inflate(R.layout.fragment_shipper, container, false)
        viewModel = ViewModelProviders.of(this).get(ShipperViewModel::class.java)

        initViews(itemView)
        viewModel.getMessageError().observe(this, Observer {

            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        viewModel.getShipperList().observe(this, Observer {
            dialog.dismiss()
            shipperModels = it
            adapter = MyShipperAdapter(context!!,shipperModels)
            recycler_shipper!!.adapter = adapter
            recycler_shipper!!.layoutAnimation = layoutAnimationController
        })

        return itemView
    }

    private fun initViews(root: View) {
        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

        recycler_shipper = root.findViewById(R.id.recycler_shipper) as? RecyclerView
//        recycler_shipper!!.setHasFixedSize(true)
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(1,RecyclerView.VERTICAL)
        val layoutManager = LinearLayoutManager(context)

//        recycler_menu!!.layoutManager = layoutManager
        recycler_shipper!!.layoutManager = staggeredGridLayoutManager
        recycler_shipper!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        if(EventBus.getDefault().hasSubscriberForEvent(UpdateActiveEvent::class.java))
            EventBus.getDefault().removeStickyEvent(UpdateActiveEvent::class.java)
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }


    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))

        super.onDestroy()
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    fun onUpdateActiveEvent(updateActiveEvent: UpdateActiveEvent){
        val updateData = HashMap<String,Any>()
        updateData.put("active",updateActiveEvent.active)
        FirebaseDatabase.getInstance()
            .getReference(Common.RESTAURANT_REF)
            .child(Common.currentServerUser!!.restaurant!!)
            .child(Common.SHIPPER_REF)
            .child(updateActiveEvent.shipperModel!!.key!!)
            .updateChildren(updateData)
            .addOnFailureListener { e -> Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show() }
            .addOnSuccessListener { aVoid  ->
                if(updateActiveEvent.active){
                    Toast.makeText(context,"Shipper đã được active ",Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(context,"Shipper đẵ bị khoá ",Toast.LENGTH_SHORT).show()

                }
            }
    }

}