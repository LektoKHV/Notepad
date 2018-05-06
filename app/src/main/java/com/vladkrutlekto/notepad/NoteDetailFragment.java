package com.vladkrutlekto.notepad;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vladkrutlekto.notepad.objects.Note;
import com.vladkrutlekto.notepad.utilities.DatabaseHelper;

import java.util.Date;

public class NoteDetailFragment extends Fragment implements View.OnClickListener {

    private Note note;
    public static final int EDIT_MODE = 200,
            ADD_MODE = 201;
    private int mode;

    private SQLiteDatabase db;

    private TextView name, text;
    private ImageButton addButton, editButton;

    public NoteDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mode = getArguments().getInt("mode");

            // Check fragment mode
            if (mode == EDIT_MODE) {
                note = (Note) getArguments().getSerializable("note");

                if (getActivity()  instanceof NoteDetailActivity) {
                    ActionBar actionBar = ((NoteDetailActivity) getActivity()).getSupportActionBar();
                    Log.d("TAG", "actionBar == null? " + Boolean.toString(actionBar == null));
                    if (actionBar != null) actionBar.setTitle(getString(R.string.edit_note_title));
                }
            } else if (mode == ADD_MODE) {
                if (getActivity() instanceof NoteDetailActivity) {
                    ActionBar actionBar = ((NoteDetailActivity) getActivity()).getSupportActionBar();
                    if (actionBar != null) actionBar.setTitle(getString(R.string.add_note_title));
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_note_detail, container, false);

        name = rootView.findViewById(R.id.name);
        text = rootView.findViewById(R.id.text);

        addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        editButton = rootView.findViewById(R.id.edit_button);
        editButton.setOnClickListener(this);

        if (note != null) {
            name.setText(note.getName());
            text.setText(note.getText());
        }

        updateUI(mode);
        return rootView;
    }

    private void updateUI(int mode) {
        if (mode == EDIT_MODE) {
            addButton.setVisibility(View.GONE);
            editButton.setVisibility(View.VISIBLE);
        } else if (mode == ADD_MODE) {
            addButton.setVisibility(View.VISIBLE);
            editButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.add_button) {
            db = NoteListActivity.databaseHelper.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put("name", name.getText().toString());
            contentValues.put("text", text.getText().toString());
            contentValues.put("date", NoteListActivity.simpleDateFormat.format(new Date()));

            db.insert(DatabaseHelper.TABLE, null, contentValues);

            // Exit from this screen
            if (getActivity() != null) {
                if (getActivity() instanceof NoteListActivity) {
                    getActivity().getSupportFragmentManager().popBackStack();
                } else {
                    getActivity().finish();
                }
            }
        } else if (id == R.id.edit_button) {

        }
    }
}
