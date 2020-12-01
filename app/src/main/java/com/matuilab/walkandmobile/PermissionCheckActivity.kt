package com.matuilab.walkandmobile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.matuilab.walkandmobile.http.HttpGetJson
import com.matuilab.walkandmobile.http.HttpGetJson.CallBackTask
import com.matuilab.walkandmobile.util.LanguageProcessor
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import java.util.*


@RuntimePermissions
class PermissionCheckActivity : AppCompatActivity() {

    private var isCameraAllowed = false
    private val REQEST_CODE_MAGIC = 1212

    override fun onStart() {
        super.onStart()

        showCameraWithPermissionCheck()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
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
            val intent = Intent(this, MainActivity::class.java)


            val languageProcessor = LanguageProcessor(resources.getStringArray(R.array.code_language))
            var localLang = Locale.getDefault().language   //端末の設定言語を取得

            if (languageProcessor.indexOfLanguage(localLang) <= 0) {
                // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
                localLang = "en"
            }

            val saveAppDir = filesDir.absolutePath

            /**事前ダウンロードのダイアログ*/
            val getJson = HttpGetJson(this)
            getJson.setOnCallBack(object : CallBackTask() {
                override fun CallBack(result: String?) {
                    super.CallBack(result)
                    // resultにはdoInBackgroundの返り値が入ります。
                    // ここからAsyncTask処理後の処理を記述します。
                    startActivity(intent)
                    finish()
                }
            })

            val listDownloadButtons: MutableList<String> = mutableListOf(
                    getString(R.string.download_in_advance_pbutton),
                    getString(R.string.download_in_advance_nebutton),
                    getString(R.string.download_in_advance_nbutton))

            val arrayAdapterButtons = ArrayAdapter(this,
                    R.layout.dialog_download_in_advance_row, R.id.dialog_download_in_advance_list_item, listDownloadButtons)

            val content: View = layoutInflater.inflate(R.layout.dialog_download_in_advance, null)

            //this is the ListView that lists your downloadButtons
            val downloadButtons: ListView = content.findViewById(R.id.dialog_download_in_advance_list)
            downloadButtons.adapter = arrayAdapterButtons

            val builder = MaterialAlertDialogBuilder(this).setOnCancelListener {
                startActivity(intent)
                finish()
            }.setTitle(R.string.download_in_advance_title).setMessage(R.string.download_in_advance_message).setView(content)
            val dialog = builder.create()

            dialog.show()

            downloadButtons.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                //when you need to act on itemClick
                when (position) {
                    0 -> {
                        dialog.dismiss()
                        getJson.execute(saveAppDir, languageProcessor.addressLanguage(localLang))
                    }
                    1 -> {
                        dialog.dismiss()
                        getJson.execute(saveAppDir, languageProcessor.addressLanguage(localLang), "FALSE")
                    }
                    else -> {
                        dialog.cancel()
                    }
                }
            }
        } else {

            if (!isCameraAllowed)
            MaterialAlertDialogBuilder(this)
                    .setPositiveButton(android.R.string.cancel) { _, _ ->
                        finish()
                    }
                    .setNegativeButton(R.string.button_name_app_info) { _, _ ->
                        val uriString = "package:$packageName"
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
