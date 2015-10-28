package bac.database;

import bac.helper.Helper;

import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public final class Database {

    private static volatile JdbcConnectionPool cp;

    public static void init() {
        cp = JdbcConnectionPool.create("jdbc:h2:./bac-database/bac;DB_CLOSE_ON_EXIT=FALSE", "sa", "sa");
        CheckVersion();
    }

    public static void stop() {
        if (cp != null) {
            try (Connection con = cp.getConnection();
                Statement stmt = con.createStatement()) {
                stmt.execute("SHUTDOWN COMPACT");
                Helper.logMessage("Database stoped.");
            } catch (SQLException e) {
                Helper.logMessage("Database stop failed. ("+e.toString()+")");
            }
            cp = null;
        }
    }

    public static Connection GetConnection() throws SQLException {
        Connection con = cp.getConnection();
        con.setAutoCommit(false);
        return con;
    }

    static void CheckVersion() {
       try (Connection con = GetConnection(); Statement stmt = con.createStatement()) {
            int DBversion = 0;
            try {
                ResultSet rs = stmt.executeQuery("SELECT db_version FROM version");
                if (! rs.next()) {
                    throw new RuntimeException("Invalid version table");
                }
                DBversion = rs.getInt("db_version");
                if (! rs.isLast()) {
                    throw new RuntimeException("Invalid version table");
                }
                rs.close();
                Helper.logMessage("Database version " + DBversion);
            } catch (SQLException e) {
                Helper.logMessage("Initializing an empty database");
                stmt.executeUpdate("CREATE TABLE version (db_version INT NOT NULL)");
                stmt.executeUpdate("INSERT INTO version VALUES (0)");
                con.commit();
            }
            UpdateDB(DBversion);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }    
    }

    
    private static void ExecuteSQL(String sql, int NextVersion ) {
        try (Connection con = GetConnection(); Statement stmt = con.createStatement()) {
            try {
                if (sql != null) {
                    Helper.logMessage("Execute SQL:" + sql);
                    stmt.executeUpdate(sql);
                }
                if (NextVersion != 0) {
                    stmt.executeUpdate("UPDATE version SET db_version = "+NextVersion);
                    con.commit();
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error executing " + sql, e);
        }
    }

    private static void ExecuteSQL(String sql) {
       ExecuteSQL( sql, 0 );    
    }

    private static void UpdateDB(int DBversion) {
        switch (DBversion) {
            case 0: {
            	ExecuteSQL("CREATE TABLE IF NOT EXISTS peer (announcement VARCHAR PRIMARY KEY)",1);
            	return;
            }  
            case 1: {
            	return;
            } 
            default:
                throw new RuntimeException("Database inconsistent.");
        }
    } 
 
    private Database() {} // never

}