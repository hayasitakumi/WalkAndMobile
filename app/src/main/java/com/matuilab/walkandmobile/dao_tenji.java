/**データベース操作用クラス
 * Roomの中でデータベース操作のための機能となるDao
 * Data Access Object
 * */
package com.matuilab.walkandmobile;

import androidx.room.*;

@Dao
interface dao_tenji {
    // blockmessage全取得
    @Query("SELECT * FROM blockmessage WHERE code=:code AND angle=:angle AND messagecategory=:category")
    public entity_blockmessage[] getBlockmessage(int code, int angle, String category);

    // 案内文を取得
    @Query("SELECT message FROM blockmessage WHERE code=:code AND angle=:angle AND messagecategory=:category")
    public String[] getMessage(int code, int angle, String category);

    // 音声ファイル名を取得
    @Query("SELECT wav FROM blockmessage WHERE code=:code AND angle=:angle AND messagecategory=:category")
    public String[] getWav(int code, int angle, String category);

    // blockdataにINSERTする
    //@Query("INSERT INTO blockdata(code, category, latitude, longitude, install, buildingfloor, name) VALUES (:code, :category, :latitude, :longitude, :install, :buildingfloor, :name)")
    //public void insertBlockdata(int code, String category, double latitude, double longitude, int install, int buildingfloor, String name);

    // blockmessageにINSERTする（まとめてやるとき）
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertBlockmessage(entity_blockmessage... blockmessage);

    // blockmessageにINSERTする（上と同じ、非推奨）
    @Query("INSERT INTO blockmessage(id, code, angle, messagecategory, message, reading, wav) VALUES (:id, :code, :angle, :category, :message, :reading, :wav)")
    public void insertBlockmessageB(int id, int code, int angle, String category, String message, String reading, String wav);

    // blockmessageを全削除する【取扱注意】
    @Query("DELETE FROM blockmessage")
    public void deleteAllBlockmessage();
}
