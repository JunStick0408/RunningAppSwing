package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import dao.RunningDao;
import model.Running;
import view.RunningDialog;
import view.RunningFrame;

public class RunningController {
	private final RunningFrame frame;
	private final RunningDao dao;

	public RunningController(RunningFrame frame, RunningDao dao) {
		super();
		this.frame = frame;
		this.dao = dao;

		//1.編集ボタン押下時に起動する
		// ボタンに「クリックされた時の監視役（リスナー）」をあらかじめ登録しておく
		//	new ActionListener() { ... } で「ボタンが押されたときの指示書（リスナー）」を1つ作ります。
		//	それを addActionListener でボタンにあらかじめ貼り付けて（登録して）おきます。	
		//	※この時点では addRunningRecord() は実行されません。じっと待機状態になります。	
		this.frame.openDialogButton.addActionListener(new ActionListener() {
			// 実際にボタンが押されたタイミングで、Javaが自動的にこのメソッドを呼び出す
			//	ActionListener内のactionPerformedを書き換えたのでオーバーライド		
			//	@Override を書いておくと、メソッド名を打ち間違えたときに Java（Eclipse）が「そんなメソッド上書きできてないよ！」とエラーで教えてくれます。		
			@Override
			public void actionPerformed(ActionEvent e) {
				// ボタンが押されたので、記録の追加処理を実行する
				openAddDialog();
			}
		});

		//2.削除押下時に起動する
		this.frame.deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteRunningRecord();
			}
		});

		// 3.画面立ち上げ時に起動して全件表示
		updateListView();
	}

	// ★ 1. ダイアログを開いてイベントを登録するメソッド
	public void openAddDialog() {
		// メモリ上に部品を準備（まだ表示されない）
		RunningDialog dialog = new RunningDialog(frame);

		// ダイアログ内の新規ボタン押下時に起動
		// あくまで押下時なので、次の処理に移って先に画面を表示
		dialog.addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeAdd(dialog);
			}
		});

		// 編集画面の表示 アクションリスナーと記述順を入れ替えるとモーダルでプログラムを停めちゃうのでダメ。
		dialog.setVisible(true);

	}

	// ★ 2. 実際の登録処理を担当するメソッド（スッキリ！）
	public void executeAdd(RunningDialog dialog) {

		try {
			// 1.画面の入力フィールドから文字列を取得
			String dateStr = dialog.dateField.getText();
			String disrtanceStr = dialog.distanceField.getText();
			String durationeStr = dialog.durationField.getText();
			String stepsStr = dialog.stepsField.getText();
			String memoStr = dialog.memoField.getText();

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

			// 6.DB登録と一覧更新が成功したため、別窓（ダイアログ）を閉じて終了する
			dialog.dispose();

		} catch (Exception e) {
			System.out.println("入力エラーが発生しました" + e.getMessage());
		}
	}

	public void updateListView() {
		// テーブルを空にする 行数0なので
		frame.tableModel.setRowCount(0);

		// 最新のデータを取得して、1件ずつリストに追加
		List<Running> list = dao.findAll();
		for (Running r : list) {
			// 1件分のデータを配列にまとめる
			// タイトルのcolumnNames の要素数と合わせる必要あり
			Object[] rowData = {
					r.getRunDate(),
					r.getDistance(),
					r.getDuration(),
					r.getSteps(),
					r.getMemo()
			};

			// ビューのモデルに追加
			frame.tableModel.addRow(rowData);
		}

	}

	public void deleteRunningRecord() {

		try {
			// 1. 画面の ID フィールドから文字列を取得して数値に変換
			String idStr = frame.idField.getText();
			int id = Integer.parseInt(idStr);

			// 2. 削除実行
			dao.delete(id);

			// 3. 画面の表示を更新
			updateListView();

			//	4. 入力フィールドをクリア
			frame.idField.setText("");

			// Integer.parseInt("abc") や Integer.parseInt("")（空文字）を実行したときに発生します。これをつかまえる（catch）ことで、「数字を入力してください」といった具体的な案内を出せます。
		} catch (NumberFormatException e) {
			System.out.println("削除対象のIDを正しい数値で入力してください。");
			// NumberFormatException 以外の「想定外のあらゆるエラー（DB接続切れなど）」をまとめて拾うセーフティネットとして機能します。複数 catch を並べる場合、一番最後に書くルールになっています。
		} catch (Exception e) {
			// 例えば Integer.parseInt("abc") でエラーが出た場合、e.getMessage() を呼ぶと "For input string: \"abc\"" という具体的なエラー内容が文字列で手に入ります。コンソールや画面にエラー理由を出力したいときに使います。
			System.out.println("削除処理中にエラーが発生しました: " + e.getMessage());
		}
	}

}
