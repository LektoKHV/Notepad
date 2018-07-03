package com.vladkrutlekto.notepad.utilities;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.vladkrutlekto.notepad.objects.Note;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface NoteDao {
    @Query("SELECT * FROM notes")
    Flowable<List<Note>> getAll();

    @Query("SELECT * FROM notes WHERE name LIKE :name LIMIT 1")
    Note findByName(String name);

    @Insert
    void insertNote(Note note);

    @Delete
    void delete(Note note);

    @Query("DELETE FROM notes")
    void deleteAll();

    @Update
    void updateNote(Note note);
}
