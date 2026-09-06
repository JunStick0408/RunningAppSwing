package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
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

	// 編集ボタン
	public JButton editButton = new JButton("編集する");

	// カラム名
	public String[] columnNames = { "日付", "距離", "時間", "歩数", "時間帯", "天候", "メモ" };

	// データの管理モデルを作成
	// columnNamesの要素数6で、列数がここで決まる
	public DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			return false; // 全てのセルを直接編集不可にする。 オーバーライドしないと個別編集が優先されて編集画面が開かない
		}
	}; // model コントローラーのupdateメソッドでこのモデル内にaddされる

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
		JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5)); // ボタンの間隔や並びの設定
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // トップパネルひとかたまりでの余白

		// ボタンのサイズを統一
		Dimension buttonSize = new Dimension(150, 30);
		openDialogButton.setPreferredSize(buttonSize);
		deleteButton.setPreferredSize(buttonSize);
		editButton.setPreferredSize(buttonSize);

		// 左側に新規追加画面ボタン
		topPanel.add(openDialogButton);
		topPanel.add(deleteButton);
		topPanel.add(editButton);

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
