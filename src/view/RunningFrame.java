package view;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class RunningFrame extends JFrame { // ← extends JFrame を書いた時点で、RunningFrame クラス自身が1つのウィンドウ枠そのものになっています。

	//	入力用のテキストフィールド（Contolollerから値を読み取る為にpublicにしている）
	public JTextField idField = new JTextField(5);

	//	新規登録ダイアログを開くボタン
	public JButton openDialogButton = new JButton("新規記録を追加する");

	// 削除用エリアのパーツ
	public JButton deleteButton = new JButton("削除する");

	// カラム名
	public String[] columnNames = { "日付", "距離", "時間", "歩数", "メモ", };

	// データの管理モデルを作成
	// columnNamesの要素数6で、列数がここで決まる
	public DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0); // model コントローラーのupdateメソッドでこのモデル内にaddされる

	// テーブル本体を作る
	public JTable recordTable = new JTable(tableModel);

	public RunningFrame() {
		// 1. ウィンドウ全体の基本設定
		setTitle("ランニング記録アプリ");
		setSize(600, 550);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ×で閉じる処理
		setLocationRelativeTo(null); // 画面中央に配置
		setLayout(new BorderLayout(10, 10)); // 部品同士を10ずつ空ける 内部コンポのみ

		// 2.操作エリア（画面上部：NORTHに配置）
		JPanel topPanel = new JPanel(new BorderLayout(10, 10)); // こいつから見た内部のWEST EASTの間隔のこと
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10)); // コンポの内部間隔

		// 左側に新規追加画面ボタン
		topPanel.add(openDialogButton, BorderLayout.WEST);

		// 右側に削除ボタン topPanel.add～では1機能しか配置できないので、ID記載＆ボタンのため新規コンポ作成
		// JPanel はデフォルトで FlowLayout（横並び）になるため、
		// ラベル・入力欄・ボタンの3つが左から順に横1列で並ぶ
		JPanel deletePanel = new JPanel();
		deletePanel.add(new JLabel("削除対象ID："));
		deletePanel.add(idField);
		deletePanel.add(deleteButton);
		topPanel.add(deletePanel, BorderLayout.EAST);

		// 上部に配置
		add(topPanel, BorderLayout.NORTH);

		//	3.走行履歴表示エリア（画面の中央：CENTERに配置）
		// 単にrecordTableだけを配置するとタイトルが表示されないし、スクロールバーが出ないのでセットで包む
		JScrollPane scrollpane = new JScrollPane(recordTable);
		scrollpane.setBorder(BorderFactory.createTitledBorder("走行履歴"));

		//　サイズ: NORTH（および他の方角）が占有した残りのエリア全体をすべて埋めるように自動拡大されます。
		add(scrollpane, BorderLayout.CENTER); // ← add(...) と書くだけで、自分（ウィンドウ枠）の中にパーツを配置できます。

	}

}
