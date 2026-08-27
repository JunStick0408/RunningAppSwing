package dao;

import java.util.List;

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
}
