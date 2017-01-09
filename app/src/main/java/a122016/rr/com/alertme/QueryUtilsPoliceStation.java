package a122016.rr.com.alertme;

/**
 * Created by rachitrawat on 12/26/2016.
 */

/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Helper methods related to requesting and receiving PoliceStations data from server.
 */
public final class QueryUtilsPoliceStation {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtilsPoliceStation.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtilsPoliceStation} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsPoliceStation() {
    }

    /**
     * Query the server and return appropriate PoliceStations object
     */
    public static ArrayList<PoliceStation> fetchPoliceStationsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }


        // Extract relevant fields from the JSON response and create a {@link PoliceStations} object
        ArrayList<PoliceStation> PoliceStationsList = extractFeatureFromJson(jsonResponse);

        // Return the {@link PoliceStations} object
        return PoliceStationsList;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the PoliceStations JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // Closing the input stream could throw an IOException, which is why
                // the makeHttpRequest(URL url) method signature specifies than an IOException
                // could be thrown.
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a {@link PoliceStation} object that has been built up from
     * parsing the given JSON response.
     */
    private static ArrayList<PoliceStation> extractFeatureFromJson(String PoliceStationsJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(PoliceStationsJSON)) {
            return null;
        }

        // Create an array list of PoliceStations
        ArrayList<PoliceStation> PoliceStationsList = new ArrayList<PoliceStation>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONArray from the JSON response string
            JSONArray PoliceStationArray = new JSONArray(PoliceStationsJSON);

            // For each PoliceStation in the PoliceStationArray, create an {@link PoliceStation} object
            for (int i = 0; i < PoliceStationArray.length(); i++) {

                // Get a single PoliceStation at position i within the list of PoliceStations
                JSONObject currentPoliceStation = PoliceStationArray.getJSONObject(i);

                // Extract the value for the key called "Name"
                String name = currentPoliceStation.getString("Name");

                // Extract the value for the key called "Number"
                String number = currentPoliceStation.getString("Number");

                double latitude = currentPoliceStation.getDouble("Latitude");
                double longitude = currentPoliceStation.getDouble("Longitude");

                // Create a new {@link PoliceStation} object
                PoliceStation PoliceStation = new PoliceStation(name, number, latitude, longitude);

                // Add the new {@link PoliceStation} to the list of PoliceStations.
                PoliceStationsList.add(PoliceStation);
            }


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtilsPoliceStation", "Problem parsing the PoliceStations JSON results", e);
        }

        // Return the array list
        return PoliceStationsList;
    }

}
