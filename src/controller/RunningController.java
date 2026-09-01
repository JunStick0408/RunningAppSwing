package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import javax.swing.JOptionPane;

import dao.RunningDao;
import model.Running;
import view.RunningEditDialog;
import view.RunningFrame;
import view.RunningNewDialog;

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

		//3.行をダブルクリック時に起動する
		// MouseListenerインタフェースは本来5メソッド書く必要あるが、MouseAdapter は、使わないメソッドの記述を省き、
		// 使いたい mouseClicked だけをピンポイントで書けるようにしてくれている便利クラスです。
		this.frame.recordTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				// ダブルクリックされたか確認
				if (e.getClickCount() == 2) {
					int selectedRow = frame.recordTable.getSelectedRow();
					// 行が選択されていない場合は -1を返します。
					// テーブル内なら、行以外をダブルクリックしても-1で実行されてしまうためガードする
					if (selectedRow != -1) {
						openEditDialog(selectedRow);
					}
				}
			}
		});

		// 4.画面立ち上げ時に起動して全件表示
		updateListView();
	}

	// ★ 1. ダイアログを開いてイベントを登録するメソッド
	public void openAddDialog() {
		// メモリ上に部品を準備（まだ表示されない）
		RunningNewDialog dialog = new RunningNewDialog(frame);

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
	public void executeAdd(RunningNewDialog dialog) {
		// 未入力チェック
		if (dialog.distanceField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "距離を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (dialog.durationField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "時間を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (dialog.stepsField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "歩数を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 距離の入力値チェック
		BigDecimal distance;
		try {
			// 編集画面で入力された文字が数字以外なら変換出来ずにcatchの処理に移行
			// BigDecimalにはparseは無いのでこの書き方で変換するしかない
			distance = new BigDecimal(dialog.distanceField.getText().trim());

			// int double は生の値（プリミティブ型）なのでそのままdistance<0のように使えるが
			// BigDecimal String は多機能型（オブジェクト型）なので箱に対して<>= を使うと箱には使えずエラーとなるので、専用の比較メソッドを使う。
			if (distance.compareTo(BigDecimal.ZERO) < 0) {
				JOptionPane.showMessageDialog(dialog, "距離は0以上を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
				return;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(dialog, "距離は半角数字で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 時間の入力値チェック
		int duration;
		try {
			// 編集画面で入力された文字が数字以外なら変換出来ずにcatchの処理に移行
			duration = Integer.parseInt(dialog.durationField.getText().trim());

			if (duration < 0) {
				JOptionPane.showMessageDialog(dialog, "時間は0以上を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
				return;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(dialog, "時間は半角数字で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 歩数の入力値チェック
		int steps;
		try {
			// 編集画面で入力された文字が数字以外なら変換出来ずにcatchの処理に移行
			steps = Integer.parseInt(dialog.stepsField.getText().trim());

			if (steps < 0) {
				JOptionPane.showMessageDialog(dialog, "歩数は0以上を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
				return;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(dialog, "歩数は半角数字で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// 1. JSpinner から日付（java.util.Date）を取得し、java.sql.Date に変換
			java.util.Date utilDate = (java.util.Date) dialog.dateSpinner.getValue();
			Date runDate = new Date(utilDate.getTime());

			// 1.画面の入力フィールドから文字列を取得
			String memoStr = dialog.memoField.getText();

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
			// 1.テーブルで選択されている行番号を取得
			int selectedRow = frame.recordTable.getSelectedRow();

			// 未選択の場合は処理を中断 showMessageDialogはメッセージのみ表示
			if (selectedRow == -1) {
				JOptionPane.showMessageDialog(frame, "削除する行を選択してください");
				return;
			}

			// 全件リストを作る
			List<Running> list = dao.findAll();

			// 取得した選択行のインデックスを入れることで指定の1件のみ取得
			Running target = list.get(selectedRow);

			// 該当行のidを取得する
			int id = target.getId();

			// 確認ダイアログを表示する 画面をポップアップさせて、ボタンを押させると最終的にどのボタンが押されたかのintが残るって処理
			// showConfirmDialogは、はい/いいえを表示
			int option = JOptionPane.showConfirmDialog(frame, "選択した記録（日付: " + target.getRunDate() + "）を本当に削除しますか？",
					"削除の確認", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			// YESと一致していたら
			if (option == JOptionPane.YES_OPTION) {
				// 2. 削除実行
				dao.delete(id);

				// 3. 画面の表示を更新
				updateListView();
			}

			// NumberFormatException 以外の「想定外のあらゆるエラー（DB接続切れなど）」をまとめて拾うセーフティネットとして機能します。複数 catch を並べる場合、一番最後に書くルールになっています。
		} catch (Exception e) {
			// 例えば Integer.parseInt("abc") でエラーが出た場合、e.getMessage() を呼ぶと "For input string: \"abc\"" という具体的なエラー内容が文字列で手に入ります。コンソールや画面にエラー理由を出力したいときに使います。
			System.out.println("削除処理中にエラーが発生しました: " + e.getMessage());
		}
	}

	// ★ 1. 編集ダイアログを開いてイベントを登録するメソッド
	public void openEditDialog(int selectedRow) {
		// 全件リストを作る
		List<Running> list = dao.findAll();
		// マウスリスナー内で取得した選択行のインデックスを入れることで指定の1件のみ取得
		Running target = list.get(selectedRow);

		// メモリ上に部品を準備（まだ表示されない）
		RunningEditDialog dialog = new RunningEditDialog(frame);

		// 編集画面に表示する初期値をセット
		dialog.idField.setText(String.valueOf(target.getId()));
		dialog.dateField.setText(String.valueOf(target.getRunDate()));
		dialog.distanceField.setText(String.valueOf(target.getDistance()));
		dialog.durationField.setText(String.valueOf(target.getDuration()));
		dialog.stepsField.setText(String.valueOf(target.getSteps()));
		dialog.memoField.setText(target.getMemo());

		// ダイアログ内の新規ボタン押下時に起動
		// あくまで押下時なので、次の処理に移って先に画面を表示
		dialog.updateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeEdit(dialog);
			}
		});

		// 編集画面の表示 アクションリスナーと記述順を入れ替えるとモーダルでプログラムを停めちゃうのでダメ。
		dialog.setVisible(true);

	}

	// ★ 2. 実際の編集処理を担当するメソッド（スッキリ！）
	public void executeEdit(RunningEditDialog dialog) {

		// 未入力チェック
		if (dialog.dateField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "日付を入力してください", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (dialog.distanceField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "距離を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (dialog.durationField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "時間を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		} else if (dialog.stepsField.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "歩数を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 日付の入力値チェック
		Date runDate;
		try {
			// Date.valueOf()は不正値を自動で判別してくれる。
			runDate = Date.valueOf(dialog.dateField.getText().trim());
		} catch (IllegalArgumentException e) {
			JOptionPane.showMessageDialog(dialog, "日付は yyyy-MM-dd の形式で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 距離の入力値チェック
		BigDecimal distance;
		try {
			// 編集画面で入力された文字が数字以外なら変換出来ずにcatchの処理に移行
			// BigDecimalにはparseは無いのでこの書き方で変換するしかない
			distance = new BigDecimal(dialog.distanceField.getText().trim());

			// int double は生の値（プリミティブ型）なのでそのままdistance<0のように使えるが
			// BigDecimal String は多機能型（オブジェクト型）なので箱に対して<>= を使うと箱には使えずエラーとなるので、専用の比較メソッドを使う。
			if (distance.compareTo(BigDecimal.ZERO) < 0) {
				JOptionPane.showMessageDialog(dialog, "距離は0以上を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
				return;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(dialog, "距離は半角数字で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 時間の入力値チェック
		int duration;
		try {
			// 編集画面で入力された文字が数字以外なら変換出来ずにcatchの処理に移行 parseIntにはNumberFormatExceptionに投げるようになっている。
			duration = Integer.parseInt(dialog.durationField.getText().trim());

			if (duration < 0) {
				JOptionPane.showMessageDialog(dialog, "時間は0以上を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
				return;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(dialog, "時間は半角数字で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 歩数の入力値チェック
		int steps;
		try {
			// 編集画面で入力された文字が数字以外なら変換出来ずにcatchの処理に移行
			steps = Integer.parseInt(dialog.stepsField.getText().trim());

			if (steps < 0) {
				JOptionPane.showMessageDialog(dialog, "歩数は0以上を入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
				return;
			}

		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(dialog, "歩数は半角数字で入力してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			// 1.画面の入力フィールドから文字列を取得
			String idStr = dialog.idField.getText();
			String memoStr = dialog.memoField.getText();

			// 2.適切な型に変換
			int id = Integer.parseInt(idStr);

			// 3.Modelオブジェクトの作成（IDは仮で0を設定）
			Running running = new Running(id, distance, duration, steps, memoStr, runDate);

			// 4.daoを使って保存・追加
			dao.update(running);

			// 5.画面のリスト表示を更新
			updateListView();

			// 6.DB登録と一覧更新が成功したため、別窓（ダイアログ）を閉じて終了する
			dialog.dispose();

		} catch (Exception e) {
			System.out.println("更新処理中にエラーが発生しました" + e.getMessage());
		}
	}

}
