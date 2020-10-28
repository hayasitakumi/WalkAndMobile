/** データベースの定義
 *
 * https://tech.recruit-mp.co.jp/mobile/post-12311/
 */
package com.matuilab.walkandmobile.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matuilab.walkandmobile.data.dao.TenjiDao
import com.matuilab.walkandmobile.data.model.Blockmessage
import com.matuilab.walkandmobile.data.model.BlockmessageEN

// データベースの設定、exportSchemaをfalseにするとエラーが消える
@Database(
        entities = [Blockmessage::class, BlockmessageEN::class],
        version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun daoTenji(): TenjiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        ////////// データベースの移行に関して
        /* データベースの仕様が変更（アップデート）された際に、その変更に対応（移行）できるように処理を定義する。
       バージョン 1-->2 のように、どう移行するか、何の処理をすれば移行できるのかを定義。
       Room.databaseBuilderを記述している箇所にも追記が必要。（HttpResponsAsync と HttpGetJson）
       ざっくり概要 : https://medium.com/@star_zero/room%E3%81%AE%E3%83%9E%E3%82%A4%E3%82%B0%E3%83%AC%E3%83%BC%E3%82%B7%E3%83%A7%E3%83%B3%E3%81%BE%E3%81%A8%E3%82%81-a07593aa7c78
       DB移行について（公式） : https://developer.android.com/training/data-storage/room/migrating-db-versions?hl=ja
       バージョン違いにより起こりうるエラーについて（公式） : https://developer.android.com/training/data-storage/room/prepopulate?hl=ja#java
    * */
        // 移行バージョン 1 --> 2    (英語案内情報blockmessage_enの追加)
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE 'blockmessage_en' (" +
                        "'id' INT, 'code' INT, 'angle' INT, 'messagecategory' TEXT, 'message' TEXT, 'reading' TEXT, 'wav' TEXT, " +
                        "PRIMARY KEY('id') )")
            }
        }
    }
}