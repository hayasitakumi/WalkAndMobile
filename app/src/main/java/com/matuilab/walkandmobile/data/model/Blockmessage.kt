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
    @PrimaryKey
    var id = 0
    var code = 0
    var angle = 0
    var messagecategory: String? = null
    var message: String? = null
    var reading: String? = null
    var wav: String? = null
}