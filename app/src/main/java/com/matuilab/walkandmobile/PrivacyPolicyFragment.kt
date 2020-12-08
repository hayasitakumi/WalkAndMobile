package com.matuilab.walkandmobile

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_privacy_policy.*

class PrivacyPolicyFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
//        myInterface!!.lockDrawer();
        val view = inflater.inflate(R.layout.fragment_privacy_policy, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        privacy_policy_pdfview.fromAsset("privacypolicy.pdf")
                .defaultPage(1)
                .showMinimap(false)
                .enableSwipe(true)
                .load()
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
//        myInterface!!.unlockDrawer()
//    }
}