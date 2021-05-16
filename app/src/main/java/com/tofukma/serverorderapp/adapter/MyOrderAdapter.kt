package com.tofukma.serverorderapp.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.callback.IRecyclerItemClickListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.model.CartItem
import com.tofukma.serverorderapp.model.FoodModel
import com.tofukma.serverorderapp.model.OrderModel
import java.text.SimpleDateFormat

class MyOrderAdapter (internal var context: Context,
                      internal var orderList: MutableList<OrderModel>) :
    RecyclerView.Adapter<MyOrderAdapter.MyViewHolder>() {
    lateinit var simpleDataFormat:SimpleDateFormat
    init {
        simpleDataFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
    }

    class MyViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var txt_time: TextView? = null
        var txt_order_number: TextView? = null
        var txt_order_status: TextView? = null
        var txt_num_item: TextView? = null
        var txt_name: TextView? = null

        var img_food_image:ImageView?=null

        internal  var iRecyclerItemClickListener:IRecyclerItemClickListener? = null

        fun setListener(iRecyclerItemClickListener: IRecyclerItemClickListener)
        {
            this.iRecyclerItemClickListener = iRecyclerItemClickListener
        }

        init {
            img_food_image =  itemView.findViewById(R.id.img_food_image) as ImageView

            txt_time =  itemView.findViewById(R.id.txt_time) as TextView
            txt_order_number =  itemView.findViewById(R.id.txt_order_number) as TextView
            txt_order_status =  itemView.findViewById(R.id.txt_order_status) as TextView
            txt_num_item =  itemView.findViewById(R.id.txt_num_item) as TextView
            txt_name =  itemView.findViewById(R.id.txt_name) as TextView

            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            iRecyclerItemClickListener!!.onItemClick(p0!!,adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context)
            .inflate(R.layout.layout_order_items,parent,false))
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(orderList[position].carItemList?.get(0)?.foodImage)
            .into(holder.img_food_image!!)
        holder.txt_order_number!!.setText(orderList[position].key)
        Common.setPanStringColor("Ngày đặt hàng",simpleDataFormat.format(orderList[position].createDate),
        holder.txt_time,Color.parseColor("#333639"))
        Common.setPanStringColor("Trạng thái đơn hàng",Common.convertStatusToString(orderList[position].orderStatus),
        holder.txt_order_status,Color.parseColor("#005758"))

        Common.setPanStringColor("Số đơn hàng ",if(orderList[position].carItemList == null) "0" else orderList[position].carItemList!!.size.toString(),
        holder.txt_num_item,Color.parseColor("#00574B"))

        Common.setPanStringColor("Ten ",orderList[position].userName,
            holder.txt_name,Color.parseColor("#006061"))

        holder.setListener(object :IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int) {
                showDialog(orderList[pos].carItemList)
            }

        })

    }

    private fun showDialog(carItemList: List<CartItem>?) {
        val layout_dialog = LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail, null)
        val builder = AlertDialog.Builder(context)
        builder.setView(layout_dialog)

        val btn_ok = layout_dialog.findViewById<View>(R.id.btn_ok) as Button
        val recycler_order_detail = layout_dialog.findViewById<View>(R.id.recycler_order_detail) as RecyclerView
        recycler_order_detail.setHasFixedSize(true)
        val layoutMager = LinearLayoutManager(context)
        recycler_order_detail.layoutManager = layoutMager
        recycler_order_detail.addItemDecoration(DividerItemDecoration(context, layoutMager.orientation))
        val adapter = MyOrderDetailAdapter(context,carItemList!!.toMutableList())
        recycler_order_detail.adapter = adapter

        //show dialog
        val dialog = builder.create()
        dialog.show()
        //Custom dialog
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.setGravity(Gravity.CENTER)

        btn_ok.setOnClickListener{ dialog.dismiss() }
    }

    fun getItemAtPosition(pos: Int): OrderModel {
        return  orderList[pos]
    }

    fun removeItem(pos: Int) {
        orderList.removeAt(pos)
    }
}