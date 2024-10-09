package com.weather;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder> {

    // List of weather data and context for inflating views
    private List<WeatherData> weatherDataList;
    private Context context;

    // Constructor to initialize the adapter with context and weather data
    public WeatherAdapter(Context context, List<WeatherData> weatherDataList) {
        this.context = context;
        this.weatherDataList = weatherDataList;
    }

    // Called when RecyclerView needs a new ViewHolder to represent an item
    @NonNull
    @Override
    public WeatherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the weather item layout and create a new ViewHolder
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather, parent, false);
        return new WeatherViewHolder(itemView);
    }

    // Called by RecyclerView to bind data to the ViewHolder at the specified position
    @Override
    public void onBindViewHolder(@NonNull WeatherViewHolder holder, int position) {
        // Get the weather data at the current position
        WeatherData weatherData = weatherDataList.get(position);

        // Set the time, weather description, and temperature text in the ViewHolder
        holder.tvTime.setText(weatherData.getTime());
        holder.tvWeatherDescription.setText(weatherData.getWeatherMain());
        holder.tvTemperature.setText(weatherData.getTemperature());

        // Use Glide library to load the weather icon from URL and set it to the ImageView
        Glide.with(context)
                .load(weatherData.getIconUrl())
                .into(holder.ivWeatherIcon);
    }

    // Return the total number of items in the weather data list
    @Override
    public int getItemCount() {
        return weatherDataList.size();
    }

    // Method to update the weather data list and refresh the RecyclerView
    public void updateData(List<WeatherData> newWeatherData) {
        weatherDataList = newWeatherData;
        notifyDataSetChanged();  // Notify RecyclerView that data has changed
    }

    // ViewHolder class to hold the views for each weather item
    public static class WeatherViewHolder extends RecyclerView.ViewHolder {
        ImageView ivWeatherIcon;      // ImageView for the weather icon
        TextView tvWeatherDescription; // TextView for the weather description
        TextView tvTime;              // TextView for the time of the weather data
        TextView tvTemperature;       // TextView for the temperature

        // Constructor to bind the views
        public WeatherViewHolder(@NonNull View itemView) {
            super(itemView);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
            tvWeatherDescription = itemView.findViewById(R.id.tv_weather_description);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
        }
    }
}
