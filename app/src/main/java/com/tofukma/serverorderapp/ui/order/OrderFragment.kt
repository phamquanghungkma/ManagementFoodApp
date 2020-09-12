package com.tofukma.serverorderapp.ui.order

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.adapter.MyOrderAdapter
import com.tofukma.serverorderapp.adapter.MyShipperSelectedAdapter
import com.tofukma.serverorderapp.callback.IMyButtonCallback
import com.tofukma.serverorderapp.callback.IShipperLoadCallbackListener
import com.tofukma.serverorderapp.common.BottomSheetOrderFragment
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.common.MySwipeHelper
import com.tofukma.serverorderapp.eventbus.ChangeMenuClick
import com.tofukma.serverorderapp.eventbus.LoadOrderEvent
import com.tofukma.serverorderapp.model.*
import com.tofukma.serverorderapp.remote.IFCMService
import com.tofukma.serverorderapp.remote.RetrofitFCMClient
import com.tofukma.serverorderapp.services.MyFCMServices
import com.tofukma.shippingapp.model.TokenModel
import dmax.dialog.SpotsDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_order.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.StringBuilder

class OrderFragment : Fragment(), IShipperLoadCallbackListener {
    private val compositeDiposable = CompositeDisposable()
    lateinit var ifcmService: IFCMService

    lateinit var recycler_order:RecyclerView
    lateinit var layoutAnimationController:LayoutAnimationController
    lateinit var orderViewModel: OrderViewModel
    private var adapter : MyOrderAdapter?= null
    var myShipperSelectedAdapter: MyShipperSelectedAdapter? = null
   lateinit var shipperLoadCallbackListener:IShipperLoadCallbackListener
    var recycle_shipper:RecyclerView?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_order, container, false)
        initView(root)

        orderViewModel = ViewModelProviders.of(this).get(OrderViewModel::class.java)

        orderViewModel!!.messsageError.observe(this, Observer { s->
            Toast.makeText(context,s,Toast.LENGTH_SHORT).show()
        })
        orderViewModel!!.getOrderModelList().observe(this, Observer { orderList ->
            if(orderList != null){
                adapter = MyOrderAdapter(context!!,orderList.toMutableList())
                recycler_order.adapter = adapter
                recycler_order.layoutAnimation = layoutAnimationController

                updateTextCounter()
            }
        })
        return root
    }
    private fun initView(root:View){

        shipperLoadCallbackListener = this
        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService::class.java)

        setHasOptionsMenu(true)

        recycler_order = root.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)

        val displayMetrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels

        val swipe = object: MySwipeHelper(context!!, recycler_order!!, width/6)
        {
            override fun instantianteMyButton(
                viewHolder: RecyclerView.ViewHolder,
                buffer: MutableList<MyButton>
            ) {
                buffer.add(MyButton(context!!,
                    "Trạng thái",
                    30,
                    0,
                    Color.parseColor("#9b0000"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                        }
                    })
                )
                buffer.add(MyButton(context!!,
                    "Gọi",
                    30,
                    0,
                    Color.parseColor("#560027"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            Dexter.withActivity(activity)
                                .withPermission(android.Manifest.permission.CALL_PHONE)
                                .withListener(object : PermissionListener{
                                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                                        val orderModel = adapter!!.getItemAtPosition(pos)
                                        val intent = Intent()
                                        intent.setAction(Intent.ACTION_DIAL)
//                                        intent.setData(
//                                            Uri.parse(StringBuilder("tel : ")
//                                            .append(orderModel.userPhone).toString()))
                                        intent.setData(Uri.fromParts("tel", orderModel.userPhone.toString(), null))
                                        startActivity(intent)
                                    }
                                    override fun onPermissionRationaleShouldBeShown(
                                        permission: PermissionRequest?,
                                        token: PermissionToken?
                                    ) {
                                    }
                                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                                        Toast.makeText(context, "Ban phai chap nhan" + response!!.permissionName,
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }).check()
                        }
                    }))

                buffer.add(MyButton(context!!,
                    "Di chuyển",
                    30,
                    0,
                    Color.parseColor("#12005e"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {
                            val orderModel = adapter!!.getItemAtPosition(pos)

                            val builder = AlertDialog.Builder(context!!)
                                .setTitle("Xóa")
                                .setMessage("Bạn thực sự muốn xóa đơn hàng này?")
                                .setNegativeButton("Hủy"){dialogInterface, i -> dialogInterface.dismiss()}
                                .setPositiveButton("Xóa"){dialogInterface, i ->
                                    FirebaseDatabase.getInstance()
                                        .getReference(Common.ORDER_REF)
                                        .child(orderModel!!.key!!)
                                        .removeValue()
                                        .addOnFailureListener{
                                            Toast.makeText(context!!,""+it.message,Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnSuccessListener {
                                            adapter!!.removeItem(pos)
                                            adapter!!.notifyItemRemoved(pos)
                                            updateTextCounter()
                                            dialogInterface.dismiss()
                                            Toast.makeText(context!!,"Đơn hàng đã bị xóa",Toast.LENGTH_SHORT).show()
                                        }
                                }

                            val dialog = builder.create()
                            dialog.show()

                            val btn_negative = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                            btn_negative.setTextColor(Color.LTGRAY)
                            val btn_positve = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                            btn_positve.setTextColor(Color.RED)
                        }

                    }))

                buffer.add(MyButton(context!!,
                    "Sửa",
                    30,
                    0,
                    Color.parseColor("#333639"),
                    object: IMyButtonCallback {
                        override fun onClick(pos: Int) {

                            showEditDialog(adapter!!.getItemAtPosition(pos),pos)
                        }
                    }))
            }
        }

    }

    private fun showEditDialog(orderModel: OrderModel, pos: Int) {
        var layout_dialog:View?=null
        var builder:AlertDialog.Builder?=null

        var rdi_shipping:RadioButton?=null
        var rdi_shipped:RadioButton?=null
        var rdi_cancelled:RadioButton?=null
        var rdi_delete:RadioButton?=null
        var rdi_restore_placed:RadioButton?=null


        if(orderModel.orderStatus == -1)
        {
            layout_dialog = LayoutInflater.from(context!!)
                .inflate(R.layout.layout_dialog_cancelled, null)

            builder = AlertDialog.Builder(context!!)
                .setView(layout_dialog)

            rdi_delete = layout_dialog.findViewById<View>(R.id.rdi_delete) as RadioButton
            rdi_restore_placed = layout_dialog.findViewById<View>(R.id.rdi_restore_placed) as RadioButton
        }
        else if(orderModel.orderStatus == 0)
        {
            layout_dialog = LayoutInflater.from(context!!)
                .inflate(R.layout.layout_dialog_shipping, null)
            recycle_shipper = layout_dialog.findViewById(R.id.recycler_shipper) as RecyclerView//add when shippung order status
            builder = AlertDialog.Builder(context!!,
                android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
                .setView(layout_dialog)

            rdi_shipping = layout_dialog.findViewById<View>(R.id.rdi_shipping) as RadioButton
            rdi_cancelled = layout_dialog.findViewById<View>(R.id.rdi_cancelled) as RadioButton
        }
        else
        {
            layout_dialog = LayoutInflater.from(context!!)
                .inflate(R.layout.layout_dialog_shipped, null)
            builder = AlertDialog.Builder(context!!)
                .setView(layout_dialog)

            rdi_shipped = layout_dialog.findViewById<View>(R.id.rdi_shipped) as RadioButton
            rdi_cancelled = layout_dialog.findViewById<View>(R.id.rdi_cancelled) as RadioButton

        }

        //View
        val btn_ok = layout_dialog.findViewById<View>(R.id.btn_oke) as Button
        val btn_cancel = layout_dialog.findViewById<View>(R.id.btn_cancel) as Button

        val txt_status = layout_dialog.findViewById<View>(R.id.txt_status) as TextView

        //Set data
        txt_status.setText(StringBuilder("Order Status(")
            .append(Common.convertStatusToString(orderModel.orderStatus))
            .append(")"))

        //Create Dialog
        val dialog = builder.create()
        if (orderModel.orderStatus == 0) //shipping
            loadShipperList(pos,orderModel,dialog,btn_ok,btn_cancel,
                rdi_shipping,rdi_shipped,rdi_cancelled,
                rdi_delete,rdi_restore_placed)
        else
            showDialog(pos,orderModel,dialog,btn_ok,btn_cancel,
                rdi_shipping,rdi_shipped,rdi_cancelled,
                rdi_delete,rdi_restore_placed)
       
    }

    private fun loadShipperList(
        pos: Int,
        orderModel: OrderModel,
        dialog: AlertDialog,
        btnOk: Button,
        btnCancel: Button,
        rdiShipping: RadioButton?,
        rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?,
        rdiDelete: RadioButton?,
        rdiRestorePlaced: RadioButton?
    ) {
    val tempList:MutableList<ShipperMOdel> = ArrayList()
        val shipperRef = FirebaseDatabase.getInstance().getReference(Common.SHIPPER_REF)
        val shipperActive = shipperRef.orderByChild("active").equalTo(true)
        shipperActive.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            shipperLoadCallbackListener.onShipperLoadFailed(p0!!.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (shipperSnapshot in p0.children){
                    val shipperMOdel = shipperSnapshot.getValue(ShipperMOdel::class.java)!!
                    shipperMOdel.key = shipperSnapshot.key
                    tempList.add(shipperMOdel)
                }
                shipperLoadCallbackListener.onShipperLoadSuccess(pos,
                    orderModel,
                    tempList,
                    dialog,
                    btnOk,
                    btnCancel,
                    rdiShipping,rdiShipped,rdiCancelled,
                    rdiDelete,rdiRestorePlaced)
            }
        })
    }

    private fun showDialog(
        pos: Int,
        orderModel: OrderModel,
        dialog: AlertDialog,
        btnOk: Button,
        btnCancel: Button,
        rdiShipping: RadioButton?,
        rdiShipped: RadioButton?,
        rdiCancelled: RadioButton?,
        rdiDelete: RadioButton?,
        rdiRestorePlaced: RadioButton?
    ) {
        dialog.show()
        //Custom dialog
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener {

            if(rdiCancelled != null && rdiCancelled.isChecked)
            {
                updateOrder(pos, orderModel, -1)
                dialog.dismiss()
            }
            else if(rdiShipping != null && rdiShipping.isChecked)
            {
                var shipperMOdel:ShipperMOdel?=null
                if(myShipperSelectedAdapter !=null){
                    shipperMOdel = myShipperSelectedAdapter!!.selectedShipper
                    if(shipperMOdel != null){
                        createShippingOrder(pos,shipperMOdel,orderModel,dialog)
                    }else{
                        Toast.makeText(context,"Xin vui long chon giao hang",Toast.LENGTH_SHORT).show()
                    }
                }

            }
            else if(rdiShipped != null && rdiShipped.isChecked)
            {
                updateOrder(pos, orderModel, 2)
                dialog.dismiss()
            }
            else if(rdiRestorePlaced != null && rdiRestorePlaced.isChecked)
            {
                updateOrder(pos, orderModel, 0)
                dialog.dismiss()
            }
            else if(rdiDelete != null && rdiDelete.isChecked)
            {
                deleteOrder(pos, orderModel)
                dialog.dismiss()
            }
        }
    }

    private fun createShippingOrder(
        pos: Int,
        shipperModel: ShipperMOdel,
        orderModel: OrderModel,
        dialog: AlertDialog) {
        val shippingOrder = ShippingOrderModel()
        shippingOrder.shipperName = shipperModel.name
        shippingOrder.shipperPhone = shipperModel.phone
        shippingOrder.orderModel = orderModel
        shippingOrder.isStartTrip = false
        shippingOrder.currentLat = -1.0
        shippingOrder.currentLng = -1.0
    FirebaseDatabase.getInstance()
        .getReference(Common.SHIPPING_ORDER_REF)
        .push().setValue(shippingOrder)
        .addOnFailureListener{e:Exception ->
            dialog.dismiss()
            Toast.makeText(context,""+e.message,Toast.LENGTH_SHORT).show()

        }
        .addOnCompleteListener{
            task: Task<Void> ->
            if(task.isSuccessful){
                dialog.dismiss()

                FirebaseDatabase.getInstance().getReference(Common.TOKEN_REF).child(shipperModel.key!!)
                    .addListenerForSingleValueEvent(object: ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                            dialog.dismiss()
                            Toast.makeText(context,""+p0.message,Toast.LENGTH_SHORT).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if(p0.exists())
                            {
                                val tokenModel = p0.getValue(TokenModel::class.java)
                                Log.d("Token", tokenModel.toString())
                                val notiData = HashMap<String,String>()
                                notiData.put(Common.NOTI_TITLE,"Bạn có đơn hàng mới cần ship ")
                                notiData.put(Common.NOTI_CONTENT,StringBuilder("Đơn ship mới ")
                                    .append(orderModel.userPhone)
                                    .toString()
                                )
                                val sendData = FCMSendData(tokenModel!!.token!!,notiData)

                                compositeDiposable.add(ifcmService.sendNotification(sendData)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({
                                            fcmResponse ->
                                        if(fcmResponse.success == 1){
                                           updateOrder(pos,orderModel,1)
                                            dialog.dismiss()
                                        }
                                        else {
                                            Toast.makeText(context!!, "Không gửi được thông báo! Đơn hàng không được cập nhật",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                        {
                                                t ->
                                            dialog.dismiss()
                                            Toast.makeText(context!!,""+t.message,Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                )

                            }else {
                                dialog.dismiss()
                                Toast.makeText(context,"Không tìm thấy Token ",Toast.LENGTH_SHORT).show()
                            }
                        }

                    })





//                updateOrder(pos, orderModel, 1)
                Toast.makeText(context,"Đơn hàng được gửi cho giao hàng"+shipperModel.name,Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteOrder(pos: Int, orderModel: OrderModel) {
        if(!TextUtils.isEmpty(orderModel.key))
        {

            FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(orderModel.key!!)
                .removeValue()
                .addOnFailureListener { throawable -> Toast.makeText(context!!, ""+throawable.message,
                    Toast.LENGTH_SHORT).show() }
                .addOnSuccessListener {

                    adapter!!.removeItem(pos)
                    adapter!!.notifyItemRemoved(pos)
                    updateTextCounter()
                    Toast.makeText(context!!, "Order success",
                        Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateOrder(pos: Int, orderModel: OrderModel, status: Int) {
            if(!TextUtils.isEmpty(orderModel.key))
            {
                val update_data = HashMap<String, Any>()
                update_data.put("orderStatus",status)
                FirebaseDatabase.getInstance()
                    .getReference(Common.ORDER_REF)
                    .child(orderModel.key!!)
                    .updateChildren(update_data)
                    .addOnFailureListener { throawable -> Toast.makeText(context!!, ""+throawable.message,
                        Toast.LENGTH_SHORT).show() }
                    .addOnSuccessListener {

                        val dialog = SpotsDialog.Builder().setContext(context!!).setCancelable(false).build()
                        dialog.show()

                        // Load token
                        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REF).child(orderModel.userId!!)
                            .addListenerForSingleValueEvent(object: ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {
                                    dialog.dismiss()
                                    Toast.makeText(context,""+p0.message,Toast.LENGTH_SHORT).show()
                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    if(p0.exists())
                                    {
                                        val tokenModel = p0.getValue(TokenModel::class.java)
                                        Log.d("Token", tokenModel.toString())
                                        val notiData = HashMap<String,String>()
                                        notiData.put(Common.NOTI_TITLE,"Cập nhật đơn hàng ")
                                        notiData.put(Common.NOTI_CONTENT,StringBuilder("Đơn của bạn ")
                                            .append(orderModel.key)
                                            .append(" đã được cập nhật ")
                                            .append(Common.convertStatusToString(status)).toString()
                                        )
                                        val sendData = FCMSendData(tokenModel!!.token!!,notiData)

                                        compositeDiposable.add(ifcmService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({
                                                fcmResponse ->
                                                if(fcmResponse.success == 1){
                                                    Toast.makeText(context!!, "Cập nhật đơn hàng thành công ",
                                                        Toast.LENGTH_SHORT).show()
                                                    dialog.dismiss()
                                                }
                                                else {
                                                    Toast.makeText(context!!, "Không gửi được thông báo",
                                                        Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                                {
                                                    t ->
                                                    dialog.dismiss()
                                                    Toast.makeText(context!!,""+t.message,Toast.LENGTH_SHORT).show()
                                                }
                                                )
                                        )

                                    }else {
                                        dialog.dismiss()
                                        Toast.makeText(context,"Không tìm thấy Token ",Toast.LENGTH_SHORT).show()
                                    }
                                }

                            })

                        adapter!!.removeItem(pos)
                        adapter!!.notifyItemRemoved(pos)
                        updateTextCounter()

                    }
            }
    }

    private fun updateTextCounter() {
        txt_order_filter.setText(StringBuilder("Order (")
            .append(adapter!!.itemCount)
            .append(")"))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_filter){
            val bottomSheet = BottomSheetOrderFragment.instances
            bottomSheet!!.show(activity!!.supportFragmentManager,"OrderList")
            return true
        }
        else
            return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onStop() {
        if(EventBus.getDefault().hasSubscriberForEvent(LoadOrderEvent::class.java))
            EventBus.getDefault().removeStickyEvent(LoadOrderEvent::class.java)
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        compositeDiposable.clear()
        super.onStop()
    }

    override fun onDestroy() {
        EventBus.getDefault().postSticky(ChangeMenuClick(true))
        super.onDestroy()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun  onLoadOrder(event: LoadOrderEvent)
    {
        orderViewModel.loadOrder(event.status)
    }

        override fun onShipperLoadSuccess(shipperList: List<ShipperMOdel>) {
        //do anything
    }

    override fun onShipperLoadSuccess(
        pos: Int,
        oderModel: OrderModel?,
        shipperList: List<ShipperMOdel>?,
        dialog: android.app.AlertDialog,
        ok: Button?,
        cancel: Button?,
        rdi_shipping: RadioButton?,
        rdi_shipped: RadioButton?,
        rdi_cancelled: RadioButton?,
        rdi_delete: RadioButton?,
        rdi_restore_placed: RadioButton?
    ) {
        if(recycle_shipper !=null){
            recycle_shipper!!.setHasFixedSize(true)
            val layoutManager = LinearLayoutManager(context)
            recycle_shipper!!.layoutManager = layoutManager
            recycle_shipper!!.addItemDecoration(DividerItemDecoration(context,layoutManager.orientation))
            myShipperSelectedAdapter = MyShipperSelectedAdapter(context!!,shipperList!!)
            recycle_shipper!!.adapter = myShipperSelectedAdapter
        }
        showDialog(pos,oderModel!!,dialog!!,ok!!,cancel!!,rdi_shipping,rdi_shipped,rdi_cancelled,rdi_delete,rdi_restore_placed )
    }

    override fun onShipperLoadFailed(message: String) {
        Toast.makeText(context!!,message,Toast.LENGTH_SHORT).show()
    }
}