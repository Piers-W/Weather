package com.weather;

public class WeatherData {
    private String time;
    private String weatherMain;
    private String iconUrl;
    private String temperature;

    public WeatherData(String time, String weatherMain, String iconUrl, String temperature) {
        this.time = time;
        this.weatherMain = weatherMain;
        this.iconUrl = iconUrl;
        this.temperature = temperature;
    }

    public String getTime() {
        return time;
    }


    public String getWeatherMain() {
        return weatherMain;
    }


    public String getIconUrl() {
        return iconUrl;
    }


    public String getTemperature() {
        return temperature;
    }

}

