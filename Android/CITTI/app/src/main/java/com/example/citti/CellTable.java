package com.example.citti;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

@Database(entities = {BS.class}, version=1, exportSchema = false)
public abstract class CellTable extends RoomDatabase {
    private static final String DB_NAME = "CellTable.db";
    private static volatile CellTable instance;

    static synchronized CellTable getInstance(Context context){
        if(instance==null){
            instance = create(context);
        }
        return instance;
    }

    private static CellTable create(final Context context){
        return Room.databaseBuilder(context, CellTable.class, DB_NAME).allowMainThreadQueries().build();
    }

    public abstract BSDao getBSDao();

    @NonNull
    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config){
        return null;
    }

    @NonNull
    @Override
    protected InvalidationTracker createInvalidationTracker(){
        return null;
    }

    @Override
    public void clearAllTables(){

    }

}
