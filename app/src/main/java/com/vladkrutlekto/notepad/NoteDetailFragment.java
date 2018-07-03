package com.vladkrutlekto.notepad;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.vladkrutlekto.notepad.objects.Note;
import com.vladkrutlekto.notepad.utilities.App;
import com.vladkrutlekto.notepad.utilities.NoteDatabase;

import java.util.Date;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class NoteDetailFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "NoteDetailFragment";
    private Note note;
    public static final int EDIT_MODE = 200,
            ADD_MODE = 201;
    private int mode;

    private NoteDatabase db;

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
            db = App.getInstance().getDatabase();

            final Note note = new Note(name.getText().toString(),
                    text.getText().toString(),
                    NoteListActivity.simpleDateFormat.format(new Date()));

            // [START inserting with Room and RxJava]
            Completable completable = Completable.fromRunnable(new Runnable() {
                @Override
                public void run() {
                    db.noteDao().insertNote(note);
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

                            // Exit from this screen
                            if (getActivity() != null) {
                                hideSoftKeyboard(getActivity());
                                if (getActivity() instanceof NoteListActivity) {
                                    getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    //((NoteListActivity) getActivity()).onResume();
                                } else {
                                    getActivity().finish();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, e.getMessage());
                            Log.d(TAG, "onError");
                        }
                    });
            // [END inserting with Room and RxJava]


        } else if (id == R.id.edit_button) {
            db = App.getInstance().getDatabase();

            note.setName(name.getText().toString());
            note.setText(text.getText().toString());

            // [START inserting with Room and RxJava]
            Completable completable = Completable.fromRunnable(new Runnable() {
                @Override
                public void run() {
                    db.noteDao().updateNote(note);
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

                            // Exit from this screen
                            if (getActivity() != null) {
                                hideSoftKeyboard(getActivity());
                                if (getActivity() instanceof NoteListActivity) {
                                    getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                    //((NoteListActivity) getActivity()).onResume();
                                } else {
                                    getActivity().finish();
                                }
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, e.getMessage());
                            Log.d(TAG, "onError");
                        }
                    });
            // [END inserting with Room and RxJava]
        }
    }

    public void hideSoftKeyboard(@NonNull Activity activity) {
        if (activity.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
