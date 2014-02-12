
package net.sehales.scservercmds;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sehales.secon.db.Database;
import net.sehales.secon.exception.DatabaseException;

public final class BanHandler {
    
    private static Database db;
    private static PreparedStatement IS_BANNED, TEMPBAN, BAN, BAN_INFO, UNBAN;
    
    public static boolean ban(String playerName, String executorName, String reason) {
        return ban0(playerName, executorName, reason, -1);
    }
    
    private static boolean ban0(String playerName, String executorName, String reason, long banTime) {
        try {
            PreparedStatement stmt = null;
            if (banTime < 0l) {
                // sql = "INSERT INTO " + getTable() +
                // "(`name`, `executorname`, `reason`, `tempban`, `bantime`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `tempban` = `tempban`;";
                // return db.write(sql, playerName, executorName, reason, 0,
                // System.currentTimeMillis());
                stmt = BAN;
                stmt.clearParameters();
                stmt.setString(1, playerName);
                stmt.setString(2, executorName);
                ;
                stmt.setString(3, reason);
                stmt.setInt(4, 0);
                stmt.setLong(5, System.currentTimeMillis());
            } else {
                // sql = "INSERT INTO " + getTable() +
                // "(`name`, `executorname`, `reason`, `tempban`, `bantime`, `endtime`) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `tempban` = `tempban`;";
                // return db.write(sql, playerName, executorName, reason, 1,
                // System.currentTimeMillis(), System.currentTimeMillis() +
                // timestamp);
                stmt = TEMPBAN;
                stmt.clearParameters();
                stmt.setString(1, playerName);
                stmt.setString(2, executorName);
                stmt.setString(3, reason);
                stmt.setInt(4, 0);
                stmt.setLong(5, System.currentTimeMillis());
                stmt.setLong(6, System.currentTimeMillis() + banTime);
            }
            
            synchronized (db.getConnection()) {
                stmt.execute();
            }
            
            return stmt.getUpdateCount() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
        
    }
    
    public static BanInfo getBanInfo(String playerName) {
        ResultSet query = null;
        try {
            BAN_INFO.clearParameters();
            BAN_INFO.setString(1, playerName);
            synchronized (db.getConnection()) {
                query = BAN_INFO.executeQuery();
            }
            if (query != null && query.next()) {
                String reason = query.getString("reason");
                String executorName = query.getString("executorname");
                boolean tmpban = false;
                Date endDate;
                
                if (query.getInt("tempban") == 1) {
                    tmpban = true;
                    endDate = new Date(query.getLong("endtime"));
                } else {
                    endDate = new Date();
                }
                
                Date banDate = new Date(query.getLong("bantime"));
                return new BanInfo(playerName, executorName, reason, tmpban, banDate, endDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static String getTable() {
        return String.format("`%s`.%s", db.getDatabaseName(), db.getFormattedTableName("banlist"));
    }
    
    static void init(Database database) throws DatabaseException {
        BanHandler.db = database;
        db.execute("CREATE TABLE IF NOT EXISTS " + getTable() + "(" +
        
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
        
        try {
            synchronized (db.getConnection()) {
                IS_BANNED = db.getConnection().prepareStatement("SELECT * FROM " + getTable() + " WHERE name = ?;");
                BAN = db.getConnection().prepareStatement("INSERT INTO " + getTable()
                                                          + "(`name`, `executorname`, `reason`, `tempban`, `bantime`) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `tempban` = `tempban`;");
                TEMPBAN = db.getConnection().prepareStatement("INSERT INTO "
                                                              + getTable()
                                                              + "(`name`, `executorname`, `reason`, `tempban`, `bantime`, `endtime`) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `tempban` = `tempban`;");
                BAN_INFO = db.getConnection().prepareStatement("SELECT * FROM " + getTable() + " WHERE name = ?;");
                UNBAN = db.getConnection().prepareStatement("DELETE FROM " + getTable() + " WHERE name = ?;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isBanned(String playerName) {
        try {
            IS_BANNED.clearParameters();
            IS_BANNED.setString(1, playerName);
            synchronized (db.getConnection()) {
                if (IS_BANNED.execute()) {
                    ResultSet query = IS_BANNED.getResultSet();
                    if (query != null && query.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static List<BanInfo> listBans() {
        List<BanInfo> players = new ArrayList<BanInfo>();
        ResultSet query = null;
        synchronized (db.getConnection()) {
            query = db.executeQuery("SELECT * FROM " + getTable() + ";");
        }
        
        try {
            if (query != null && query.next()) {
                do {
                    try {
                        String playerName = query.getString("name");
                        String executorName = query.getString("executorname");
                        String reason = query.getString("reason");
                        boolean tmpban = false;
                        Date endDate;
                        if (query.getInt("tempban") == 1) {
                            tmpban = true;
                            endDate = new Date(query.getLong("endtime"));
                        } else {
                            endDate = new Date();
                        }
                        Date banDate = new Date(query.getLong("bantime"));
                        players.add(new BanInfo(playerName, executorName, reason, tmpban, banDate, endDate));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } while (query.next());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }
    
    public static boolean tempban(String playerName, String executorName, String reason, long banTime) {
        return ban0(playerName, executorName, reason, banTime);
    }
    
    public static void unban(String playerName) {
        try {
            UNBAN.clearWarnings();
            UNBAN.setString(1, playerName);
            UNBAN.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }
}
