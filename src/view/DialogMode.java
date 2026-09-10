package view;

public enum DialogMode {
	// この時点でコンストラクタの引数に渡される	
	// title="新規登録", buttonText="登録" で作成される
	NEW("新規登録", "登録"), EDIT("編集画面", "更新"), COPY("複写画面", "複写");

	private final String title;
	private final String buttonText;

	// カッコ内の値を受け取って、privateな変数に保持するコンストラクタ
	private DialogMode(String title, String buttonText) {
		this.title = title;
		this.buttonText = buttonText;
	}

	public String getTitle() {
		return title;
	}

	public String getButtonText() {
		return buttonText;
	}

}
