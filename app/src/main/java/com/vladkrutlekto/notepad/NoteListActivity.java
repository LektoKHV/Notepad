package com.vladkrutlekto.notepad;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.vladkrutlekto.notepad.adapters.NoteRecyclerViewAdapter;
import com.vladkrutlekto.notepad.objects.Note;
import com.vladkrutlekto.notepad.utilities.App;
import com.vladkrutlekto.notepad.utilities.DatabaseHelper;
import com.vladkrutlekto.notepad.utilities.NoteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class NoteListActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NoteListActivity";
    public static final String DEBUG_TAG = "Application_DEBUG";

    static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm", Locale.ENGLISH);

    static DatabaseHelper databaseHelper;
    private NoteDatabase db;

    private boolean isTwoPane; // tablet (true) or phone (false)
    private ArrayList<Note> noteList = new ArrayList<>();
    private NoteRecyclerViewAdapter adapter;
    private FrameLayout detailContainer;

    private ImageButton addButton, deleteButton, deleteAllButton;
    private View recyclerView; // It can be LinearLayout or RecyclerView, depends on device type

    private ArrayList<Boolean> selections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        toolbar.setTitle(getResources().getString(R.string.screen_note_list));

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(this);
        deleteButton = findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(this);
        deleteAllButton = findViewById(R.id.delete_all_button);
        deleteAllButton.setOnClickListener(this);
        recyclerView = findViewById(R.id.note_list);
        detailContainer = findViewById(R.id.note_detail_container);

        if (detailContainer != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            isTwoPane = true;
        }

        db = App.getInstance().getDatabase();

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Log.d(TAG, "onBackStackChanged()");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(DEBUG_TAG, "NoteListActivity - onResume()");

        selections.clear();
        Log.d(TAG, "check line");

        adapter = setupRecyclerView((RecyclerView) recyclerView);

        // [START getting all notes with Room and RxJava]
        Disposable disposable = db.noteDao().getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Note>>() {
                    @Override
                    public void accept(List<Note> notes) throws Exception {
                        noteList.clear();
                        noteList.addAll(notes);
                        Log.d(TAG, "noteList.size() = " + noteList.size());
                        adapter.notifyDataSetChanged();
                    }
                });
        // [END getting all notes with Room and RxJava]
    }

    private NoteRecyclerViewAdapter setupRecyclerView(@NonNull final RecyclerView recyclerView) {
        final NoteRecyclerViewAdapter adapter = new NoteRecyclerViewAdapter(this, noteList, isTwoPane);

        adapter.setOnItemClickListener(new NoteRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, boolean wasChoiceModeOn) {
                if (!wasChoiceModeOn) {
                    // To NoteDetailFragment (edit) [START]
                    Note note = (Note) view.getTag(); // Get object by tag
                    if (adapter.isTwoPane()) { // Tablet
                        NoteDetailFragment editFragment = new NoteDetailFragment();
                        Bundle arguments = new Bundle();
                        arguments.putInt("mode", NoteDetailFragment.EDIT_MODE);
                        arguments.putSerializable("note", note);
                        editFragment.setArguments(arguments);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.note_detail_container, editFragment)
                                .addToBackStack("NoteListActivity-replace-NoteDetailFragment")
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

                updateUI(!adapter.getSelectedItemPositions().isEmpty());
            }

        });

        adapter.setOnItemLongClickListener(new NoteRecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public void onLongItemClick(View view, int position) {
                updateUI(!adapter.getSelectedItemPositions().isEmpty());
            }
        });

        recyclerView.setAdapter(adapter);
        updateUI(!adapter.getSelectedItemPositions().isEmpty());

        return adapter;
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
            getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            super.onBackPressed();
        }
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
                        .addToBackStack("NoteListActivity-replace-NoteDetailFragment")
                        .commit();
            } else {
                Intent intent = new Intent(NoteListActivity.this, NoteDetailActivity.class);
                intent.putExtra("mode", NoteDetailFragment.ADD_MODE);

                startActivity(intent);
            }
        } else if (id == R.id.delete_button) {
            HashSet<Integer> selectedItemPositions = adapter.getSelectedItemPositions();
            for (int index : selectedItemPositions) {
                final Note note = noteList.get(index);

                // [START deleting note with Room and RxJava]
                Completable completable = Completable.fromRunnable(new Runnable() {
                    @Override
                    public void run() {
                        db.noteDao().delete(note);
                    }
                });

                completable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new CompletableObserver() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                Log.d(TAG, "onSubscribe");
                            }

                            @Override
                            public void onComplete() {
                                Log.d(TAG, "onComplete");
                                noteList.remove(note);
                                adapter.notifyDataSetChanged();

                                if (detailContainer != null) {
                                    getSupportFragmentManager()
                                            .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d(TAG, e.getMessage());
                                Log.d(TAG, "onError");
                            }
                        });
                // [END deleting note with Room and RxJava]
            }

            noteList.removeAll(adapter.getSelectedNotes());
            selectedItemPositions.clear();

            updateUI(!adapter.getSelectedItemPositions().isEmpty());
            adapter.notifyDataSetChanged();
        } else if (id == R.id.delete_all_button) {
            // [START deleting all notes with Room and RxJava]
            Completable completable = Completable.fromRunnable(new Runnable() {
                @Override
                public void run() {
                    db.noteDao().deleteAll();
                }
            });

            completable
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new CompletableObserver() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            Log.d(TAG, "onSubscribe");
                        }

                        @Override
                        public void onComplete() {
                            Log.d(TAG, "onComplete");
                            noteList.clear();
                            adapter.notifyDataSetChanged();

                            if (detailContainer != null) {
                                getSupportFragmentManager()
                                        .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, e.getMessage());
                            Log.d(TAG, "onError");
                        }
                    });


            // [END deleting all notes with Room and RxJava]
        }
    }

    private void updateUI(boolean isChoiceModeOn) {
        setVisibility(isChoiceModeOn, deleteButton);
        setVisibility(!isChoiceModeOn, addButton, deleteAllButton);
    }

    private void setVisibility(boolean visible, View... views) {
        for (View view : views) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
