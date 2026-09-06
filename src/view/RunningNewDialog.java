package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

import model.TimeSlot;

public class RunningNewDialog extends JDialog {

	// 入力フィールド（Controllerから参照できるよう public にしています）
	// public を消すとパッケージプライベート（無指定）という状態になるので、そのパッケージでしか使えなくなる。
	public JTextField idField = new JTextField();
	public JTextField distanceField = new JTextField();
	public JTextField durationField = new JTextField();
	public JTextField stepsField = new JTextField();
	public JTextField memoField = new JTextField();
	//	時間帯コンボボックス
	public JComboBox<TimeSlot> timeSlotCombo = new JComboBox<TimeSlot>(TimeSlot.values());
	//	天候ラジオボタン
	public JRadioButton sunnyRadio = new JRadioButton("晴れ");
	public JRadioButton cloudyRadio = new JRadioButton("曇り");
	public JRadioButton rainyRadio = new JRadioButton("雨");
	public ButtonGroup weatherGroup = new ButtonGroup();
	// 1. 日付用のスピンボタン(JSpinner：上下ボタン付の入力枠)を作成
	public JSpinner dateSpinner = new JSpinner(
			new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH)); //(new Date() で「今（現在日時）, どこまでも過去 , どこまでも未来 , ボタンでは日単位で変えられる)と設定

	// ボタン
	public JButton addButton = new JButton("登録");

	// このダイアログ（子画面）を呼び出すときに、親画面である RunningFrame（JFrame）を渡してもらうための引数として定義
	// コンストラクタなのでメイン画面の新規ボタンが押されたタイミングでnewしてコンストラクタ起動
	public RunningNewDialog(RunningFrame owner) {
		// 親画面（owner）、タイトル文字列（title）、モーダルにするか（modal = true）の3つを渡す型を選んで書いています。
		// superを省略すると引数無しsuper()になる
		// でも引数無しだと何の情報か分かんないからsuper(owner, title, modal)を書くのが定石
		super(owner, "新規記録の登録", true);
		setSize(350, 400);
		setLocationRelativeTo(owner);
		// レイアウトマネージャー（画面上の部品の並べ方を管理するオブジェクト）を new で生成しています。
		/* 1. 変数を作って new する
		   BorderLayout layout = new BorderLayout(10, 10); これはコンポーネント同士の隙間
		   2. その変数を渡す
		   setLayout(layout);
		 */
		setLayout(new BorderLayout(10, 10));

		weatherGroup.add(sunnyRadio);
		weatherGroup.add(cloudyRadio);
		weatherGroup.add(rainyRadio);
		sunnyRadio.setSelected(true);

		JPanel weatherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		weatherPanel.add(sunnyRadio);
		weatherPanel.add(cloudyRadio);
		weatherPanel.add(rainyRadio);

		// 2. 表示フォーマットを yyyy-MM-DD に指定
		JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
		dateSpinner.setEditor(editor); // ただのメソッドはコンストラクタ内でしか書けない。

		JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5)); // 6行2列で幅が5
		inputPanel.setBorder(BorderFactory.createTitledBorder("データの入力"));

		inputPanel.add(new JLabel("日付（yyyy-MM-dd）:"));
		//		inputPanel.add(dateField);
		inputPanel.add(dateSpinner);

		inputPanel.add(new JLabel("距離（km）:"));
		inputPanel.add(distanceField);

		inputPanel.add(new JLabel("時間（分）:"));
		inputPanel.add(durationField);

		inputPanel.add(new JLabel("歩数:"));
		inputPanel.add(stepsField);

		inputPanel.add(new JLabel("時間帯:"));
		inputPanel.add(timeSlotCombo);

		inputPanel.add(new JLabel("天候:"));
		inputPanel.add(weatherPanel);

		inputPanel.add(new JLabel("メモ:"));
		inputPanel.add(memoField);

		inputPanel.add(new JLabel(""));
		inputPanel.add(addButton); // ボタン配置

		add(inputPanel, BorderLayout.CENTER);
	}
}
