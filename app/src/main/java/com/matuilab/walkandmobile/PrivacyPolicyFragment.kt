package com.matuilab.walkandmobile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class PrivacyPolicyFragment : Fragment() {
//    private var myInterface: DrawerInterface? = null

//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        myInterface = try {
//            activity as DrawerInterface
//        } catch (e: ClassCastException) {
//            throw ClassCastException("$activity must implement MyInterface")
//        }
//    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        myInterface!!.lockDrawer();
        val view = inflater.inflate(R.layout.fragment_privacy_policy, container, false)
        return view
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        myInterface!!.unlockDrawer()
//    }
}