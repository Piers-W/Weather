package com.weather;

import android.os.Handler;
import android.os.Looper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherService {
    // API key for OpenWeatherMap
    private static final String API_KEY = "9f7acf49a3248c32711ca383faa558c6";

    // ExecutorService to handle network requests in the background
    private final ExecutorService executorService;

    // Handler to post results back to the main thread
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Constructor initializing a fixed thread pool
    public WeatherService() {
        executorService = Executors.newFixedThreadPool(4); // Using a fixed thread pool for network requests
    }

    // Interface to provide callback methods for success or error scenarios
    public interface WeatherCallback {
        void onWeatherDataReceived(List<WeatherData> weatherDataList); // Called when weather data is successfully received
        void onError(Exception e); // Called when an error occurs during the network request
    }

    // Method to fetch weather data for a given city and forecast type (hourly or daily)
    public void getWeatherData(String cityName, boolean isHourly, WeatherCallback callback) {
        executorService.execute(() -> {
            try {
                // Fetch geographic coordinates for the given city name
                URL geoUrl = new URL("https://api.openweathermap.org/geo/1.0/direct?q=" + cityName + "&limit=1&appid=" + API_KEY);
                HttpURLConnection geoConnection = (HttpURLConnection) geoUrl.openConnection();
                geoConnection.setRequestMethod("GET");
                String geoResponse = streamToString(geoConnection.getInputStream());
                JSONArray geoResults = new JSONArray(geoResponse);

                // Check if there are results for the city name
                if (geoResults.length() == 0) {
                    throw new Exception("No geographic data found for city: " + cityName);
                }

                // Extract latitude and longitude from the JSON response
                JSONObject geoJson = geoResults.getJSONObject(0);
                double lat = geoJson.getDouble("lat");
                double lon = geoJson.getDouble("lon");

                // Fetch weather data based on coordinates and forecast type (48-hour or 7-day)
                String excludePart = isHourly ? "daily,minutely,current,alerts" : "hourly,minutely,current,alerts";
                URL weatherUrl = new URL("https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon + "&exclude=" + excludePart + "&units=imperial&appid=" + API_KEY);
                HttpURLConnection weatherConnection = (HttpURLConnection) weatherUrl.openConnection();
                weatherConnection.setRequestMethod("GET");
                String weatherResponse = streamToString(weatherConnection.getInputStream());
                JSONObject weatherJson = new JSONObject(weatherResponse);

                // Parse the weather data from JSON response
                List<WeatherData> weatherDataList = parseWeatherData(weatherJson, isHourly);

                // Post the result back to the main thread via the callback
                handler.post(() -> callback.onWeatherDataReceived(weatherDataList));
            } catch (Exception e) {
                // Post the error back to the main thread via the callback
                handler.post(() -> callback.onError(e));
            }
        });
    }

    // Method to parse the weather data from the JSON response
    private List<WeatherData> parseWeatherData(JSONObject jsonObject, boolean isHourly) throws Exception {
        List<WeatherData> weatherDataList = new ArrayList<>();

        if (isHourly) {
            // Parse 48-hour weather data
            JSONArray hourlyArray = jsonObject.getJSONArray("hourly");
            for (int i = 0; i < 48 && i < hourlyArray.length(); i++) {
                JSONObject hourlyObject = hourlyArray.getJSONObject(i);
                String time = convertUnixToDateTime(hourlyObject.getLong("dt")); // Convert Unix timestamp to time
                String weatherMain = hourlyObject.getJSONArray("weather").getJSONObject(0).getString("main");
                String iconUrl = "https://openweathermap.org/img/wn/" + hourlyObject.getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png";
                String temperature = String.valueOf(hourlyObject.getDouble("temp")) + " °F"; // Temperature in Fahrenheit

                // Create a new WeatherData object and add it to the list
                WeatherData weatherData = new WeatherData(time, weatherMain, iconUrl, temperature);
                weatherDataList.add(weatherData);
            }
        } else {
            // Parse 7-day weather data
            JSONArray dailyArray = jsonObject.getJSONArray("daily");
            for (int i = 0; i < 7 && i < dailyArray.length(); i++) {
                JSONObject dailyObject = dailyArray.getJSONObject(i);
                String time = convertUnixToDate(dailyObject.getLong("dt")); // Convert Unix timestamp to date
                String weatherMain = dailyObject.getJSONArray("weather").getJSONObject(0).getString("main");
                String iconUrl = "https://openweathermap.org/img/wn/" + dailyObject.getJSONArray("weather").getJSONObject(0).getString("icon") + "@2x.png";
                double minTemp = dailyObject.getJSONObject("temp").getDouble("min");
                double maxTemp = dailyObject.getJSONObject("temp").getDouble("max");
                String temperature = String.format(Locale.getDefault(), "%.1f - %.1f °F", minTemp, maxTemp); // Min and max temperature

                // Create a new WeatherData object and add it to the list
                WeatherData weatherData = new WeatherData(time, weatherMain, iconUrl, temperature);
                weatherDataList.add(weatherData);
            }
        }

        return weatherDataList; // Return the parsed weather data list
    }

    // Convert Unix timestamp to formatted time (for hourly weather)
    private String convertUnixToDateTime(long unixTime) {
        Date date = new Date(unixTime * 1000L);  // Convert to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // Display in 12-hour format
        return sdf.format(date);
    }

    // Convert Unix timestamp to formatted date (for daily weather)
    private String convertUnixToDate(long unixTime) {
        Date date = new Date(unixTime * 1000L);  // Convert to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault()); // Example: Mon, Sep 27
        return sdf.format(date);
    }

    // Helper method to convert InputStream to a String
    private String streamToString(java.io.InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : ""; // Convert entire input stream to a string
    }
}
