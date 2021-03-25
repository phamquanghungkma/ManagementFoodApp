package com.tofukma.serverorderapp.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tofukma.serverorderapp.R
import com.tofukma.serverorderapp.eventbus.LoadOrderEvent
import kotlinx.android.synthetic.main.fragment_order_filter.*
import org.greenrobot.eventbus.EventBus

class BottomSheetOrderFragment : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.fragment_order_filter,container,false)
        return  itemView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        place_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(0));
            dismiss()
        }
        shipping_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(1));
            dismiss()
        }
        shipped_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(2));
            dismiss()
        }
        clear_filter.setOnClickListener {
            EventBus.getDefault().postSticky(LoadOrderEvent(-1));
            dismiss()
        }
    }

    companion object{
        val instances:BottomSheetOrderFragment? = null
            get() = field ?: BottomSheetOrderFragment()
    }
}