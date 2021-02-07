package com.matuilab.walkandmobile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ListView
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.matuilab.walkandmobile.http.HttpGetJson
import com.matuilab.walkandmobile.util.LanguageProcessor
import kotlinx.android.synthetic.main.dialog_download_in_advance.*
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import java.util.*

@RuntimePermissions
class PermissionCheckFragment : Fragment() {

    private var isCameraAllowed = false
    private val REQEST_CODE_MAGIC = 1212


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_permission_check, container, false)
    }

    override fun onStart() {
        super.onStart()
        showCameraWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    //以下CAMERA
    @NeedsPermission(Manifest.permission.CAMERA)
    fun showCamera() {
        isCameraAllowed = true
        startNextActivity()
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        isCameraAllowed = false
        startNextActivity()
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCaneraNeverAskAgain() {
        isCameraAllowed = false
        startNextActivity()
    }

    private fun startNextActivity() {

        if (isCameraAllowed) {

            /**Preferenceの設定*/
            val sharedPreferences: SharedPreferences =
                requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE)

            if (sharedPreferences.getBoolean("HIDDEN_DIALOG", false)) {
                findNavController().navigate(R.id.action_permission_check_to_camera)
            } else {
                val editor: SharedPreferences.Editor = sharedPreferences.edit()

                val languageProcessor =
                    LanguageProcessor(resources.getStringArray(R.array.code_language))
                var localLang = Locale.getDefault().language   //端末の設定言語を取得

                if (languageProcessor.indexOfLanguage(localLang) <= 0) {
                    // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
                    localLang = "en"
                }

                val saveAppDir = requireActivity().filesDir.absolutePath

                /**事前ダウンロードのダイアログ*/
                val getJson = HttpGetJson(requireActivity())
                getJson.setOnCallBack(object : HttpGetJson.CallBackTask() {
                    override fun CallBack(result: String?) {
                        super.CallBack(result)
                        // resultにはdoInBackgroundの返り値が入ります。
                        // ここからAsyncTask処理後の処理を記述します。
                        findNavController().navigate(R.id.action_permission_check_to_camera)
                    }
                })

                val listDownloadButtons: MutableList<String> = mutableListOf(
                    getString(R.string.download_in_advance_pbutton),
                    getString(R.string.download_in_advance_nebutton),
                    getString(R.string.download_in_advance_nbutton)
                )

                val arrayAdapterButtons = ArrayAdapter(
                    requireActivity(),
                    R.layout.dialog_download_in_advance_row,
                    R.id.dialog_download_in_advance_list_item,
                    listDownloadButtons
                )

                val content: View =
                    layoutInflater.inflate(R.layout.dialog_download_in_advance, null)

                //this is the ListView that lists your downloadButtons
                val downloadButtons: ListView =
                    content.findViewById(R.id.dialog_download_in_advance_list)
                downloadButtons.adapter = arrayAdapterButtons

                val builder = MaterialAlertDialogBuilder(requireActivity()).setOnCancelListener {
                    findNavController().navigate(R.id.action_permission_check_to_camera)
                }.setTitle(R.string.download_in_advance_title)
                    .setMessage(R.string.download_in_advance_message).setView(content)
                val dialog = builder.create()
                dialog.show()

                val checkBox =
                    content.findViewById<CheckBox>(R.id.dialog_download_in_advance_checkbox)
                checkBox.setOnClickListener {
                    if (checkBox.isChecked) {
                        editor.putBoolean("HIDDEN_DIALOG", true).apply()
                    } else {
                        editor.putBoolean("HIDDEN_DIALOG", false).apply()
                    }
                }

                downloadButtons.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, position, _ ->
                        //when you need to act on itemClick
                        when (position) {
                            0 -> {
                                dialog.dismiss()
                                getJson.execute(
                                    saveAppDir,
                                    languageProcessor.addressLanguage(localLang)
                                )
                            }
                            1 -> {
                                dialog.dismiss()
                                getJson.execute(
                                    saveAppDir,
                                    languageProcessor.addressLanguage(localLang),
                                    "FALSE"
                                )
                            }
                            else -> {
                                dialog.cancel()
                            }
                        }
                    }
            }

        } else {
            if (!isCameraAllowed)
                requireActivity().finish()
            MaterialAlertDialogBuilder(requireActivity())
                .setPositiveButton(android.R.string.cancel) { _, _ ->
                    requireActivity().finish()
                }
                .setNegativeButton(R.string.button_name_app_info) { _, _ ->
                    val uriString = "package:${requireActivity().packageName}"
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse(uriString))
                    startActivityForResult(intent, REQEST_CODE_MAGIC)
                }
                .setCancelable(false)
                .setMessage(R.string.alert_dialog_message_for_permission)
                .show()

        }
    }
}
