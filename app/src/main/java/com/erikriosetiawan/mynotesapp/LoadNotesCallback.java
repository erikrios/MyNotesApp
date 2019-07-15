package com.erikriosetiawan.mynotesapp;

import com.erikriosetiawan.mynotesapp.entity.Note;

import java.util.ArrayList;

public interface LoadNotesCallback {
    void preExecute();
    void postExecute(ArrayList<Note> notes);
}
