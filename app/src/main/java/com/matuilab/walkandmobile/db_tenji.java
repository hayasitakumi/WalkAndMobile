/** データベースの定義
 *
 *  https://tech.recruit-mp.co.jp/mobile/post-12311/
 * */

package com.matuilab.walkandmobile;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {entity_blockmessage.class}, version = 1)
public abstract class db_tenji extends RoomDatabase {
    public abstract dao_tenji daoTenji();
}
