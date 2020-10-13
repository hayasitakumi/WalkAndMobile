/**ローカルデータベーステーブル定義クラス
 * ローカルデータベースを利用するための機能Roomを使用してデータベース操作
 * entity_blockmessage : 案内情報の管理を行うテーブル
 * */

package com.matuilab.walkandmobile;

import androidx.room.*;

// テーブル名指定(tableName) : blockmessage   DB関連のクラスと見分けにくいのでクラス名は識別用の命名、tableNameが本命
@Entity(tableName = "blockmessage")
public class entity_blockmessage {
    @PrimaryKey
    public int id;

    public int code;

    public int angle;

    public String messagecategory;

    public String message;

    public String reading;

    public String wav;
}
