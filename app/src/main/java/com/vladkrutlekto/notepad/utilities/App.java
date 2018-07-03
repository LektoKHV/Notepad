package com.vladkrutlekto.notepad.utilities;

import android.app.Application;
import android.arch.persistence.room.Room;

public class App extends Application {

    public static App instance;

    private NoteDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, NoteDatabase.class, "database")
                .build();
    }

    public static App getInstance() {
        return instance;
    }

    public NoteDatabase getDatabase() {
        return database;
    }
}