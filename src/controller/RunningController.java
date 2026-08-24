package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import dao.RunningDao;
import model.Running;
import view.RunningFrame;

public class RunningController {
	private final RunningFrame frame;
	private final RunningDao dao;

	public RunningController(RunningFrame frame, RunningDao dao) {
		super();
		this.frame = frame;
		this.dao = dao;

		// ボタンに「クリックされた時の監視役（リスナー）」をあらかじめ登録しておく
		//	new ActionListener() { ... } で「ボタンが押されたときの指示書（リスナー）」を1つ作ります。
		//	それを addActionListener でボタンにあらかじめ貼り付けて（登録して）おきます。	
		//	※この時点では addRunningRecord() は実行されません。じっと待機状態になります。	
		this.frame.addButton.addActionListener(new ActionListener() {
			// 実際にボタンが押されたタイミングで、Javaが自動的にこのメソッドを呼び出す
			//	ActionListener内のactionPerformedを書き換えたのでオーバーライド		
			//	@Override を書いておくと、メソッド名を打ち間違えたときに Java（Eclipse）が「そんなメソッド上書きできてないよ！」とエラーで教えてくれます。		
			@Override
			public void actionPerformed(ActionEvent e) {
				// ボタンが押されたので、記録の追加処理を実行する
				addRunningRecord();
			}
		});
	}

	public void addRunningRecord() {
		try {
			// 1.画面の入力フィールドから文字列を取得
			String dateStr = frame.dateField.getText();
			String disrtanceStr = frame.disrtanceField.getText();
			String durationeStr = frame.durationField.getText();
			String stepsStr = frame.stepsField.getText();
			String memoStr = frame.memoField.getText();

			// 2.適切な型に変換
			Date runDate = Date.valueOf(dateStr);
			BigDecimal distance = new BigDecimal(disrtanceStr);
			int duration = Integer.parseInt(durationeStr);
			int steps = Integer.parseInt(stepsStr);

			// 3.Modelオブジェクトの作成（IDは仮で0を設定）
			Running running = new Running(0, distance, duration, steps, memoStr, runDate);

			// 4.daoを使って保存・追加
			dao.add(running);

			// 5.画面のリスト表示を更新
			updateListView();

			// 6.入力値をクリア
			frame.disrtanceField.setText("");
			frame.durationField.setText("");
			frame.stepsField.setText("");
			frame.memoField.setText("");

		} catch (Exception e) {
			System.out.println("入力エラーが発生しました" + e.getMessage());
		}
	}

	public void updateListView() {
		// リストを空にする
		frame.ListModel.clear();

		// 最新のデータを取得して、1件ずつリストに追加
		List<Running> list = dao.findAll();
		for (Running r : list) {
			/*
			 * 1. String.format(...) （文字の整形）
			 *getRunDate() や getDistance() などのデータを、決まったフォーマットの文字列に埋め込んで組み立てています。
			 *%s：文字列・日付・Decimal（文字として埋め込み）
			 *%d：整数（int 型の数値）
			 *"[" + r.getRunDate() + "] 距離: " + ...でも可
			 */
			String itemText = String.format("[%s] 距離: %s km | 時間: %d 分 | 歩数: %d 歩 | メモ: %s",
					r.getRunDate(), r.getDistance(), r.getDuration(), r.getSteps(), r.getMemo());
			frame.ListModel.addElement(itemText);
		}

	}

}
