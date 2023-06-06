package com.example.avaruussaa_android;

import java.util.List;

// This is our data store Singleton which holds information about the magnetic activity and error state of each station
// The data it holds is updated by DataFetchWorker
public class StationsData {
    // stationsList is an unmodifiableList, encapsulating our station data and preventing direct modification of the list
    // It's initialized with the names and codes of each station listed at https://www.ilmatieteenlaitos.fi/revontulet-ja-avaruussaa
    // The codes can be used to parse station data from the javascript found on that page
    // Data may be read through stations() and items modified with setActivity() and setError()
    private static final List<Station> stationsList = List.of(
        new Station("Kevo", "KEV"),
        new Station("Kilpisjärvi", "KIL"),
        new Station("Ivalo", "IVA"),
        new Station("Muonio", "MUO"),
        new Station("Sodankylä", "SOD"),
        new Station("Pello", "PEL"),
        new Station("Ranua", "RAN"),
        new Station("Oulujärvi", "OUJ"),
        new Station("Mekrijärvi", "MEK"),
        new Station("Hankasalmi", "HAN"),
        new Station("Nurmijärvi", "NUR"),
        new Station("Tartto", "TAR")
    );

    // This is a static class, constructor is private
    private StationsData() {}

    public static List<Station> stations() {
        return stationsList;
    }
}
