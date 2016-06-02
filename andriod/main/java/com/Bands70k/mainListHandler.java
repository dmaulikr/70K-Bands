package com.Bands70k;

import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by rdorn on 5/29/16.
 */
public class mainListHandler {

    private final String TAG = "mainListHandler";

    public String sortedBy;
    public List<String> sortableBandNames = new ArrayList<String>();
    public List<String> bandNamesIndex = new ArrayList<String>();

    public ListAdapter arrayAdapter;
    public showBands showBands;
    private Integer numberOfEvents = 0;
    private Integer numberOfBands = 0;

    public mainListHandler(showBands showBandsValue){
        showBands = showBandsValue;
    }

    public void populateBandInfo(BandInfo bandInfo, ArrayList<String> bandList){

        arrayAdapter = new ArrayAdapter<String>(showBands, R.layout.bandlist70k, bandList);

        List<String> bandPresent = new ArrayList<String>();

        if (BandInfo.scheduleRecords != null) {
            for (String bandName : bandInfo.scheduleRecords.keySet()) {
                for (Long timeIndex : BandInfo.scheduleRecords.get(bandName).scheduleByTime.keySet()) {
                    if (staticVariables.sortBySchedule == true) {
                        if ((timeIndex + 3600000) > System.currentTimeMillis()) {
                            sortableBandNames.add(String.valueOf(timeIndex) + ":" + bandName);
                            bandPresent.add(bandName);
                            numberOfEvents++;
                        }
                    } else {
                        if ((timeIndex + 3600000) > System.currentTimeMillis()) {
                            sortableBandNames.add(bandName + ":" + String.valueOf(timeIndex));
                            numberOfEvents++;
                        }
                        bandPresent.add(bandName);
                    }
                }
            }
            Collections.sort(sortableBandNames);
            for (String bandName : bandList){
                if (bandPresent.contains(bandName) == false){
                    sortableBandNames.add(bandName + ":");
                    numberOfBands++;
                }
            }

        } else {
            sortableBandNames = bandList;
            numberOfBands = bandList.size();
            Collections.sort(sortableBandNames);
        }

        turnSortedListIntoArrayAdapter();

        TextView bandCount = (TextView) showBands.findViewById(R.id.headerBandCount);
        bandCount.setText(this.getSizeDisplay());
    }

    private void turnSortedListIntoArrayAdapter(){

        ArrayList<String> displayableBandList = new ArrayList<String>();

        for (String bandIndex: sortableBandNames){
            Log.d(TAG, "bandIndex=" + bandIndex);
            String bandName = getBandNameFromIndex(bandIndex);
            Long timeIndex = getBandTimeFromIndex(bandIndex);

            String line = buildLines(timeIndex, bandName);

            if (checkFiltering(bandName) == true) {
                displayableBandList.add(line);
                bandNamesIndex.add(bandName);
            }
        }

        //setTextAppearance(context, android.R.attr.textAppearanceMedium)
        arrayAdapter = new ArrayAdapter<String>(showBands, R.layout.bandlist70k, displayableBandList);
    }

    private String getBandNameFromIndex(String value){

        String[] indexData = value.split(":");

        if (isLong(indexData[0]) == false){
            return indexData[0];

        } else if (indexData.length == 2){
            if (isLong(indexData[1]) == false){
                return indexData[1];
            }
        }

        return value;
    }

    private Long getBandTimeFromIndex(String value){

        String[] indexData = value.split(":");
        Long timeIndex = Long.valueOf(0);

        if (isLong(indexData[0]) == true){
            return Long.valueOf(indexData[0]);

        } else if (indexData.length == 2){
            if (isLong(indexData[1]) == true){
                return Long.valueOf(indexData[1]);
            }
        }

        return timeIndex;
    }

    public String getSizeDisplay(){

        String displayText = "";

        if (numberOfBands != 0) {
            displayText = "70,0000 Tons " + numberOfBands + " bands";

        } else if (numberOfEvents != 0) {
            displayText = "70,0000 Tons " + numberOfEvents + " events";

        } else {
            displayText = "70,0000 Tons 0 bands";
        }

        return displayText;
    }

    private String buildLines(Long timeIndex,String bandName){

        String line = null;

        if (timeIndex > 0){
            line = rankStore.getRankForBand(bandName);

            if (!rankStore.getRankForBand(bandName).equals("")) {
                line += " - ";
            }
            if (BandInfo.scheduleRecords.get(bandName).scheduleByTime.get(timeIndex) != null) {
                line += bandName + " - ";
                line += BandInfo.scheduleRecords.get(bandName).scheduleByTime.get(timeIndex).getShowDay() + " ";
                line += BandInfo.scheduleRecords.get(bandName).scheduleByTime.get(timeIndex).getStartTimeString() + " ";
                line += BandInfo.scheduleRecords.get(bandName).scheduleByTime.get(timeIndex).getShowLocation();
                line += " " + staticVariables.getEventTypeIcon(BandInfo.scheduleRecords.get(bandName).scheduleByTime.get(timeIndex).getShowType());
            }
        } else {

            line = rankStore.getRankForBand(bandName);
            if (!rankStore.getRankForBand(bandName).equals("")) {
                line += " - ";
            }
            line += bandName;
        }

        return line;
    }

    private boolean checkFiltering(String bandName){

        Boolean returnValue = true;

        Log.d(TAG, "FILTERING - " + rankStore.getRankForBand(bandName) + " " + staticVariables.mustSeeIcon);

        if (rankStore.getRankForBand(bandName).equals(staticVariables.mustSeeIcon)){
            if (staticVariables.filterToogle.get(staticVariables.mustSeeIcon) == true){
                returnValue = true;
            } else {
                returnValue = false;
            }

        } else if (rankStore.getRankForBand(bandName).equals(staticVariables.mightSeeIcon)){
            if (staticVariables.filterToogle.get(staticVariables.mightSeeIcon) == true){
                returnValue = true;
            } else {
                returnValue = false;
            }
        } else if (rankStore.getRankForBand(bandName).equals(staticVariables.wontSeeIcon)){
            if (staticVariables.filterToogle.get(staticVariables.wontSeeIcon) == true){
                returnValue = true;
            } else {
                returnValue = false;
            }
        } else {
            if (staticVariables.filterToogle.get(staticVariables.unknownIcon) == true) {
                returnValue = true;
            } else {
                returnValue = false;
            }
        }

        return returnValue;
    }

    public static boolean isLong(String value) {
        try {
            Long.parseLong(value);
        } catch(Exception e) {
            Log.d("mainListHandler", "long of " + value + " is false");
            return false;
        }
        // only got here if we didn't return false

        Log.d("mainListHandler", "long of " + value + " is true");
        return true;
    }

}
