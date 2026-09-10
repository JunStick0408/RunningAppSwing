package view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;

public class RunningFrame extends JFrame { // ← extends JFrame を書いた時点で、RunningFrame クラス自身が1つのウィンドウ枠そのものになっています。

	//	入力用のテキストフィールド（Contolollerから値を読み取る為にpublicにしている）
	public JTextField idField = new JTextField(5);

	//	新規登録ダイアログを開くボタン
	public JButton openDialogButton = new JButton("追加する");

	// 削除用エリアのパーツ
	public JButton deleteButton = new JButton("削除する");

	// 編集ボタン
	public JButton editButton = new JButton("編集する");

	// 複写ボタン
	public JButton copyButton = new JButton("複写する");

	// 1キロペース
	public JLabel paceLabel = new JLabel("1キロペース: - （分/km）"); // これは仮置きの文字でコントローラー側で上書きする
	public JLabel distanceLabel = new JLabel("月平均距離: - （km/月）");
	public JLabel caloriesLabel = new JLabel("累計消費カロリー: - （kcal）");
	public JLabel timeLabel = new JLabel("通算走行時間: - （時間）");

	// 体重入力欄
	public JLabel weightLabel = new JLabel("ここに体重を入力して下さい:");
	public JTextField weightField = new JTextField("60", 5); // 5文字分の幅を確保

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
		Dimension buttonSize = new Dimension(100, 30);
		openDialogButton.setPreferredSize(buttonSize);
		deleteButton.setPreferredSize(buttonSize);
		editButton.setPreferredSize(buttonSize);
		copyButton.setPreferredSize(buttonSize);

		// 左側に新規追加画面ボタン
		topPanel.add(openDialogButton);
		topPanel.add(editButton);
		topPanel.add(copyButton);
		topPanel.add(deleteButton);

		// 上部に配置
		add(topPanel, BorderLayout.NORTH);

		// 3.統計ラベルの追加
		// 大枠
		JPanel summaryPanel = new JPanel(new BorderLayout());
		Border emptyBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10); // Borderは枠線だけでなく、コンポーネント周りのタイトル、余白も担う
		Border titleBorder = BorderFactory.createTitledBorder("集計サマリー");
		summaryPanel.setBorder(BorderFactory.createCompoundBorder(titleBorder, emptyBorder));

		// 出力部分
		JPanel outputPanel = new JPanel(new GridLayout(4, 1, 10, 10));
		outputPanel.add(paceLabel);
		outputPanel.add(distanceLabel);
		outputPanel.add(caloriesLabel);
		outputPanel.add(timeLabel);
		summaryPanel.add(outputPanel, BorderLayout.WEST);

		// 体重入力部分
		JPanel weightInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0)); // 横並びで左詰め、左右間隔0
		weightInputPanel.add(weightLabel);
		weightInputPanel.add(weightField);
		weightInputPanel.add(new JLabel("kg"));

		JPanel rightContainer = new JPanel(new GridBagLayout()); //GridBagLayoutに部品を1つだけ入れると高さが真ん中に配置される
		rightContainer.add(weightInputPanel);
		summaryPanel.add(rightContainer, BorderLayout.EAST);

		// 出力部分、入力部分のパネルを親に配置
		add(summaryPanel, BorderLayout.SOUTH);

		// 4.走行履歴表示エリア（画面の中央：CENTERに配置）
		// 単にrecordTableだけを配置するとタイトルが表示されないし、スクロールバーが出ないのでセットで包む
		JScrollPane scrollpane = new JScrollPane(recordTable);
		scrollpane.setBorder(BorderFactory.createTitledBorder("走行履歴"));

		//　サイズ: NORTH（および他の方角）が占有した残りのエリア全体をすべて埋めるように自動拡大されます。
		add(scrollpane, BorderLayout.CENTER); // ← add(...) と書くだけで、自分（ウィンドウ枠）の中にパーツを配置できます。

	}

}
