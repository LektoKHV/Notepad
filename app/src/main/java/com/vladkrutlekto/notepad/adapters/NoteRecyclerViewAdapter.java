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
import java.util.List;
import java.util.Locale;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {
    private static final String TAG = "NoteListActivity";

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface OnItemLongClickListener {
        void onLongItemClick(View view, int position);
    }

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM.dd.yyyy HH:mm", Locale.ENGLISH);
    private final NoteListActivity activity;
    private final List<Note> noteList;
    private final boolean isTwoPane;
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

        holder.name.setText(noteList.get(position).getName());
        holder.date.setText(simpleDateFormat.format(noteList.get(position).getDate()));

        holder.itemView.setTag(noteList.get(position));
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
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            onItemLongClickListener.onLongItemClick(v, getAdapterPosition());
            return true;
        }
    }
}
