package com.matuilab.walkandmobile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.matuilab.walkandmobile.http.HttpGetJson
import com.matuilab.walkandmobile.util.LanguageProcessor
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import java.util.*


class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**Preferenceの設定*/
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        /**Spinnerの設定*/
        val spinnerAdapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.array_settings_spinner, android.R.layout.simple_spinner_item)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        settings_spinner.adapter = spinnerAdapter

        when (sharedPreferences.getFloat("PLAYBACK_SPEED", 1.0f)) {
            0.25f -> settings_spinner.setSelection(0)
            0.5f -> settings_spinner.setSelection(1)
            0.75f -> settings_spinner.setSelection(2)
            1.0f -> settings_spinner.setSelection(3)
            1.25f -> settings_spinner.setSelection(4)
            1.5f -> settings_spinner.setSelection(5)
            1.75f -> settings_spinner.setSelection(6)
            2.0f -> settings_spinner.setSelection(7)
        }

        // リスナーを登録
        settings_spinner.onItemSelectedListener = object : OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                val spinner = parent as Spinner
                val item = spinner.selectedItem as String
                Log.d("spinner_test", "item:$item position:$position id:$id")

                when (position) {
                    // 0.25x
                    0 -> editor.putFloat("PLAYBACK_SPEED", 0.25f).apply()
                    // 0.5x
                    1 -> editor.putFloat("PLAYBACK_SPEED", 0.5f).apply()
                    // 0.75x
                    2 -> editor.putFloat("PLAYBACK_SPEED", 0.75f).apply()
                    // normal
                    3 -> editor.putFloat("PLAYBACK_SPEED", 1.0f).apply()
                    // 1.25x
                    4 -> editor.putFloat("PLAYBACK_SPEED", 1.25f).apply()
                    // 1.5x
                    5 -> editor.putFloat("PLAYBACK_SPEED", 1.5f).apply()
                    // 1.75x
                    6 -> editor.putFloat("PLAYBACK_SPEED", 1.75f).apply()
                    // 2x
                    7 -> editor.putFloat("PLAYBACK_SPEED", 2.0f).apply()
                }
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
            }
        }

        val downloadTexts = arrayOf(
                getString(R.string.text_settings_download_guidance_infomation),
                getString(R.string.text_settings_download_guidance_infomation_and_guidance_voices)
        )

        //android.R.layout.simple_list_item_1は用意されているレイアウトファイル
        val downloadAdapter: ArrayAdapter<String> = ArrayAdapter<String>(requireActivity(), android.R.layout.simple_list_item_1, downloadTexts)

        //Adapterをセットする
        settings_listview.adapter = downloadAdapter

        settings_listview.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val getJson = HttpGetJson(requireActivity())
            val saveAppDir = requireActivity().filesDir.absolutePath

            val languageProcessor = LanguageProcessor(resources.getStringArray(R.array.code_language))
            var localLang = Locale.getDefault().language   //端末の設定言語を取得

            if (languageProcessor.indexOfLanguage(localLang) <= 0) {
                // 対応リストに無ければ英語を使用（日本語、英語でもなければ英語を設定）
                localLang = "en"
            }
            //when you need to act on itemClick
            when (position) {
                0 -> {
                    getJson.execute(saveAppDir, languageProcessor.addressLanguage(localLang))
                }
                1 -> {
                    getJson.execute(saveAppDir, languageProcessor.addressLanguage(localLang), "FALSE")
                }
            }
        }
    }
}