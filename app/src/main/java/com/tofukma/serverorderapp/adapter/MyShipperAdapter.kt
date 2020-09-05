package com.tofukma.serverorderapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.callback.IRecyclerItemClickListener
import com.tofukma.serverorderapp.eventbus.UpdateActiveEvent
import com.tofukma.serverorderapp.model.ShipperMOdel
import org.greenrobot.eventbus.EventBus

public class MyShipperAdapter (internal var context: Context, internal var shipperList: List<ShipperMOdel>)
    :RecyclerView.Adapter<MyShipperAdapter.MyViewHolder>(){


    inner class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView) {

        var txt_name: TextView?= null
        var txt_phone: TextView?= null
        var btn_enable :SwitchCompat ?= null




        init {
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_phone = itemView.findViewById(R.id.txt_phone) as TextView
            btn_enable = itemView.findViewById(R.id.btn_enable) as SwitchCompat

        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //Khi xử lý sự kiện người dùng click vào một item trong RecyclerView chúng ta nên xử lý bên
        // trong onCreateViewHolder(…)
        val itemView = LayoutInflater.from(context).inflate(R.layout.layout_shipper2,parent,false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return shipperList.size
    }

    //  Phương thức này dùng để gắn data và view.
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.setText(shipperList[position].name)
        holder.txt_phone!!.setText(shipperList[position].phone)
        holder.btn_enable!!.isChecked = shipperList[position].isActive

        holder.btn_enable!!.setOnCheckedChangeListener { compoundButton, b ->
            EventBus.getDefault().postSticky(UpdateActiveEvent(shipperList[position],b))

        }

    }
}
