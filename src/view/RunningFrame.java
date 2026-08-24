package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public class RunningFrame extends JFrame { // ← extends JFrame を書いた時点で、RunningFrame クラス自身が1つのウィンドウ枠そのものになっています。

	//	入力用のテキストフィールド（Contolollerから値を読み取る為にpublicにしている）
	public JTextField disrtanceField = new JTextField();
	public JTextField durationField = new JTextField();
	public JTextField stepsField = new JTextField();
	public JTextField memoField = new JTextField();
	public JTextField dateField = new JTextField("2026-08-21"); // 初期値は今日の日付

	//	ボタン一覧表示用の共通コンポーネント
	public JButton addButton = new JButton("記録を追加する");
	public DefaultListModel<String> ListModel = new DefaultListModel<>(); // model
	public JList<String> recordList = new JList<>(ListModel); // 画面に表示される部分。modelのセットで画面にmodelを映せる。

	public RunningFrame() {
		// 1. ウィンドウ全体の基本設定
		setTitle("ランニング記録アプリ");
		setSize(500, 450);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // ×で閉じる処理
		setLocationRelativeTo(null); // 画面中央に配置
		setLayout(new BorderLayout(10, 10)); // 部品同士を10ずつ空ける

		// 2. 入力フォームエリア（画面の上側：NORTH に配置）
		JPanel inputPanel = new JPanel(new GridLayout(6, 2, 5, 5)); // 6列2行で幅が5
		//	setBorder入力エリアに枠線を引く。
		//	BorderFactory.createTitledBorder「新規記録の入力」という見出し文字列が枠線の一部に埋め込まれた、綺麗なタイトル付き枠線を簡単に作成してくれる
		inputPanel.setBorder(BorderFactory.createTitledBorder("新規記録の入力"));

		inputPanel.add(new JLabel("日付（YYYY-MM-DD）:"));
		inputPanel.add(dateField);

		inputPanel.add(new JLabel("距離（km）:"));
		inputPanel.add(disrtanceField);

		inputPanel.add(new JLabel("時間（分）:"));
		inputPanel.add(durationField);

		inputPanel.add(new JLabel("歩数:"));
		inputPanel.add(stepsField);

		inputPanel.add(new JLabel("メモ:"));
		inputPanel.add(memoField);

		inputPanel.add(addButton); // ボタン配置

		//　上部に固定（高さは部品のサイズに合わせる）横幅はいっぱいに自動で広がります。		
		add(inputPanel, BorderLayout.NORTH); // 上部に配置

		//	3.走行履歴表示エリア（画面の中央：CENTERに配置）
		JScrollPane scrollpane = new JScrollPane(recordList);
		scrollpane.setBorder(BorderFactory.createTitledBorder("走行履歴"));

		//　サイズ: NORTH（および他の方角）が占有した残りのエリア全体をすべて埋めるように自動拡大されます。
		add(scrollpane, BorderLayout.CENTER); // ← add(...) と書くだけで、自分（ウィンドウ枠）の中にパーツを配置できます。

	}

}
