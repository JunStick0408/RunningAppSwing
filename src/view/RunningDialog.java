package view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RunningDialog extends JDialog {

	// 入力フィールド（Controllerから参照できるよう public にしています）
	// public を消すとパッケージプライベート（無指定）という状態になるので、そのパッケージでしか使えなくなる。
	public JTextField idField = new JTextField();
	public JTextField distanceField = new JTextField();
	public JTextField durationField = new JTextField();
	public JTextField stepsField = new JTextField();
	public JTextField memoField = new JTextField();
	public JTextField dateField = new JTextField("2026-08-21"); // 初期値は今日の日付

	// ボタン
	public JButton addButton = new JButton("登録");

	// このダイアログ（子画面）を呼び出すときに、親画面である RunningFrame（JFrame）を渡してもらうための引数として定義
	// コンストラクタなのでメイン画面の新規ボタンが押されたタイミングでnewしてコンストラクタ起動
	public RunningDialog(Frame owner) {
		// 親画面（owner）、タイトル文字列（title）、モーダルにするか（modal = true）の3つを渡す型を選んで書いています。
		// superを省略すると引数無しsuper()になる
		// でも引数無しだと何の情報か分かんないからsuper(owner, title, modal)を書くのが定石
		super(owner, "新規記録の登録", true);
		setSize(350, 300);
		setLocationRelativeTo(owner);
		// レイアウトマネージャー（画面上の部品の並べ方を管理するオブジェクト）を new で生成しています。
		/* 1. 変数を作って new する
		   BorderLayout layout = new BorderLayout(10, 10); これはコンポーネント同士の隙間
		   2. その変数を渡す
		   setLayout(layout);
		 */
		setLayout(new BorderLayout(10, 10));

		JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5)); // 6行2列で幅が5
		inputPanel.setBorder(BorderFactory.createTitledBorder("データの入力"));

		inputPanel.add(new JLabel("日付（YYYY-MM-DD）:"));
		inputPanel.add(dateField);

		inputPanel.add(new JLabel("距離（km）:"));
		inputPanel.add(distanceField);

		inputPanel.add(new JLabel("時間（分）:"));
		inputPanel.add(durationField);

		inputPanel.add(new JLabel("歩数:"));
		inputPanel.add(stepsField);

		inputPanel.add(new JLabel("メモ:"));
		inputPanel.add(memoField);

		inputPanel.add(new JLabel(""));
		inputPanel.add(addButton); // ボタン配置

		add(inputPanel, BorderLayout.CENTER);
	}
}
