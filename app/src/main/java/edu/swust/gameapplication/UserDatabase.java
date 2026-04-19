package edu.swust.gameapplication;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, GameRecord.class}, version = 2, exportSchema = false)
public abstract class UserDatabase extends RoomDatabase {
    public abstract UserDao getuserDao();
    public abstract GameRecordDao getGameRecordDao();
}