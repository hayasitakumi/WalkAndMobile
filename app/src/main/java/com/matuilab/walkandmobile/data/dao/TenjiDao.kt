/**データベース操作用クラス
 * Roomの中でデータベース操作のための機能となるDao
 * Data Access Object
 */
package com.matuilab.walkandmobile.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.matuilab.walkandmobile.data.model.Blockmessage

@Dao
interface TenjiDao {
    // 動的に呼び出しているため未使用のように見えてます
    // 案内文を取得
    @Query("SELECT message FROM blockmessage WHERE code=:code AND angle=:angle AND messagecategory=:category")
    fun getMessage(code: Int, angle: Int, category: String?): Array<String?>?

    // blockmessageにINSERTする（非推奨、insertBlockmessage()の使い方が理想、できないのでこちらを使う）
    @Query("INSERT INTO blockmessage(id, code, angle, messagecategory, message, reading, wav) VALUES (:id, :code, :angle, :category, :message, :reading, :wav)")
    fun insertBlockmessageB(id: Int, code: Int, angle: Int, category: String?, message: String?, reading: String?, wav: String?)

    // blockmessageを全削除する【取扱注意】
    @Query("DELETE FROM blockmessage")
    fun deleteAllBlockmessage()

    ////////// 英語版
    // 案内文を取得
    @Query("SELECT message FROM blockmessage_en WHERE code=:code AND angle=:angle AND messagecategory=:category")
    fun getMessage_en(code: Int, angle: Int, category: String?): Array<String?>?

    // blockmessageにINSERTする（非推奨、insertBlockmessage()の使い方が理想、できないのでこちらを使う）
    @Query("INSERT INTO blockmessage_en(id, code, angle, messagecategory, message, reading, wav) VALUES (:id, :code, :angle, :category, :message, :reading, :wav)")
    fun insertBlockmessageB_en(id: Int, code: Int, angle: Int, category: String?, message: String?, reading: String?, wav: String?)

    // blockmessageを全削除する【取扱注意】
    @Query("DELETE FROM blockmessage_en")
    fun deleteAllBlockmessage_en()

    ////////// 以下、定義したものの未使用
    // blockmessage全取得
    @Query("SELECT * FROM blockmessage WHERE code=:code AND angle=:angle AND messagecategory=:category")
    fun getBlockmessage(code: Int, angle: Int, category: String?): Array<Blockmessage?>?

    // 音声ファイル名を取得
    @Query("SELECT wav FROM blockmessage WHERE code=:code AND angle=:angle AND messagecategory=:category")
    fun getWav(code: Int, angle: Int, category: String?): Array<String?>?

    // blockmessageにINSERTする（まとめてやるとき）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBlockmessage(vararg blockmessage: Blockmessage?) // blockdataにINSERTする
    //@Query("INSERT INTO blockdata(code, category, latitude, longitude, install, buildingfloor, name) VALUES (:code, :category, :latitude, :longitude, :install, :buildingfloor, :name)")
    //public void insertBlockdata(int code, String category, double latitude, double longitude, int install, int buildingfloor, String name);
}