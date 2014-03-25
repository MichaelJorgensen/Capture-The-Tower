package com.mike101102.ctt.gameapi.sql.options;

import java.io.File;

/**
 * Used to store SQLite options for connecting to the database
 * 
 * @author mike101102
 */
public class SQLiteOptions implements DatabaseOptions {

    private File file;

    public SQLiteOptions(File file) {
        this.file = file;
    }

    public File getSQLFile() {
        return file;
    }
}
