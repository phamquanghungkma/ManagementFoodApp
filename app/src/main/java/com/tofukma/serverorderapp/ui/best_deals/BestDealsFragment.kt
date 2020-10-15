package com.tofukma.serverorderapp.ui.best_deals

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.adapter.MyBestDealsAdapter
import com.tofukma.serverorderapp.adapter.MyCategoriesAdapter
import com.tofukma.serverorderapp.model.BestDealsModel
import com.tofukma.serverorderapp.model.CategoryModel
import com.tofukma.serverorderapp.ui.category.CategoryViewModel
import dmax.dialog.SpotsDialog

class BestDealsFragment : Fragment() {

    private lateinit var viewModel: BestDealsViewModel

    private lateinit var dialog: AlertDialog
    private  lateinit var layoutAnimationController: LayoutAnimationController
    private var adapter: MyBestDealsAdapter?= null
    private var recycler_best_deals: RecyclerView?= null

    internal var bestDealsModels: List<BestDealsModel> = ArrayList<BestDealsModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel =
            ViewModelProviders.of(this).get(BestDealsViewModel::class.java)
        val root = inflater.inflate(R.layout.best_deals_fragment, container, false)
        initViews(root)

        viewModel.getMessageError().observe(this, Observer {

            Toast.makeText(context,it, Toast.LENGTH_SHORT).show()
        })
        viewModel.getBestDealsList().observe(this, Observer {
            dialog.dismiss()
            bestDealsModels = it
            adapter = MyBestDealsAdapter(context!!,bestDealsModels)
            recycler_best_deals!!.adapter = adapter
            recycler_best_deals!!.layoutAnimation = layoutAnimationController
        })
        return root
    }

    private fun initViews(root: View?) {

        dialog = SpotsDialog.Builder().setContext(context).setCancelable(false).build()
        dialog.show()
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(context,R.anim.layout_item_from_left)
        recycler_best_deals = root!!.findViewById(R.id.recycler_best_deal) as RecyclerView
        recycler_best_deals!!.setHasFixedSize(true)
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(1,RecyclerView.VERTICAL)
        val layoutManager = LinearLayoutManager(context)

//        recycler_menu!!.layoutManager = layoutManager
        recycler_best_deals!!.layoutManager = staggeredGridLayoutManager
        recycler_best_deals!!.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
    }


}