package model;

//　定義されたものしか選択出来ないので打ち間違いを防止できる。
public enum TimeSlot {
	// 定数：DBにはMORNINGが保存されるが、画面には「朝」として表示される
	// public static final TimeSlot MORNING = new TimeSlot("朝"); の省略形が書かれている。
	// TimeSlot("朝")がコンストラクタ呼び出しでセットされる。
	MORNING("朝"), DAYTIME("昼"), EVENING("夕方"), NIGHT("夜");

	// 朝とか昼とかの日本語を保持するもの（モデル）
	private final String displayLabel;

	// コンストラクタ：定数が読み込まれた時にそれをセットする
	TimeSlot(String displayLabel) {
		this.displayLabel = displayLabel;
	}

	// getter：日本語のラベルを取り出す
	public String getDisplayLabel() {
		return displayLabel;
	}

	// プルダウン（JcomboBox）等が自動で画面表示用に文字列を取り出す仕組み
	// toString()は全クラスにある、自身を文字列を返す仕組み。
	// オーバーライドしない状態だと定数名MORNINGが返ってきてしまう。
	// Swingの JComboBox（プルダウン）は、セットされたオブジェクトの toString() を自動的に呼び出して画面に表示します。
	@Override
	public String toString() {
		return displayLabel; // 画面には「朝」「昼」などの日本語を表示する
	}

}

//1個のTimeSlotは1個の定数のことを指す。