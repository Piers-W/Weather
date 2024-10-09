package com.weather;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class WeatherActivity extends AppCompatActivity {

    // UI components for city input, forecast type selection, and query button
    private EditText editTextCityName;
    private RadioGroup radioGroupForecast;
    private RadioButton radioButton48Hours;
    private RadioButton radioButton7Days;
    private Button buttonQuery;
    private ProgressBar progressBar;

    // RecyclerView and Adapter to display weather data
    private RecyclerView recyclerViewWeather;
    private WeatherAdapter weatherAdapter;

    // WeatherService to handle fetching weather data
    private WeatherService weatherService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize UI components
        editTextCityName = findViewById(R.id.et_city_name);
        radioGroupForecast = findViewById(R.id.rg_forecast);
        radioButton48Hours = findViewById(R.id.radio_48hours);
        radioButton7Days = findViewById(R.id.radio_7days);
        buttonQuery = findViewById(R.id.btn_query);
        progressBar = findViewById(R.id.progress_bar);
        recyclerViewWeather = findViewById(R.id.recycler_view_weather);

        // Set up the RecyclerView with a LinearLayoutManager and an empty WeatherAdapter
        recyclerViewWeather.setLayoutManager(new LinearLayoutManager(this));
        weatherAdapter = new WeatherAdapter(this, new ArrayList<>());
        recyclerViewWeather.setAdapter(weatherAdapter);

        // Initialize the WeatherService to fetch weather data
        weatherService = new WeatherService();

        // Set up the button click listener for querying weather data
        buttonQuery.setOnClickListener(v -> {
            // Get the city name input by the user
            String cityName = editTextCityName.getText().toString();

            // If no city name is entered, show a Toast message
            if (cityName.isEmpty()) {
                Toast.makeText(WeatherActivity.this, "Please enter a city name.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check whether the user selected the 48-hour or 7-day forecast
            boolean isHourly = radioButton48Hours.isChecked();

            // Fetch the weather data based on the city name and forecast type
            fetchWeatherData(cityName, isHourly);
        });
    }

    // Method to fetch weather data using the WeatherService
    private void fetchWeatherData(String cityName, boolean isHourly) {
        // Show the progress bar while fetching data
        progressBar.setVisibility(View.VISIBLE);

        // Call the weather service to get the weather data
        weatherService.getWeatherData(cityName, isHourly, new WeatherService.WeatherCallback() {
            @Override
            public void onWeatherDataReceived(List<WeatherData> weatherDataList) {
                // Hide the progress bar when data is received
                progressBar.setVisibility(View.GONE);

                // Update the RecyclerView adapter with the new weather data
                weatherAdapter.updateData(weatherDataList);
            }

            @Override
            public void onError(Exception e) {
                // Hide the progress bar if there is an error
                progressBar.setVisibility(View.GONE);

                // Show a Toast message indicating the error
                Toast.makeText(WeatherActivity.this, "Failed to fetch weather data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}


