package net.sehales.scservercmds;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sehales.secon.SeCon;
import net.sehales.secon.exception.DatabaseException;
import net.sehales.secon.obj.DatabaseResult;
import net.sehales.secon.utils.Database;

public final class BanHandler {

	private static Database db;
	static {
		try {
			db = SeCon.getAPI().getDatabaseUtils().getDefault();
		} catch (Exception e) {
			SeCon.getAPI().getLogger().severe("Server-Cmd-Collection", "Critical MySQL Error!");
			e.printStackTrace();
		}
	}

	public static boolean ban(String playerName, String executorName, String reason) {
		return ban0(playerName, executorName, reason, -1);
	}

	private static boolean ban0(String playerName, String executorName, String reason, long timestamp) {
		if (!isBanned(playerName)) {
			String sql = "";
			if (timestamp < 0l) {
				sql = "INSERT INTO " + getTableName() + "(`name`, `executorname`, `reason`, `tempban`, `bantime`) VALUES (?, ?, ?, ?, ?);";
				return db.write(sql, playerName, executorName, reason, 0, System.currentTimeMillis());
			} else {
				sql = "INSERT INTO " + getTableName() + "(`name`, `executorname`, `reason`, `tempban`, `bantime`, `endtime`) VALUES (?, ?, ?, ?, ?, ?);";
				return db.write(sql, playerName, executorName, reason, 1, System.currentTimeMillis(), System.currentTimeMillis() + timestamp);
			}
		} else
			return false;

	}

	public static BanInfo getBanInfo(String playerName) {
		String sql = "SELECT * FROM " + getTableName() + " WHERE name = ?;";
		DatabaseResult query = db.readEnhanced(sql, playerName);
		if (query != null && query.hasRows())
			try {
				String reason = query.getString(0, "reason");
				String executorName = query.getString(0, "executorname");
				boolean tmpban = false;
				Date endDate;
				if (query.getInteger(0, "tempban") == 1) {
					tmpban = true;
					endDate = new Date(query.getLong(0, "endtime"));
				} else
					endDate = new Date();
				Date banDate = new Date(query.getLong(0, "bantime"));
				return new BanInfo(playerName, executorName, reason, tmpban, banDate, endDate);
			} catch (Exception e) {
				return null;
			}
		return null;
	}

	public static String getTableName() {
		try {
			return SeCon.getAPI().getDatabaseUtils().getDefault().tableName("banlist");
		} catch (DatabaseException e) {
			e.printStackTrace();
			return null;
		}
	}

	static void init() throws DatabaseException {
		SeCon.getAPI().getDatabaseUtils().getDefault().write("CREATE TABLE IF NOT EXISTS " + getTableName() + "(" +

		"`id` INT AUTO_INCREMENT," +

		"`name` VARCHAR(16) NOT NULL ," +

		"`executorname` VARCHAR(16) NOT NULL ," +

		"`reason` VARCHAR(1000) NULL ," +

		"`bantime` BIGINT NOT NULL ," +

		"`tempban` INT NOT NULL DEFAULT 0 ," +

		"`endtime` BIGINT NULL ," +

		"PRIMARY KEY (`id`, `name`) ," +

		"UNIQUE INDEX `id_UNIQUE` (`id` ASC) ," +

		"UNIQUE INDEX `name_UNIQUE` (`name` ASC) )" +

		"ENGINE = MyISAM " +

		"DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;");
	}

	public static boolean isBanned(String playerName) {
		String sql = "SELECT * FROM " + getTableName() + " WHERE name = ?;";
		DatabaseResult query = null;
		query = db.readEnhanced(sql, playerName);
		if (query != null && query.hasRows())
			return true;
		return false;
	}

	public static List<BanInfo> listBans() {
		String sql = "SELECT * FROM " + getTableName() + ";";
		List<BanInfo> players = new ArrayList<BanInfo>();
		DatabaseResult query = null;
		query = db.readEnhanced(sql);
		if (query != null && query.hasRows())
			for (int i = 0; i < query.rowCount(); i++)
				try {
					String playerName = query.getString(i, "name");
					String executorName = query.getString(i, "executorname");
					String reason = query.getString(i, "reason");
					boolean tmpban = false;
					Date endDate;
					if (query.getInteger(i, "tempban") == 1) {
						tmpban = true;
						endDate = new Date(query.getLong(i, "endtime"));
					} else
						endDate = new Date();
					Date banDate = new Date(query.getLong(i, "bantime"));
					players.add(new BanInfo(playerName, executorName, reason, tmpban, banDate, endDate));
				} catch (Exception e) {
				}
		if (players.size() > 0)
			return players;
		return null;
	}

	public static boolean tempban(String playerName, String executorName, String reason, long timestamp) {
		return ban0(playerName, executorName, reason, timestamp);
	}

	public static void unban(String playerName) {
		String sql = "DELETE FROM " + getTableName() + " WHERE name = ?;";
		db.write(sql, playerName);
	}
}
