package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.Running;

public class RunningDaoImpl implements RunningDao {

	// 1.DBに記録を1件追加する
	@Override
	public void add(Running running) {
		// id は自動で番号が振られるため、INSERT文からは除外します
		// ? にはシングルクォーテーションを付けません
		String sql = "INSERT INTO `running_db`.`running` (`distance`, `duration`, `steps`, `memo`, `run_date`) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = DBUtil.getConnection(); // 「DBへの専用電話回線をつなぐ（パスを開く）」
				PreparedStatement ps = conn.prepareStatement(sql)) { // 「送信するSQL（命令文）の下書きを用意する」

			// コントローラーのaddRunningRecord()で既にモデルには画面の入力値をセットしている。
			ps.setBigDecimal(1, running.getDistance());
			ps.setInt(2, running.getDuration());
			ps.setInt(3, running.getSteps());
			ps.setString(4, running.getMemo());
			ps.setDate(5, running.getRunDate());

			ps.executeUpdate();

		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	// 元のデータ(db)が外部から直接変更されるのを防ぐため、コピーした新しいリストを返す
	@Override
	public List<Running> findAll() {
		List<Running> list = new ArrayList<>();

		String sql = "SELECT * FROM running_db.running ORDER BY id";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) { // SQLは行と列状態なので、Javaオブジェクトの形に変換しないと扱えない
				Running running = new Running();
				running.setId(rs.getInt("id")); // 引数はカラム名。カラム名とすることで列が追加されてもバグらない。
				running.setDistance(rs.getBigDecimal("distance"));
				running.setDuration(rs.getInt("duration"));
				running.setSteps(rs.getInt("steps"));
				running.setMemo(rs.getString("memo"));
				running.setRunDate(rs.getDate("run_date"));

				list.add(running);

			}

		} catch (SQLException e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		return list;
	}

}
