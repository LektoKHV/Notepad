package com.vladkrutlekto.notepad;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.vladkrutlekto.notepad.adapters.NoteRecyclerViewAdapter;
import com.vladkrutlekto.notepad.objects.Note;
import com.vladkrutlekto.notepad.utilities.DatabaseHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NoteListActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NoteListActivity";

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm", Locale.ENGLISH);

    static DatabaseHelper databaseHelper;
    private SQLiteDatabase db;

    private boolean isTwoPane; // tablet (true) or phone (false)
    private ArrayList<Note> noteList = new ArrayList<>();

    private ImageButton addButton, deleteButton, deleteAllButton;
    private View recyclerView; // It can be LinearLayout or RecyclerView, depends on device type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getResources().getString(R.string.screen_note_list));

        if (findViewById(R.id.note_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            isTwoPane = true;
        }

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(this);
        deleteAllButton = findViewById(R.id.delete_all_button);
        deleteAllButton.setOnClickListener(this);
        recyclerView = findViewById(R.id.note_list);

        databaseHelper = new DatabaseHelper(NoteListActivity.this);
        db = databaseHelper.getReadableDatabase();
    }

    @Override
    protected void onResume() {
        super.onResume();

        noteList.clear();
        Cursor query = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE + ";", null);
        if (query.moveToFirst()) {
            do {
                try {
                    Note note = new Note(query.getString(1),
                            query.getString(2),
                            simpleDateFormat.parse(query.getString(3)));

                    noteList.add(note);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } while (query.moveToNext());
        }
        query.close();

//        noteList.add(new Note("Note1", "That's interesting story, dude", new Date()));
//        noteList.add(new Note("Note2", "That's boring story, dude", new Date()));
//        noteList.add(new Note("Note3", "What are you even talking about", new Date()));
//        noteList.add(new Note("Note4", "Just another line in list", new Date()));
//        noteList.add(new Note("Note5", "I like number 5!", new Date()));
//        noteList.add(new Note("Note6", "Don't attend two more 6, for God's sake!", new Date()));
//        noteList.add(new Note("Note7", "We are safe now", new Date()));
//        noteList.add(new Note("Note8", "Does it ever end?", new Date()));
//        noteList.add(new Note("Note9", "I'm tired of this, bye!", new Date()));
//        noteList.add(new Note("Note10", "Sorry, I'm here just for tenth note. It looks great! Now really goodbye, man!", new Date()));

        setupRecyclerView((RecyclerView) recyclerView);
    }

    private void setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        final NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(this, noteList, isTwoPane);

        adapter.setOnItemClickListener(new NoteRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (view.isSelected()) view.setSelected(false);

                // To NoteDetailFragment (edit) [START]
                Note note = (Note) view.getTag(); // Get object by tag
                if (adapter.isTwoPane()) { // Tablet
                    NoteDetailFragment editFragment = new NoteDetailFragment();
                    Bundle arguments = new Bundle();
                    arguments.putInt("mode", NoteDetailFragment.EDIT_MODE);
                    arguments.putSerializable("note", note);
                    editFragment.setArguments(arguments);

                    NoteListActivity.this.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.note_detail_container, editFragment)
                            .commit();
                } else { // Phone
                    Context context = view.getContext();
                    Intent intent = new Intent(context, NoteDetailActivity.class);
                    intent.putExtra("mode", NoteDetailFragment.EDIT_MODE);
                    intent.putExtra("note", note);

                    context.startActivity(intent);
                }
                // To NoteDetailFragment (edit) [END]
            }
        });

        adapter.setOnItemLongClickListener(new NoteRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onLongItemClick(View view, int position) {
                Log.d(TAG, view.toString());
                view.setSelected(!view.isSelected());
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.add_button) {
            if (isTwoPane) {
                NoteDetailFragment editFragment = new NoteDetailFragment();
                Bundle arguments = new Bundle();
                arguments.putInt("mode", NoteDetailFragment.ADD_MODE);
                editFragment.setArguments(arguments);

                NoteListActivity.this.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.note_detail_container, editFragment)
                        .commit();
            } else {
                Intent intent = new Intent(NoteListActivity.this, NoteDetailActivity.class);
                intent.putExtra("mode", NoteDetailFragment.ADD_MODE);

                startActivity(intent);
            }
        } else if (id == R.id.delete_button) {

        } else if (id == R.id.delete_all_button) {

        }
    }

    private void updateUI(int size) {
        if (size == 0) {
            setVisibility(true, addButton, deleteAllButton);
            setVisibility(false, deleteButton);
        } else {
            setVisibility(false, addButton, deleteAllButton);
            setVisibility(true, deleteButton);
        }
    }

    private void setVisibility(boolean visible, View... views) {
        for (View view : views) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
