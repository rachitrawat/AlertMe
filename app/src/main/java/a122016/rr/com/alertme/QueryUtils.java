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

import com.google.android.gms.location.places.Places;

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
 * Helper methods related to requesting and receiving Places data from server.
 */
public final class QueryUtils {

    /**
     * Tag for the log messages
     */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the server and return appropriate Places object
     */
    public static ArrayList<Place> fetchPlacesData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // Extract relevant fields from the JSON response and create a {@link Places} object
        ArrayList<Place> placesList = extractFeatureFromJson(jsonResponse);

        // Return the {@link Places} object
        return placesList;
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
            Log.e(LOG_TAG, "Problem retrieving the Places JSON results.", e);
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
     * Return a {@link Place} object that has been built up from
     * parsing the given JSON response.
     */
    private static ArrayList<Place> extractFeatureFromJson(String PlacesJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(PlacesJSON)) {
            return null;
        }

        // Create an array list of Places
        ArrayList<Place> placesList = new ArrayList<Place>();

        // Try to parse the JSON response string. If there's a problem with the way the JSON
        // is formatted, a JSONException exception object will be thrown.
        // Catch the exception so the app doesn't crash, and print the error message to the logs.
        try {

            // Create a JSONArray from the JSON response string
            JSONArray placeArray = new JSONArray(PlacesJSON);

            // For each place in the placeArray, create an {@link place} object
            for (int i = 0; i < placeArray.length(); i++) {

                // Get a single place at position i within the list of places
                JSONObject currentPlace = placeArray.getJSONObject(i);

                // Extract the value for the key called "PLACE OF ACCIDENT"
                String location = currentPlace.getString("PLACE OF ACCIDENT");

                // Extract the value for the key called "NH/SH/MDR/OTHER"
                String highwayNumber = currentPlace.getString("NH/SH/MDR/OTHER");

                // Extract the value for the key called "FATALITIES IN 2015"
                int fatalities2015 = currentPlace.getInt("FATALITIES IN 2015");

                // Extract the value for the key called "FATALITIES IN 2016"
                int fatalities2016 = currentPlace.getInt("FATALITIES IN 2016");

                // Extract the value for the key called "CAUSE OF ACCIDENT"
                String causeOfAccident = currentPlace.getString("CAUSE OF ACCIDENT");

                double latitude = currentPlace.getDouble("LATITUDE");
                double longitude = currentPlace.getDouble("LONGITUDE");

                // Create a new {@link place} object
                Place place = new Place(location, highwayNumber, fatalities2015, fatalities2016, causeOfAccident, latitude, longitude);

                // Add the new {@link place} to the list of places.
                placesList.add(place);
            }


        } catch (JSONException e) {
            // If an error is thrown when executing any of the above statements in the "try" block,
            // catch the exception here, so the app doesn't crash. Print a log message
            // with the message from the exception.
            Log.e("QueryUtils", "Problem parsing the Places JSON results", e);
        }

        // Return the array list
        return placesList;
    }

}
