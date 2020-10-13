/**ローカルデータベーステーブル定義クラス
 * ローカルデータベースを利用するための機能Roomを使用してデータベース操作
 * entity_blockmessage : 案内情報の管理を行うテーブル
 */
package com.matuilab.walkandmobile.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// テーブル名指定(tableName) : blockmessage   DB関連のクラスと見分けにくいのでクラス名は識別用の命名、tableNameが本命
@Entity(tableName = "blockmessage")
class Blockmessage {
    @JvmField
    @PrimaryKey
    var id = 0
    @JvmField
    var code = 0
    @JvmField
    var angle = 0
    @JvmField
    var messagecategory: String? = null
    @JvmField
    var message: String? = null
    @JvmField
    var reading: String? = null
    @JvmField
    var wav: String? = null
}