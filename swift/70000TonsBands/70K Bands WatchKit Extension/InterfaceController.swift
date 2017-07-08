//
//  InterfaceController.swift
//  70K Bands WatchKit Extension
//
//  Created by Ron Dorn on 3/13/15.
//  Copyright (c) 2015 Ron Dorn. All rights reserved.
//

import WatchKit
import Foundation


@available(iOS 8.2, *)
class InterfaceController: WKInterfaceController {

    @IBOutlet weak var nextBand: WKInterfaceLabel!
    @IBOutlet weak var DayTime: WKInterfaceLabel!
    @IBOutlet weak var location: WKInterfaceLabel!
    @IBOutlet weak var Next: WKInterfaceButton!
    
    var bandsByTime = [String]()
    var schedule = scheduleHandler()
    var bands =  [String]()
    var DayTimeText = String();
    var LocationText = String();
    var index = 0;
    
    override func awakeWithContext(context: AnyObject?) {
        super.awakeWithContext(context)
        
        // Start iCloud key-value updates
        NSUbiquitousKeyValueStore.defaultStore().synchronize()
        updateBandFromICloud()
        
       
        //register application testing defaults
        let defaultValues = ["artistUrl": lastYearsartistUrlDefault,
            "scheduleUrl": "https://www.dropbox.com/s/902zj4tq0w9l5kg/PreviousSchedule1.csv?dl=1",
            "mustSeeAlert": mustSeeAlertDefault, "mightSeeAlert": mightSeeAlertDefault,
            "minBeforeAlert": minBeforeAlertDefault, "alertForShows": alertForShowsDefault,
            "alertForSpecial": alertForSpecialDefault, "alertForMandG": alertForMandGDefault,
            "alertForClinics": alertForClinicsDefault, "alertForListening": alertForListeningDefault,
            "validateScheduleFile": validateScheduleFileDefault]
        
         /*
        artistUrlDefault
        scheduleUrlDefault

        //register Application Defaults
        let defaultValues = ["artistUrl": lastYearsartistUrlDefault,
            "scheduleUrl": scheduleUrlDefault,
            "mustSeeAlert": mustSeeAlertDefault, "mightSeeAlert": mightSeeAlertDefault,
            "minBeforeAlert": minBeforeAlertDefault, "alertForShows": alertForShowsDefault,
            "alertForSpecial": alertForSpecialDefault, "alertForMandG": alertForMandGDefault,
            "alertForClinics": alertForClinicsDefault, "alertForListening": alertForListeningDefault,
            "validateScheduleFile": validateScheduleFileDefault]
               */
        
        NSUserDefaults.standardUserDefaults().registerDefaults(defaultValues)
        
        var scheduleUrl = defaults.stringForKey("scheduleUrl")

        gatherData()
        schedule.DownloadCsv()
        //readBandFile()
        schedule.populateSchedule()
        refreshData()
        
    }
    
    @IBAction func NextShow() {
        
        print(index)
        print(bandsByTime.count)
        
        if (index < (bandsByTime.count - 1)){
            index = index + 1
            refreshData()
        }
    }
    
    @IBAction func PreviousShow() {
        if (index != 0){
            index = index - 1
            refreshData()
        }
    }
    
    func refreshData() {
        
        bands = getFilteredBands(getBandNames(), schedule: schedule)
        sortBandsByTime()
        getPriorityDataFromiCloud()
        print(schedule.schedulingData);
        
        let bandText = getPriorityIcon(getPriorityData(bandsByTime[index])) + bandsByTime[index]
        nextBand.setText(bandText)
        getScheduleData(bandsByTime[index])
        if (DayTimeText.isEmpty == true){
            DayTime.setText("Schedule Not");
            location.setText("Yet  Available")
        } else {
            DayTime.setText(DayTimeText)
            location.setText(LocationText)
        }
    }
    
    func getScheduleData (bandName: String) {
        
        if (schedule.schedulingData[bandName]?.isEmpty == false){
            let keyValues = schedule.schedulingData[bandName]!.keys
            let arrayValues = keyValues.enumerate()
            //var sortedArray = arrayValues.sort({
            //    $0 < $1
            //})
            
            let sortedArray = arrayValues.reverse()
            var count = 1
            for index in sortedArray {
                
                let location = schedule.getData(bandName, index: Double(index.0), variable: "Location")
                let day = schedule.getData(bandName, index: Double(index.0), variable: "Day")
                let startTime = schedule.getData(bandName, index: Double(index.0), variable: "Start Time")
                var endTime = schedule.getData(bandName, index: Double(index.0), variable: "End Time")
                let date = schedule.getData(bandName, index:Double(index.0), variable: "Date")
                var type = schedule.getData(bandName, index:Double(index.0), variable: "Type")
                var notes = schedule.getData(bandName, index:Double(index.0), variable: "Notes")
                
                var scheduleText = String()
                if (date.isEmpty == false){
                    DayTimeText = day + " " + startTime
                    LocationText = location;
                    return
                }
            }
        }
    }

    
    private func updateBandFromICloud() {
        let bandInfo = NSUbiquitousKeyValueStore.defaultStore().dictionaryRepresentation
        if (bandInfo.count >= 1) {
            getPriorityDataFromiCloud()
        }
    }
    
    func sortBandsByTime() {
        
        var sortableBands = Dictionary<NSTimeInterval, String>()
        var sortableTimeIndexArray = [NSTimeInterval]()
        var sortedBands = [String]()
        
        var fullBands = bands;
        var dupAvoidBands = Dictionary<String,Int>()
        
        let futureTime :Int64 = 8000000000000;
        var noShowsLeftMagicNumber = NSTimeInterval(futureTime)

        for bandName in bands {
            let timeIndex: NSTimeInterval = schedule.getCurrentIndex(bandName);
            if (timeIndex > NSDate().timeIntervalSince1970 - 3600){
                sortableBands[timeIndex] = bandName
                sortableTimeIndexArray.append(timeIndex)
            } else {
                sortableBands[noShowsLeftMagicNumber] = bandName
                sortableTimeIndexArray.append(noShowsLeftMagicNumber)
                noShowsLeftMagicNumber = noShowsLeftMagicNumber + 1
            }
        }
        
        
        let sortedArray = sortableTimeIndexArray.sort({$0 < $1})
        
        for index in sortedArray{
            if (dupAvoidBands[sortableBands[index]!] == nil){
                sortedBands.append(sortableBands[index]!)
                dupAvoidBands[sortableBands[index]!] = 1
            }
        }
        
        
        bandsByTime = sortedBands
    }

    override func willActivate() {
        // This method is called when watch view controller is about to be visible to user
        super.willActivate()
        
    }

    override func didDeactivate() {
        // This method is called when watch view controller is no longer visible
        super.didDeactivate()
    }

}
