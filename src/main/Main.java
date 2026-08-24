package main;

import javax.swing.SwingUtilities;

import controller.RunningController;
import dao.RunningDao;
import dao.RunningDaoImpl;
import view.RunningFrame;

public class Main {

	public static void main(String[] args) {
		// SwingUtilities.invokeLater(...)画面の崩れや予期せぬ不具合を防ぐための、Swingにおけるお約束
		// 【匿名クラス】
		// 画面を作る処理は、Swing専用のスレッド（裏方処理）に任せる必要がある。
		// 一回しか使わない処理なので、わざわざ別ファイルを作らず「その場で使い捨てのクラス（new Runnable）」を作成している。
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// 1.各パーツのインスタンス化
				// RunningDao でnew できない理由
				// 中身（具体的な処理のコード）が空っぽなので、Javaは new RunningDao() と言われても「実体がないから作れません！」とエラー（Cannot instantiate the type RunningDao）を出してしまいます。
				RunningDao dao = new RunningDaoImpl();
				RunningFrame frame = new RunningFrame();
				RunningController controller = new RunningController(frame, dao);

				// 2.起動時にDBの既存データを一覧表示する
				controller.updateListView();

				// 3.ウィンドウを画面に表示する
				frame.setVisible(true);

			}
		});
	}

}
