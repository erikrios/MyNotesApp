package com.erikriosetiawan.mynotesapp;

import android.database.Cursor;

import com.erikriosetiawan.mynotesapp.entity.Note;

import java.util.ArrayList;

public interface LoadNotesCallback {
    void preExecute();
    void postExecute(ArrayList<Note> notes);

    void postExecute(Cursor notes);
}
