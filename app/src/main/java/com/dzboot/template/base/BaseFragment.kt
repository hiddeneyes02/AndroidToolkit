package com.dzboot.template.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.dzboot.template.MainActivity


abstract class BaseFragment(private val TAG: String) : Fragment() {

    protected var activity: MainActivity? = null


    @LayoutRes
    abstract fun provideLayout(): Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(provideLayout(), container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun toString(): String {
        return TAG
    }

    protected fun getDrawable(@DrawableRes resId: Int) = ResourcesCompat.getDrawable(resources, resId, null)
}