package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JOptionPane;

import dao.RunningDao;
import model.Running;
import model.TimeSlot;
import model.Weather;
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

		//1.新規ボタン押下時に起動する
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

		// 4.編集ボタン押下時に起動する
		this.frame.editButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// ダブルクリックの必要は無し
				int selectedRow = frame.recordTable.getSelectedRow();
				// 行が選択されていない場合は -1を返します。
				// 直接呼び出して、selectedRow==-1のときにエラーメッセージを出す。
				openEditDialog(selectedRow);
			}
		});

		// 5.画面立ち上げ時に起動して全件表示
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
		// スピナーから取り出すときはこの形にする
		java.util.Date utilDate = (java.util.Date) dialog.dateSpinner.getValue();
		// addのコンストラクタ用の形に変換
		java.sql.Date runDate = new java.sql.Date(utilDate.getTime());

		LocalDate today = LocalDate.now();
		// java.sql.Dateはオブジェクトなので専用メソッドで比較する 今日の日付以降が入力されてればエラー
		if (runDate.toLocalDate().isAfter(today)) {
			JOptionPane.showMessageDialog(dialog, "未来の日付は入力できません。", "入力エラー", JOptionPane.ERROR_MESSAGE);
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

			// 1.画面の入力フィールドから文字列を取得
			String memoStr = dialog.memoField.getText();

			// 時間帯コンボボックス(Enum型)から選択されたEnumを取り出して、文字列に変換する
			TimeSlot selectedSlot = (TimeSlot) dialog.timeSlotCombo.getSelectedItem();
			String timeSlot = (selectedSlot != null) ? selectedSlot.name() : TimeSlot.MORNING.name();

			// ラジオボタンの真偽から、Enumの数値を紐づける
			int weather = Weather.SUNNY.getCode();
			if (dialog.cloudyRadio.isSelected()) {
				weather = Weather.CLOUDY.getCode();
			} else if (dialog.rainyRadio.isSelected()) {
				weather = Weather.RAINY.getCode();
			}

			// 3.Modelオブジェクトの作成（IDは仮で0を設定）
			Running running = new Running(0, distance, duration, steps, memoStr, runDate, timeSlot, weather);

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
			//　DBの値をEnumの定義した定数に変換する & 選ばれたEnum定数のみを返す
			TimeSlot slot = (r.getTimeSlot() != null) ? TimeSlot.valueOf(r.getTimeSlot()) : TimeSlot.MORNING;
			Weather weather = Weather.getByCode(r.getWeather());
			// 1件分のデータを配列にまとめる
			// タイトルのcolumnNames の要素数と合わせる必要あり
			Object[] rowData = {
					r.getRunDate(),
					r.getDistance(),
					r.getDuration(),
					r.getSteps(),
					slot.getDisplayLabel(), // Enumで設定した日本語になおす
					weather.getDisplayLabel(), // Enumで設定した日本語になおす
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
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(frame, "編集する行を選択してください。。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// 全件リストを作る
		List<Running> list = dao.findAll();
		// マウスリスナー内で取得した選択行のインデックスを入れることで指定の1件のみ取得
		Running target = list.get(selectedRow);

		// メモリ上に部品を準備（まだ表示されない）
		RunningEditDialog dialog = new RunningEditDialog(frame); // コンストラクタ実行され、ラジオボタン「晴れ」が初期trueに

		// 編集画面に表示する初期値をセット
		dialog.idField.setText(String.valueOf(target.getId()));
		dialog.dateSpinner.setValue(target.getRunDate());
		dialog.distanceField.setText(String.valueOf(target.getDistance()));
		dialog.durationField.setText(String.valueOf(target.getDuration()));
		dialog.stepsField.setText(String.valueOf(target.getSteps()));
		dialog.memoField.setText(target.getMemo());

		if (target.getTimeSlot() != null) {
			try {
				// target.getTimeSlot()をEnumに変換
				// JComboBox<TimeSlot>がEnum型なので、Enumにしないと入らないため
				dialog.timeSlotCombo.setSelectedItem(TimeSlot.valueOf(target.getTimeSlot()));
			} catch (IllegalArgumentException e) {
				System.err.println("未定義の時間帯コードがDBに存在します:" + target.getTimeSlot());
				// 画面上はデフォルト値（MORNING）を選択させて落ちないようにする
				dialog.timeSlotCombo.setSelectedItem(TimeSlot.MORNING);
			}
		}

		// findAll()で得た初期値をEnumの形に変換する
		// getByCodeは天気の数値と一致するEnum定数を返すメソッド
		Weather weatherEnum = Weather.getByCode(target.getWeather());
		if (weatherEnum == Weather.CLOUDY) {
			dialog.cloudyRadio.setSelected(true);
		} else if (weatherEnum == Weather.RAINY) {
			dialog.rainyRadio.setSelected(true);
		} else {
			dialog.sunnyRadio.setSelected(true);
		}

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

		// スピナーから取り出すときはこの形にする
		java.util.Date utilDate = (java.util.Date) dialog.dateSpinner.getValue();
		// addのコンストラクタ用の形に変換
		java.sql.Date runDate = new java.sql.Date(utilDate.getTime());

		LocalDate today = LocalDate.now();
		// 未入力チェック
		if (runDate.toLocalDate().isAfter(today)) {
			JOptionPane.showMessageDialog(dialog, "未来の日付は入力できません。", "入力エラー", JOptionPane.ERROR_MESSAGE);
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

		// ★スピナーは、ビューでスピナーを使っており、Date型に変換する必要がないのでtry-catchやバリデーションチェックが不要

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

			// 時間帯コンボボックス(Enum型)から選択されたEnumを取り出して、文字列に変換する
			TimeSlot selectedSlot = (TimeSlot) dialog.timeSlotCombo.getSelectedItem();
			// name()はEnumから文字列型を取り出す
			String timeSlotStr = (selectedSlot != null) ? selectedSlot.name() : TimeSlot.MORNING.name();

			// 天候ラジオボタン（ビュー）からtrue/falseを取得して、Enumを介して数値にする
			// WeatherEnumのgetCode()で数値に変換できる
			int weather;
			if (dialog.cloudyRadio.isSelected()) {
				weather = Weather.CLOUDY.getCode();
			} else if (dialog.rainyRadio.isSelected()) {
				weather = Weather.RAINY.getCode();
			} else {
				weather = Weather.SUNNY.getCode();
			}

			// 3.Modelオブジェクトの作成
			Running running = new Running(id, distance, duration, steps, memoStr, runDate, timeSlotStr, weather);

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
