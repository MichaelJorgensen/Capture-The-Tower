package com.mike101102.ctt.gameapi.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;

import com.mike101102.ctt.CTT;
import com.mike101102.ctt.gameapi.sql.options.DatabaseOptions;
import com.mike101102.ctt.gameapi.sql.options.MySQLOptions;
import com.mike101102.ctt.gameapi.sql.options.SQLiteOptions;

/**
 * This class is for easily accessing a MySQL or SQLite database
 * 
 * @author mike101102
 */
public class SQL {

    private DatabaseOptions dop;
    private Connection con;

    /**
     * Represents an SQL connection with the given DatabaseOptions, MySQL or
     * SQLite
     * 
     * @param plugin that is using this
     * @param dop DatabaseOptions to use for connecting
     */
    public SQL(JavaPlugin plugin, DatabaseOptions dop) {
        this.dop = dop;
        plugin.getDataFolder().mkdirs();

        if (dop instanceof SQLiteOptions) {
            try {
                ((SQLiteOptions) dop).getSQLFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the DatabaseOptions for connection to the database
     * 
     * @return DatabaseOptions used for connecting to the database
     */
    public DatabaseOptions getDatabaseOptions() {
        return dop;
    }

    /**
     * Returns the connection used for most SQL methods
     * 
     * @return Connection used
     */
    public Connection getConnection() {
        return con;
    }

    /**
     * Opens the connection with the database based on the DatabaseOptions. Will
     * check for org.sqlite.JDBC and will return false and set the connection to
     * null if not found
     * 
     * @return true if the connection was established, otherwise false
     * @throws SQLException
     */
    public boolean open() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            con = null;
            return false;
        }
        if (dop instanceof MySQLOptions) {
            this.con = DriverManager.getConnection("jdbc:mysql://" + ((MySQLOptions) dop).getHostname() + ":" + ((MySQLOptions) dop).getPort() + "/" + ((MySQLOptions) dop).getDatabase(), ((MySQLOptions) dop).getUsername(), ((MySQLOptions) dop).getPassword());
            return true;
        } else if (dop instanceof SQLiteOptions) {
            this.con = DriverManager.getConnection("jdbc:sqlite:" + ((SQLiteOptions) dop).getSQLFile().getAbsolutePath());
            return true;
        } else {
            return false;
        }
    }

    /**
     * Closes the connection with the database
     * 
     * @throws SQLException
     */
    public void close() throws SQLException {
        con.close();
    }

    /**
     * Reloads the connection with the database. Equivalent to calling close()
     * and then open()
     * 
     * @return true if the connection re-opened successfully, otherwise false
     */
    public boolean reload() {
        try {
            close();
            return open();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Sends the given query to the database. Will execute update or execute
     * query based on keywords in the query The keywords for executeUpdate() to
     * be called are
     * <ul>
     * <li>delete
     * <li>update
     * <li>insert
     * </ul>
     * 
     * @param query to send to the database
     * @return ResultSet, will be null if executeUpdate() is used (based on
     *         keywords above), otherwise will be equal to executeQuery(query)
     * @throws SQLException
     */
    public ResultSet query(String query) throws SQLException {
        Statement st = null;
        ResultSet rs = null;
        st = con.createStatement();
        CTT.debug("Query: " + query);
        if (query.toLowerCase().contains("delete") || query.toLowerCase().contains("update") || query.toLowerCase().contains("insert")) {
            st.executeUpdate(query);
            return rs;
        } else {
            rs = st.executeQuery(query);
            return rs;
        }
    }

    /**
     * Sends the given query to the database using execute()
     * 
     * @param table
     * @return {@link java.sql.Statement.execute(String sql)}
     * @throws SQLException
     */
    public boolean createTable(String table) throws SQLException {
        Statement st = con.createStatement();
        CTT.debug("Create Table Query: " + table);
        return st.execute(table);
    }
}
