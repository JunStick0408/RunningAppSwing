package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

public class RunningEditDialog extends JDialog {

	//　入力フィールドの用意　コントローラーで使用するのでpublic
	public JTextField idField = new JTextField();
	public JTextField distanceField = new JTextField();
	public JTextField durationField = new JTextField();
	public JTextField stepsField = new JTextField();
	public JTextField memoField = new JTextField();

	// 「日付専用のモデル」を作成 初期日付、過去下限、未来上限、日単位変更可能
	SpinnerDateModel model = new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
	public JSpinner dateSpinner = new JSpinner(model);

	// ボタン
	public JButton updateButton = new JButton("更新");

	public RunningEditDialog(RunningFrame owner) {
		super(owner, "編集画面", true); // trueにすることで親画面を触れなくする
		setSize(350, 350);
		// ダイアログ自身のサイズ（幅・高さ）が決まっていないと真ん中の計算がズレてしまうため、必ず setSize(...) より後 に呼び出す必要があります。
		setLocationRelativeTo(owner); // 親画面に対して真ん中 nullだと画面に対して真ん中
		setLayout(new BorderLayout(10, 10));

		// 表示フォーマットを yyyy-MM-DD に指定 
		// JSpinner は数字やリストなど何でも表示できる汎用パーツなので、デフォルトのままだと時刻（時:分:秒）までダラダラと表示されてしまいます。
		JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
		// dateSpinnerの形式をeditorという形でセット！
		dateSpinner.setEditor(editor); // ただのメソッドはコンストラクタ内でしか書けない。

		JPanel inputPanel = new JPanel(new GridLayout(7, 2, 5, 5));
		inputPanel.setBorder(BorderFactory.createTitledBorder("データの編集"));

		idField.setEditable(false);
		inputPanel.add(new JLabel("id:"));
		inputPanel.add(idField);

		inputPanel.add(new JLabel("日付（yyyy-MM-dd）:"));
		inputPanel.add(dateSpinner);

		inputPanel.add(new JLabel("距離（km）:"));
		inputPanel.add(distanceField);

		inputPanel.add(new JLabel("時間（分）:"));
		inputPanel.add(durationField);

		inputPanel.add(new JLabel("歩数:"));
		inputPanel.add(stepsField);

		inputPanel.add(new JLabel("メモ:"));
		inputPanel.add(memoField);

		inputPanel.add(new JLabel(""));
		inputPanel.add(updateButton);

		add(inputPanel, BorderLayout.CENTER);

	}
}
