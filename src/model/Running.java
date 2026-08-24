package model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

/**
 * ランニング記録モデルクラス（JavaBeans）
 * セッション保存やデータ転送を可能にするため Serializable を実装
 */
public class Running implements Serializable {

	private int id;
	private BigDecimal distance;
	private int duration;
	private int steps;
	private String memo;
	private Date runDate;

	//	引数ありコンストラクタを作ったら、セットでデフォルトコンストラクタも書いておく
	public Running() {

	}

	//	引数ありコンストラクタで初期化処理
	public Running(int id, BigDecimal distance, int duration, int steps, String memo, Date runDate) {
		super();
		this.id = id;
		this.distance = distance;
		this.duration = duration;
		this.steps = steps;
		this.memo = memo;
		this.runDate = runDate;
	}

	//	ゲッター、セッター
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public BigDecimal getDistance() {
		return distance;
	}

	public void setDistance(BigDecimal distance) {
		this.distance = distance;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public int getSteps() {
		return steps;
	}

	public void setSteps(int steps) {
		this.steps = steps;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public Date getRunDate() {
		return runDate;
	}

	public void setRunDate(Date runDate) {
		this.runDate = runDate;
	}

}
