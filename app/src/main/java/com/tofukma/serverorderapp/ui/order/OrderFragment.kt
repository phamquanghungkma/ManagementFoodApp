package com.tofukma.serverorderapp.ui.order

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.adapter.MyOrderAdapter

class OrderFragment : Fragment()
{
    lateinit var recycler_order:RecyclerView
    lateinit var layoutAnimationController:LayoutAnimationController
    lateinit var orderViewModel: OrderViewModel
    private var adapter : MyOrderAdapter?= null
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
                adapter = MyOrderAdapter(context!!,orderList)
                recycler_order.adapter = adapter
                recycler_order.layoutAnimation = layoutAnimationController
            }
        })
        return root
    }
    private fun initView(root:View){

        setHasOptionsMenu(true)

        recycler_order = root.findViewById(R.id.recycler_order) as RecyclerView
        recycler_order.setHasFixedSize(true)
        recycler_order.layoutManager = LinearLayoutManager(context)

        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.order_list_menu, menu)
    }
}