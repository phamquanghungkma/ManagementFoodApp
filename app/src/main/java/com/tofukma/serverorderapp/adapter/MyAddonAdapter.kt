package com.tofukma.serverorderapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.callback.IRecyclerItemClickListener
import com.tofukma.serverorderapp.eventbus.SelectAddonModel
import com.tofukma.serverorderapp.eventbus.SelectSizeModel
import com.tofukma.serverorderapp.eventbus.UpdateAddonModel
import com.tofukma.serverorderapp.eventbus.UpdateSizeModel
import com.tofukma.serverorderapp.model.AddonModel
import com.tofukma.serverorderapp.model.SizeModel
import org.greenrobot.eventbus.EventBus
import java.nio.channels.SelectableChannel

class MyAddonAdapter(var context: Context,var addonModelList:MutableList<AddonModel>):RecyclerView.Adapter<MyAddonAdapter.MyViewHolder>() {

    var editPos: Int
    var updateAddonModel: UpdateAddonModel

    init {
        editPos = -1
        updateAddonModel = UpdateAddonModel()
    }
    inner class MyViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        var txt_name:TextView?=null
        var txt_price:TextView?=null
        var img_delete:ImageView?=null
        internal var listener:IRecyclerItemClickListener?=null
        init {
            txt_name = itemView.findViewById(R.id.txt_name) as TextView
            txt_price = itemView.findViewById(R.id.txt_price) as TextView
            img_delete = itemView.findViewById(R.id.img_delete) as ImageView

            itemView.setOnClickListener{view -> listener!!.onItemClick(view,adapterPosition)}
        }
        fun setListener(listener: IRecyclerItemClickListener?){
            this.listener = listener
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_addon_size_item, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return addonModelList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.txt_name!!.text = addonModelList[position].name
        holder.txt_price!!.text = addonModelList[position].price.toString()

        holder.img_delete!!.setOnClickListener {
            addonModelList.removeAt(position)
            notifyItemRemoved(position)
            updateAddonModel.addonModelList = addonModelList
            EventBus.getDefault().postSticky(updateAddonModel)
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                editPos = position
                EventBus.getDefault().postSticky(SelectAddonModel(addonModelList[pos]))
            }
        })
//        holder.setListener(IRecyclerItemClickListener{view:View?,pos:Int ->
//
//        })
    }

    fun addNewAddon(addonModel: AddonModel) {
        addonModelList.add(addonModel)
        notifyItemInserted(addonModelList.size - 1)
        updateAddonModel.addonModelList = addonModelList
        EventBus.getDefault().postSticky(updateAddonModel)
    }

    fun editAddon(addonModel: AddonModel) {
        addonModelList.set(editPos, addonModel)
        notifyItemChanged(editPos)
        updateAddonModel.addonModelList = addonModelList
        EventBus.getDefault().postSticky(updateAddonModel)
    }


}