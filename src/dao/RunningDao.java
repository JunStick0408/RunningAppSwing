package dao;

import java.util.List;
import java.util.Map;

import model.Running;

// 画面（View/Controller）側は「データを保存・取得したい」だけであり、保存先が 「メモリ」なのか「ファイル（CSV）」なのか「データベース（SQLite/MySQL）」なのかを知る必要がないからです。
// インタフェースにしておくことで、後からデータ保存先をファイルやDBに変更したくなった時も、画面側のコードを一切書き換えずに済むようになります。
public interface RunningDao {
	// INSERT
	void add(Running running);

	//	全件SELECT（戻り値List<Running>のFindAll()メソッド）
	List<Running> findAll();

	//　DELETE
	void delete(int id);

	// UPDATE
	void update(Running running);

	// 合計距離と合計時間をまとめて取得 ※戻り値が必要
	Map<String, Double> getRunningSummary(); // stringに合計値のタイトル、Doubleに合計値を入れることで取り出しやすくする
}
