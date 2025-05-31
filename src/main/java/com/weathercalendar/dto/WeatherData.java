package com.weathercalendar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public class WeatherData {

    private String location;
    private LocalDate date;
    private String description; // 날씨 상태 (맑음, 흐림 등)
    private String icon; // 날씨 아이콘 코드
    private Double tempMin; // 최저 기온
    private Double tempMax; // 최고 기온
    private Integer humidity; // 습도 (%)
    private Integer precipitationProbability; // 강수 확률 (%)
    private Double windSpeed; // 풍속 (m/s)
    private Integer uvIndex; // 자외선 지수
    private String airQuality; // 대기질 상태

    // 기본 생성자
    public WeatherData() {}

    // 모든 필드 생성자
    public WeatherData(String location, LocalDate date, String description, String icon,
                       Double tempMin, Double tempMax, Integer humidity,
                       Integer precipitationProbability, Double windSpeed,
                       Integer uvIndex, String airQuality) {
        this.location = location;
        this.date = date;
        this.description = description;
        this.icon = icon;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.humidity = humidity;
        this.precipitationProbability = precipitationProbability;
        this.windSpeed = windSpeed;
        this.uvIndex = uvIndex;
        this.airQuality = airQuality;
    }

    // Getter/Setter 메서드들
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Double getTempMin() { return tempMin; }
    public void setTempMin(Double tempMin) { this.tempMin = tempMin; }

    public Double getTempMax() { return tempMax; }
    public void setTempMax(Double tempMax) { this.tempMax = tempMax; }

    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }

    public Integer getPrecipitationProbability() { return precipitationProbability; }
    public void setPrecipitationProbability(Integer precipitationProbability) { this.precipitationProbability = precipitationProbability; }

    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

    public Integer getUvIndex() { return uvIndex; }
    public void setUvIndex(Integer uvIndex) { this.uvIndex = uvIndex; }

    public String getAirQuality() { return airQuality; }
    public void setAirQuality(String airQuality) { this.airQuality = airQuality; }

    // Builder 메서드
    public static WeatherDataBuilder builder() {
        return new WeatherDataBuilder();
    }

    // Builder 클래스
    public static class WeatherDataBuilder {
        private String location;
        private LocalDate date;
        private String description;
        private String icon;
        private Double tempMin;
        private Double tempMax;
        private Integer humidity;
        private Integer precipitationProbability;
        private Double windSpeed;
        private Integer uvIndex;
        private String airQuality;

        public WeatherDataBuilder location(String location) { this.location = location; return this; }
        public WeatherDataBuilder date(LocalDate date) { this.date = date; return this; }
        public WeatherDataBuilder description(String description) { this.description = description; return this; }
        public WeatherDataBuilder icon(String icon) { this.icon = icon; return this; }
        public WeatherDataBuilder tempMin(Double tempMin) { this.tempMin = tempMin; return this; }
        public WeatherDataBuilder tempMax(Double tempMax) { this.tempMax = tempMax; return this; }
        public WeatherDataBuilder humidity(Integer humidity) { this.humidity = humidity; return this; }
        public WeatherDataBuilder precipitationProbability(Integer precipitationProbability) { this.precipitationProbability = precipitationProbability; return this; }
        public WeatherDataBuilder windSpeed(Double windSpeed) { this.windSpeed = windSpeed; return this; }
        public WeatherDataBuilder uvIndex(Integer uvIndex) { this.uvIndex = uvIndex; return this; }
        public WeatherDataBuilder airQuality(String airQuality) { this.airQuality = airQuality; return this; }

        public WeatherData build() {
            return new WeatherData(location, date, description, icon, tempMin, tempMax,
                    humidity, precipitationProbability, windSpeed, uvIndex, airQuality);
        }
    }

    // 캘린더 이벤트 생성을 위한 메서드
    public String generateEventTitle() {
        return String.format("[%s] %.0f°C / %.0f°C",
                description, tempMin, tempMax);
    }

    public String generateEventDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("📍 위치: ").append(location).append("\n");
        sb.append("🌡️ 기온: ").append(String.format("%.0f°C ~ %.0f°C", tempMin, tempMax)).append("\n");
        sb.append("💧 습도: ").append(humidity).append("%\n");
        sb.append("🌧️ 강수확률: ").append(precipitationProbability).append("%\n");
        sb.append("💨 풍속: ").append(String.format("%.1f m/s", windSpeed)).append("\n");

        if (uvIndex != null) {
            sb.append("☀️ 자외선: ").append(getUvIndexDescription(uvIndex)).append("\n");
        }

        if (airQuality != null) {
            sb.append("🏭 대기질: ").append(airQuality).append("\n");
        }

        return sb.toString();
    }

    private String getUvIndexDescription(int uvIndex) {
        if (uvIndex <= 2) return "낮음";
        else if (uvIndex <= 5) return "보통";
        else if (uvIndex <= 7) return "높음";
        else if (uvIndex <= 10) return "매우 높음";
        else return "위험";
    }

    // OpenWeatherMap API 응답 구조
    public static class OpenWeatherResponse {
        private List<DailyForecast> list;
        private City city;

        public OpenWeatherResponse() {}

        public OpenWeatherResponse(List<DailyForecast> list, City city) {
            this.list = list;
            this.city = city;
        }

        // Getter/Setter 메서드
        public List<DailyForecast> getList() { return list; }
        public void setList(List<DailyForecast> list) { this.list = list; }

        public City getCity() { return city; }
        public void setCity(City city) { this.city = city; }

        public static class DailyForecast {
            private Long dt; // Unix timestamp
            private Main main;
            private List<Weather> weather;
            private Integer humidity;
            private Wind wind;

            @JsonProperty("pop")
            private Double precipitationProbability;

            public DailyForecast() {}

            // Getter 메서드들
            public Long getDt() { return dt; }
            public void setDt(Long dt) { this.dt = dt; }

            public Main getMain() { return main; }
            public void setMain(Main main) { this.main = main; }

            public List<Weather> getWeather() { return weather; }
            public void setWeather(List<Weather> weather) { this.weather = weather; }

            public Integer getHumidity() { return humidity; }
            public void setHumidity(Integer humidity) { this.humidity = humidity; }

            public Wind getWind() { return wind; }
            public void setWind(Wind wind) { this.wind = wind; }

            public Double getPrecipitationProbability() { return precipitationProbability; }
            public void setPrecipitationProbability(Double precipitationProbability) { this.precipitationProbability = precipitationProbability; }
        }

        public static class Main {
            @JsonProperty("temp_min")
            private Double tempMin;

            @JsonProperty("temp_max")
            private Double tempMax;

            private Integer humidity;

            public Main() {}

            // Getter 메서드들
            public Double getTempMin() { return tempMin; }
            public void setTempMin(Double tempMin) { this.tempMin = tempMin; }

            public Double getTempMax() { return tempMax; }
            public void setTempMax(Double tempMax) { this.tempMax = tempMax; }

            public Integer getHumidity() { return humidity; }
            public void setHumidity(Integer humidity) { this.humidity = humidity; }
        }

        public static class Weather {
            private String main;
            private String description;
            private String icon;

            public Weather() {}

            // Getter 메서드들
            public String getMain() { return main; }
            public void setMain(String main) { this.main = main; }

            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }

            public String getIcon() { return icon; }
            public void setIcon(String icon) { this.icon = icon; }
        }

        public static class Wind {
            private Double speed;

            public Wind() {}

            // Getter 메서드
            public Double getSpeed() { return speed; }
            public void setSpeed(Double speed) { this.speed = speed; }
        }

        public static class City {
            private String name;
            private String country;

            public City() {}

            // Getter 메서드들
            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getCountry() { return country; }
            public void setCountry(String country) { this.country = country; }
        }
    }
}