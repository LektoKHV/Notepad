package com.vladkrutlekto.notepad.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vladkrutlekto.notepad.NoteListActivity;
import com.vladkrutlekto.notepad.R;
import com.vladkrutlekto.notepad.objects.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "NoteListActivity";

    public interface OnItemClickListener {
        void onItemClick(View view, int position, boolean wasChoiceModeOn);
    }

    public interface OnItemLongClickListener {
        void onLongItemClick(View view, int position);
    }

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm", Locale.ENGLISH);
    private final NoteListActivity activity;
    private final List<Note> noteList;
    private final boolean isTwoPane;

    private HashSet<Integer> selectedItemPositions = new HashSet<>();

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;


    public NoteRecyclerViewAdapter(NoteListActivity parent, List<Note> items, boolean twoPane) {
        noteList = items;
        activity = parent;
        isTwoPane = twoPane;
    }

    public boolean isTwoPane() {
        return isTwoPane;
    }

    @NonNull
    @Override
    public NoteRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.note_list_item, parent, false);
        return new NoteRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NoteRecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder(" + position + ")");

        Note note = noteList.get(position);

        holder.name.setText(note.getName());
        holder.date.setText(note.getDate());

        holder.itemView.setSelected(false);
        holder.itemView.setTag(note);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        final TextView name, date;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            date = view.findViewById(R.id.date);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            boolean wasChoiceModeOn = !selectedItemPositions.isEmpty();
            if (wasChoiceModeOn) {
                v.setSelected(!v.isSelected());

                if (v.isSelected()) selectedItemPositions.add(getAdapterPosition());
                else selectedItemPositions.remove(getAdapterPosition());
            }

            onItemClickListener.onItemClick(v, getAdapterPosition(), wasChoiceModeOn);
        }

        @Override
        public boolean onLongClick(View v) {
            v.setSelected(!v.isSelected());
            if (v.isSelected()) selectedItemPositions.add(getAdapterPosition());
            else selectedItemPositions.remove(getAdapterPosition());

            onItemLongClickListener.onLongItemClick(v, getAdapterPosition());
            return true;
        }
    }

    public HashSet<Integer> getSelectedItemPositions() {
        return selectedItemPositions;
    }

    public ArrayList<Note> getSelectedNotes() {
        ArrayList<Note> selectedNotes = new ArrayList<>();

        for (int i : selectedItemPositions) {
            selectedNotes.add(noteList.get(i));
        }

        return selectedNotes;
    }
}
