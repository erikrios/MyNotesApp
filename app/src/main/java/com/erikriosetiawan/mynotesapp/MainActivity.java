package com.erikriosetiawan.mynotesapp;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.database.ContentObserver;

import com.erikriosetiawan.mynotesapp.adapter.NoteAdapter;
import com.erikriosetiawan.mynotesapp.db.NoteHelper;
import com.erikriosetiawan.mynotesapp.entity.Note;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.erikriosetiawan.mynotesapp.db.DatabaseContract.NoteColumns.CONTENT_URI;
import static com.erikriosetiawan.mynotesapp.helper.MappingHelper.mapCursorToArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, LoadNotesCallback {

    private RecyclerView rvNotes;
    private ProgressBar progressBar;
    private FloatingActionButton fabAdd;
    private static final String EXTRA_STATE = "EXTRA_STATE";
    private static HandlerThread handlerThread;
    private DataObserver myObserver;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notes");

            rvNotes = findViewById(R.id.rv_notes);
            rvNotes.setLayoutManager(new LinearLayoutManager(this));
            rvNotes.setHasFixedSize(true);

            progressBar = findViewById(R.id.progress_bar);
            fabAdd = findViewById(R.id.fab_add);
            fabAdd.setOnClickListener(this);

            handlerThread = new HandlerThread("DataObserver");
            handlerThread.start();
            Handler handler = new Handler(handlerThread.getLooper());
            myObserver = new DataObserver(handler, this);
            getContentResolver().registerContentObserver(CONTENT_URI, true, myObserver);

            adapter = new NoteAdapter(this);
            rvNotes.setAdapter(adapter);

            if (savedInstanceState == null) {
                new LoadNotesAsync(this, this).execute();
            } else {
                ArrayList<Note> list = savedInstanceState.getParcelableArrayList(EXTRA_STATE);
                if (list != null) {
                    adapter.setListNotes(list);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fab_add) {
            Intent intent = new Intent(MainActivity.this, NoteAddUpdateActivity.class);
            startActivityForResult(intent, NoteAddUpdateActivity.REQUEST_ADD);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(EXTRA_STATE, adapter.getListNotes());
    }

    @Override
    public void preExecute() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void postExecute(ArrayList<Note> notes) {

    }

    @Override
    public void postExecute(Cursor notes) {
        progressBar.setVisibility(View.INVISIBLE);
        ArrayList<Note> listNotes = mapCursorToArrayList(notes);
        if (listNotes.size() > 0) {
            adapter.setListNotes(listNotes);
        } else {
            adapter.setListNotes(new ArrayList<Note>());
            showSnackbarMessage("Tidak ada data saat ini");
        }
    }

    private static class LoadNotesAsync extends AsyncTask<Void, Void, Cursor> {
        private WeakReference<Context> weakContext;
        private WeakReference<LoadNotesCallback> weakCallback;

        private LoadNotesAsync(Context context, LoadNotesCallback callback) {
            weakContext = new WeakReference<>(context);
            weakCallback = new WeakReference<>(callback);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weakCallback.get().preExecute();
        }

        @Override
        protected Cursor doInBackground(Void... voids) {
            Context context = weakContext.get();
            return context.getContentResolver().query(CONTENT_URI, null, null, null, null);
        }

        @Override
        protected void onPostExecute(Cursor notes) {
            super.onPostExecute(notes);
            weakCallback.get().postExecute(notes);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showSnackbarMessage(String message) {
        Snackbar.make(rvNotes, message, Snackbar.LENGTH_SHORT).show();
    }

    public static class DataObserver extends ContentObserver {
        /**
         * Creates a content observer.
         *
         * @param handler The handler to run {@link #onChange} on, or null if none.
         */

        Context context;

        public DataObserver(Handler handler, Context context) {
            super(handler);
            this.context = context;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            new LoadNotesAsync(context, (LoadNotesCallback) context).execute();
        }
    }

}
