package model;

public enum Weather {
	/* これの略した書き方。コンストラクタで呼び出される。
	 * public static final Weather SUNNY  = new Weather(1, "晴れ");
	 * public static final Weather CLOUDY = new Weather(2, "曇り");
	 * public static final Weather RAINY  = new Weather(3, "雨");
	 */
	SUNNY(1, "晴れ"), CLOUDY(2, "曇り"), RAINY(3, "雨");

	// finalがあると読み取り専用でセットも出来ない
	private final int code;
	private final String displayLabel;

	// コンストラクタが3かい呼ばれる。
	// 「あらかじめ決められた数（今回は3つ）のインスタンスだけを作って保持しておく特殊なクラス」というのが Enum の正体です。
	private Weather(int code, String displayLabel) {
		this.code = code;
		this.displayLabel = displayLabel;
	}

	// getterのみ
	public int getCode() {
		return code;
	}

	// getterのみ
	public String getDisplayLabel() {
		return displayLabel;
	}

	// DBから数値がかえって来た時に、Enumのどれと一致か調べるメソッド
	public static Weather getByCode(int code) {
		// Weather.values():すべての選択肢（SUNNY, CLOUDY, RAINY）を全種類テーブルの上に並べます。
		for (Weather w : Weather.values()) { // values()メソッドを使うことで、[Weather.SUNNY, Weather.CLOUDY, Weather.RAINY] という Weather[] 型の配列が取得可能
			// 現在の値が探しているcodeと一緒ならそれを返す
			if (w.getCode() == code) {
				return w;
			}
		}
		return SUNNY;
	}

	@Override
	public String toString() {
		// TODO 自動生成されたメソッド・スタブ
		return displayLabel;
	}

}

// 1個のWeatherは1個の定数のことを指す。