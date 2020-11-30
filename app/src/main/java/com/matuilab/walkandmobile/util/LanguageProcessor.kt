package com.matuilab.walkandmobile.util

class LanguageProcessor(codeLangs:Array<String>) {
    private val codeLangs = codeLangs //対応している言語コードの配列(onCreate()にてvalues/strings.xmlから読み込み)

    fun indexOfLanguage(localLang: String?): Int {
        // 言語コードの配列にて検索
        return codeLangs.asList().indexOf(localLang)
    }

    /** URLとDB用にアンダーバーを付けて返す*/
    fun addressLanguage(localLang: String?): String? {
        // DBの仕様上、日本語を基準とし、それ以外は名称の後ろに付ける
        // 日本語用 : テーブルblockdata    英語用 : テーブルblockdata_en
        return if (localLang == "ja") {
            "" //日本語にはアンダーバーなし
        } else {
            "_$localLang"
        }
    }
}