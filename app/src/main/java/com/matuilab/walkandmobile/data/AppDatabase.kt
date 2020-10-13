/** データベースの定義
 *
 * https://tech.recruit-mp.co.jp/mobile/post-12311/
 */
package com.matuilab.walkandmobile.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.matuilab.walkandmobile.data.dao.TenjiDao
import com.matuilab.walkandmobile.data.model.Blockmessage

@Database(
        entities = [Blockmessage::class],
        version = 1
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun tenjiDao(): TenjiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "tenji_database"
                )
//                    .addCallback(RoomDatabaseCallback())    //初期データを用いない場合はこの行をコメントアウトする。
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}