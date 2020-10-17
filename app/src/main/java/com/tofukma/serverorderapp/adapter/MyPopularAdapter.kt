package com.tofukma.serverorderapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.callback.IRecyclerItemClickListener
import com.tofukma.serverorderapp.common.Common
import com.tofukma.serverorderapp.eventbus.CategoryClick
import com.tofukma.serverorderapp.model.BestDealsModel
import com.tofukma.serverorderapp.model.MostPopularModel
import org.greenrobot.eventbus.EventBus

class MyPopularAdapter (internal var context: Context,
                        internal var mostPopularLists: List<MostPopularModel>)
                    : RecyclerView.Adapter<MyPopularAdapter.MyViewHolder>(){

    inner class MyViewHolder (itemView : View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        var category_name: TextView?= null
        var category_image : ImageView?= null

        internal var listener: IRecyclerItemClickListener?= null

        fun setListener(listener: IRecyclerItemClickListener){
            this.listener = listener
        }

        init {
            category_name = itemView.findViewById(R.id.category_name) as TextView
            category_image = itemView.findViewById(R.id.category_image) as ImageView
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            listener!!.onItemClick(view!!,adapterPosition)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MyPopularAdapter.MyViewHolder {
        return MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_category_item_v2,parent,false))

    }

    override fun getItemCount(): Int {
        return mostPopularLists.size

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(mostPopularLists.get(position).image).into(holder.category_image!!)
        holder.category_name!!.setText(mostPopularLists.get(position).name)

        //Event
        holder.setListener(object: IRecyclerItemClickListener {
            override fun onItemClick(view: View, post: Int) {

            }

        })

    }


}