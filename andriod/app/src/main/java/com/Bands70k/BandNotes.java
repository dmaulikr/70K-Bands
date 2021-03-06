package com.Bands70k;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;

/**
 * Created by rdorn on 4/16/17.
 */

public class BandNotes {

    private String bandName;
    private File bandNoteFile;

    private boolean noteIsBlank = false;

    public BandNotes(String bandValue){

        bandName = bandValue;
        bandNoteFile = new File(Environment.getExternalStorageDirectory() + "/70kBands/" + bandName + ".note");

    }

    public boolean getNoteIsBlank(){
        return noteIsBlank;
    }

    public String getBandNote(){

        String note = "";
        try {

            BufferedReader br = new BufferedReader(new FileReader(bandNoteFile));

            String line;

            while ((line = br.readLine()) != null) {
                note += line + "<br>";
            }

            noteIsBlank = false;
        } catch (Exception error) {
            Log.e("writingBandRankings", "backupFile " + error.getMessage());
            note = "Enter your own custom note here";
            noteIsBlank = true;
        }

        return note;
    }

    public void saveBandNote(String notesData){

        FileHandler70k.saveData(notesData, bandNoteFile);

    }

}
