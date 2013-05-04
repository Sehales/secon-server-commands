package net.sehales.scservercmds;

import java.io.Serializable;
import java.util.Date;

public class BanInfo implements Serializable {

	private static final long serialVersionUID = 5790135099941701404L;
	private String            playerName;
	private boolean           tempban;
	private Date              endDate;
	private Date              banDate;
	private String            reason;
	private String            executorName;

	public BanInfo(String playerName, String executorName, String reason, boolean isTemban, Date banDate, Date endDate) {
		setPlayerName(playerName);
		setExecutorName(executorName);
		setReason(reason);
		setTempban(isTemban);
		setBanDate(banDate);
		setEndDate(endDate);
	}

	/**
	 * @return the banDate
	 */
	public Date getBanDate() {
		return banDate;
	}

	/**
	 * @return the endDate
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * 
	 * @return the name of the executor
	 */
	public String getExecutorName() {
		return this.executorName;
	}

	/**
	 * @return the playerName
	 */
	public String getPlayerName() {
		return playerName;
	}

	/**
	 * @return the reason
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @return the tempban
	 */
	public boolean isTempban() {
		return tempban;
	}

	/**
	 * @param banDate
	 *            the banDate to set
	 */
	private void setBanDate(Date banDate) {
		this.banDate = banDate;
	}

	/**
	 * @param endDate
	 *            the endDate to set
	 */
	private void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * 
	 * @param executorName
	 *            the executorName to set
	 */
	private void setExecutorName(String executorName) {
		this.executorName = executorName;
	}

	/**
	 * @param playerName
	 *            the playerName to set
	 */
	private void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	/**
	 * @param reason
	 *            the reason to set
	 */
	private void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @param tempban
	 *            the tempban to set
	 */
	private void setTempban(boolean tempban) {
		this.tempban = tempban;
	}

}
