package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Running;

public class RunningDaoImpl implements RunningDao {

	// 1.INSERT DBに記録を1件追加する
	@Override
	public void add(Running running) {
		// id は自動で番号が振られるため、INSERT文からは除外します
		// ? にはシングルクォーテーションを付けません
		String sql = "INSERT INTO `running_db`.`running` (`distance`, `duration`, `steps`, `memo`, `run_date`, time_slot, weather) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DBUtil.getConnection(); // 「DBへの専用電話回線をつなぐ（パスを開く）」
				PreparedStatement ps = conn.prepareStatement(sql)) { // 「送信するSQL（命令文）の下書きを用意する」

			// コントローラーのaddRunningRecord()で既にモデルには画面の入力値をセットしている。
			ps.setBigDecimal(1, running.getDistance());
			ps.setInt(2, running.getDuration());
			ps.setInt(3, running.getSteps());
			ps.setString(4, running.getMemo());
			ps.setDate(5, running.getRunDate());
			ps.setString(6, running.getTimeSlot());
			ps.setInt(7, running.getWeather());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 2.SELECT 元のデータ(db)が外部から直接変更されるのを防ぐため、コピーした新しいリストを返す
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
				running.setTimeSlot(rs.getString("time_slot"));
				running.setWeather(rs.getInt("weather"));

				list.add(running);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return list;
	}

	//	3.DELETE
	@Override
	public void delete(int id) {
		String sql = "DELETE FROM running_db.running WHERE id = ?";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setInt(1, id);

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 4.UPDATE
	@Override //インタフェースのメソッドを実装してることを明確にするため記載
	public void update(Running running) {
		String sql = "UPDATE running_db.running SET distance = ?, duration = ?, steps = ?, memo = ?, run_date = ?, time_slot = ?, weather = ? WHERE id = ? ";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setBigDecimal(1, running.getDistance());
			ps.setInt(2, running.getDuration());
			ps.setInt(3, running.getSteps());
			ps.setString(4, running.getMemo());
			ps.setDate(5, running.getRunDate());
			ps.setString(6, running.getTimeSlot());
			ps.setInt(7, running.getWeather());

			ps.setInt(8, running.getId());

			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	// SUM
	@Override
	public Map<String, Double> getRunningSummary() {
		Map<String, Double> map = new HashMap<>();
		map.put("totalDistance", 0.0);
		map.put("totalDuration", 0.0);
		map.put("activeMonth", 0.0);

		String sql = "SELECT SUM(distance) AS total_distance, "
				+ "SUM(duration) AS total_duration, "
				+ "COUNT(DISTINCT DATE_FORMAT(run_date, '%Y-%m')) AS active_month " // 月平均なので8月だったらいくらあろうが1のみカウントしたい。日数は要らないので DATE_FORMATで整形し、DISTINCTで重複排除したものをカウント
				+ "FROM running_db.running";

		try (Connection conn = DBUtil.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) { // if(rs.next())でもOK！合計されて次の行が無いから
				map.put("totalDistance", rs.getDouble("total_distance")); // ASと名前を付けておくことで列指定が容易になる。
				map.put("totalDuration", rs.getDouble("total_duration")); // ASと名前を付けておくことで列指定が容易になる。
				map.put("activeMonth", rs.getDouble("active_month"));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return map;
	}

}
