package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
	// 接続先URL（データベース名: running_db）
	// characterEncoding=UTF-8 日本語のメモ（「すごく暑い」など）が文字化けしないようにする
	// serverTimezone=Asia/Tokyo（日本標準時）DBから日付や時間を取得したときに「時間が9時間ズレる」という面倒なトラブルを未然に防ぐ
	private static final String URL = "jdbc:mysql://localhost:3306/running_db?characterEncoding=UTF-8&serverTimezone=Asia/Tokyo";
	// MySQL Workbenchにログインするときのユーザー名とパスワードを設定してください
	private static final String USER = "root";
	private static final String PASS = "Junya0408!";

	/*
	 * Connection 後でデータを入れたり取ったりする（INSERT や SELECT）ときに、この Connection（パイプ）を通す必要があります。
	 * 「接続先（URL）」「ユーザー名（USER）」「パスワード（PASS）」の3点 を渡して、「MySQLさん、この情報でログインして接続させてください！」と要求し、成功すると Connection オブジェクトを返してくれます。
	 * throws SQLException もしパスワードが違ったり、MySQLが起動していなくて接続に失敗したらエラー（例外）を投げますよ」という宣言です。呼び出し側（DAO）でエラーハンドリング
	 * 
	 * 目的：データを登録するときも、検索するときも、削除するときも、毎回 DriverManager.getConnection(...) と長い引数を書かなければいけなくなります。
	 * 　　　DBUtil.getConnection() と呼び出すだけで一発で接続を取得できるようにするための、便利なお手伝い関数（ユーティリティ）です！
	 * 
	 * public：他のパッケージ（dao の中の別のクラスなど）からでも、このメソッドを自由に呼び出せる
	 * static有り：クラスを new（インスタンス化）しなくても直接呼び出せる ようになります。呼び出し方：DBUtil.getConnection() （クラス名.メソッド名 で直接実行）
	 * static無し：毎回 new して実体を作ってから呼び出す必要があります。呼び出し方：DBUtil util = new DBUtil(); util.getConnection(); （面倒！）
	 * DBUtil は「接続用の URL やパスワードをまとめただけの便利ツール」です。わざわざ new DBUtil() として実体を作る必要がありません。
	 * そのため、「どこからでも DBUtil.getConnection() と書くだけで一発で呼び出せるようにしよう！」 という目的で public static を付けています。
	 */

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASS);
	}
}
