package com.vladkrutlekto.notepad.utilities;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.vladkrutlekto.notepad.objects.Note;

@Database(entities = {Note.class}, version = 1)
public abstract class NoteDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();

}
