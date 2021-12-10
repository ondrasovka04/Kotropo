package cz.ucenislovicek.BakalariAPI.schoolsDatabase;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {SchoolInfo.class}, version = 1, exportSchema = false)
public abstract class SchoolsDatabase extends RoomDatabase {
    public abstract SchoolDAO schoolDAO();
}
