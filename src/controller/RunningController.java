package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import dao.RunningDao;
import model.Running;
import model.TimeSlot;
import model.Weather;
import view.DialogMode;
import view.RunningFormDialog;
import view.RunningFrame;

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
						openEditDialog();
					}
				}
			}
		});

		// 4.編集ボタン押下時に起動する
		this.frame.editButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openEditDialog();
			}
		});

		// 5-1.親画面の体重入力欄でエンターキーを押すと再計算
		frame.weightField.addActionListener(e -> updateListView()); // addActionListenerでエンターキーを察知

		/*省略しないとこの書き方
		 * frame.weightField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateListView();
			}
		});
		*/

		// 5-2.親画面の体重入力欄でフォーカスが外れた時に再計算
		frame.weightField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateListView();
			}

		});

		// 6.複写ボタン押下時に起動
		frame.copyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				openCopyDialog();
			}
		});

		// 7.画面立ち上げ時に起動して全件表示
		updateListView();
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

		// 合計値を取得して、mapから取りだす
		Map<String, Double> map = dao.getRunningSummary();
		Double totalDistance = map.get("totalDistance"); // マップタイトルから合計値を取得
		Double totalDuration = map.get("totalDuration"); // マップタイトルから合計値を取得
		Double activeMonth = map.get("activeMonth");

		// -----------------------------------------------------------------
		// 1. 1キロペースの計算・表示
		// -----------------------------------------------------------------
		// Doubleは箱なのでnullがありえる。中に数値が無いときにNullPointerException (アプリ強制終了)を防ぐ。
		// totalDistance Double型を0で割るとDouble.POSITIVE_INFINITY（無限大）になってしまう仕様
		if (totalDistance != null && totalDistance > 0 && totalDuration != null && totalDuration > 0) {
			Double avgPace = totalDuration / totalDistance;
			String paceLabel = String.format("1キロペース: %.1f （分/km）", avgPace); // %はプレースホルダ .1は小数点第一位まで fは少数を指す
			frame.paceLabel.setText(paceLabel);
		} else {
			// データが0件のとき、または全削除したとき初期化
			frame.paceLabel.setText("1キロペース: - （分/km）");
		}

		// -----------------------------------------------------------------
		// 2. 月平均距離の計算・表示
		// -----------------------------------------------------------------
		if (totalDistance != null && totalDistance > 0 && activeMonth != null && activeMonth > 0) {
			Double avgMonthDistance = totalDistance / activeMonth;
			String distanceLabel = String.format("月平均距離: %.1f （km/月）", avgMonthDistance); // avgMonthDistanceがDoubleなのでfしか指定できない
			frame.distanceLabel.setText(distanceLabel);
		} else {
			// データが0件のとき、または全削除したとき初期化
			frame.distanceLabel.setText("月平均距離: - （km/月）");
		}

		// -----------------------------------------------------------------
		// 3. 消費カロリーの計算・表示
		// -----------------------------------------------------------------
		Double weight = 0.0;
		// 前後の空白を除去（スペースのみの入力は空文字 "" になる）
		String weightStr = frame.weightField.getText().trim();
		try {
			weight = Double.parseDouble(weightStr);
		} catch (NumberFormatException e) {
			// 空文字や数値以外の文字列が入った場合に安全に 0.0 をセットする
			weight = 0.0;
		}

		if (totalDistance != null && totalDistance > 0 && weight != null && weight > 0) {
			Double kcal = totalDistance * weight;
			String caloriesLabel = String.format("累計消費カロリー: %.1f （kcal）", kcal);
			frame.caloriesLabel.setText(caloriesLabel);
		} else {
			frame.caloriesLabel.setText("累計消費カロリー: - （kcal）");
		}

		// -----------------------------------------------------------------
		// 4. 通算走行時間の計算・表示
		// -----------------------------------------------------------------
		if (totalDuration != null && totalDuration > 0) {
			Double totalHours = totalDuration / 60;
			String durationLabel = String.format("通算走行時間: %.1f （時間）", totalHours);
			frame.timeLabel.setText(durationLabel);
		} else {
			frame.timeLabel.setText("通算走行時間: - （時間）");
		}

	}

	// 新規ボタン押下時 （画面にセット不要）
	public void openAddDialog() {
		// メモリ上に部品を準備（まだ表示されない）
		RunningFormDialog dialog = new RunningFormDialog(frame, DialogMode.NEW, null);

		// ダイアログ内の新規ボタン押下時に起動
		// あくまで押下時なので、次の処理に移って先に画面を表示
		dialog.submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeSave(dialog);
			}
		});

		// 編集画面の表示 アクションリスナーと記述順を入れ替えるとモーダルでプログラムを停めちゃうのでダメ。
		dialog.setVisible(true);

	}

	// 複写の処理　（画面に値をセットするのは編集処理から、INSERTは新規処理から）
	public void openCopyDialog() {
		// マウスリスナー内で取得した選択行のインデックスを入れることで指定の1件のみ取得
		Running target = getSelectedRunning();
		if (target == null) {
			return;
		}

		// メモリ上に部品を準備（まだ表示されない）
		// 画面が作られると同時に画面に選択行の値がセットされる
		RunningFormDialog dialog = new RunningFormDialog(frame, DialogMode.COPY, target); // コンストラクタ実行され、ラジオボタン「晴れ」が初期trueに

		// 新規画面のINSERT処理を流用
		dialog.submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeSave(dialog);
			}
		});

		// 編集画面の表示 アクションリスナーと記述順を入れ替えるとモーダルでプログラムを停めちゃうのでダメ。
		dialog.setVisible(true);
	}

	// ★ 編集押下時 （画面に初期表示）
	public void openEditDialog() {
		// マウスリスナー内で取得した選択行のインデックスを入れることで指定の1件のみ取得
		Running target = getSelectedRunning();
		if (target == null) {
			return;
		}

		// メモリ上に部品を準備（まだ表示されない）
		// 画面が作られると同時に画面に選択行がセットされる
		RunningFormDialog dialog = new RunningFormDialog(frame, DialogMode.EDIT, target); // コンストラクタ実行され、ラジオボタン「晴れ」が初期trueに

		// ダイアログ内の新規ボタン押下時に起動
		// あくまで押下時なので、次の処理に移って先に画面を表示
		dialog.submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeSave(dialog);
			}
		});

		// 編集画面の表示 アクションリスナーと記述順を入れ替えるとモーダルでプログラムを停めちゃうのでダメ。
		dialog.setVisible(true);

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

	// 編集・複写時に画面に値を追加する準備　共通ヘルパーメソッド
	private Running getSelectedRunning() {
		int selectedRow = frame.recordTable.getSelectedRow();

		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(frame, "編集する行を選択してください。", "入力エラー", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// 全件リストを作る
		List<Running> list = dao.findAll();

		// マウスリスナー内で取得した選択行のインデックスを入れることで指定の1件のみ取得
		return list.get(selectedRow);

	}

	// 重複したバリデーションをまとめて、画面からの値を取得、最後のDB処理を分岐させる
	public void executeSave(RunningFormDialog dialog) {
		// -------------------------------------------------------------
		// 1. 入力値の取得と基本バリデーション
		// -------------------------------------------------------------
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

		// -------------------------------------------------------------
		// 2. 数値型のチェック（距離・時間・歩数）
		// -------------------------------------------------------------
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
		// -------------------------------------------------------------
		// 3. DB登録・更新処理（モード分岐）
		// -------------------------------------------------------------
		try {
			// 1.画面の入力フィールドから文字列を取得
			String idStr = dialog.idField.getText();
			String memoStr = dialog.memoField.getText();

			// 2.適切な型に変換

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

			// モードによる書き分け
			if (dialog.getDialogMode() == DialogMode.EDIT) {
				int id = Integer.parseInt(idStr);
				// 3.Modelオブジェクトの作成
				Running running = new Running(id, distance, duration, steps, memoStr, runDate, timeSlotStr, weather);
				// 4.daoを使って保存・追加
				dao.update(running);
			} else {
				// 3.Modelオブジェクトの作成（IDは仮で0を設定）
				Running running = new Running(0, distance, duration, steps, memoStr, runDate, timeSlotStr, weather);
				// 4.daoを使って保存・追加
				dao.add(running);
			}

			// 5.画面のリスト表示を更新
			updateListView();

			// 6.DB登録と一覧更新が成功したため、別窓（ダイアログ）を閉じて終了する
			dialog.dispose();

		} catch (Exception e) {
			System.out.println("保存処理中にエラーが発生しました" + e.getMessage());
		}
	}

}
