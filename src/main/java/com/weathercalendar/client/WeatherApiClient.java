package com.weathercalendar.client;

import com.weathercalendar.dto.WeatherData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class WeatherApiClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiClient.class);
    private final WebClient webClient;

    // 생성자 추가
    public WeatherApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${weather.api.openweather.url}")
    private String apiUrl;

    @Value("${weather.api.openweather.key}")
    private String apiKey;

    /**
     * 위치명으로 날씨 예보 조회
     */
    public Mono<List<WeatherData>> getWeatherForecast(String location, int days) {
        log.info("Fetching weather forecast for location: {}, days: {}", location, days);

        String url = String.format("%s/forecast?q=%s&appid=%s&units=metric&cnt=%d",
                apiUrl, location, apiKey, days * 8); // 3시간 간격 데이터 (8개 = 1일)

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherData.OpenWeatherResponse.class)
                .map(this::convertToWeatherDataList)
                .doOnSuccess(data -> log.info("Successfully fetched weather data for {} items", data.size()))
                .doOnError(error -> log.error("Failed to fetch weather data for location: {}", location, error));
    }

    /**
     * 좌표로 날씨 예보 조회
     */
    public Mono<List<WeatherData>> getWeatherForecast(double lat, double lon, int days) {
        log.info("Fetching weather forecast for coordinates: {}, {}, days: {}", lat, lon, days);

        String url = String.format("%s/forecast?lat=%f&lon=%f&appid=%s&units=metric&cnt=%d",
                apiUrl, lat, lon, apiKey, days * 8);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherData.OpenWeatherResponse.class)
                .map(this::convertToWeatherDataList)
                .doOnSuccess(data -> log.info("Successfully fetched weather data for {} items", data.size()))
                .doOnError(error -> log.error("Failed to fetch weather data for coordinates: {}, {}", lat, lon, error));
    }

    /**
     * 현재 날씨 조회
     */
    public Mono<WeatherData> getCurrentWeather(String location) {
        log.info("Fetching current weather for location: {}", location);

        String url = String.format("%s/weather?q=%s&appid=%s&units=metric",
                apiUrl, location, apiKey);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherData.OpenWeatherResponse.DailyForecast.class)
                .map(forecast -> convertToWeatherData(forecast, location))
                .doOnSuccess(data -> log.info("Successfully fetched current weather data"))
                .doOnError(error -> log.error("Failed to fetch current weather for location: {}", location, error));
    }

    /**
     * OpenWeatherMap API 응답을 WeatherData 리스트로 변환
     */
    private List<WeatherData> convertToWeatherDataList(WeatherData.OpenWeatherResponse response) {
        if (response == null || response.getList() == null) {
            return new ArrayList<>();
        }

        String location = response.getCity() != null ?
                response.getCity().getName() + ", " + response.getCity().getCountry() : "Unknown";

        // 일별로 그룹핑하여 대표 데이터 선택 (정오 시간대 우선)
        return response.getList().stream()
                .collect(Collectors.groupingBy(forecast -> {
                    LocalDate date = Instant.ofEpochSecond(forecast.getDt())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return date;
                }))
                .entrySet().stream()
                .map(entry -> {
                    LocalDate date = entry.getKey();
                    List<WeatherData.OpenWeatherResponse.DailyForecast> dayForecasts = entry.getValue();

                    // 하루 중 대표 예보 선택 (가능하면 정오 시간대)
                    WeatherData.OpenWeatherResponse.DailyForecast representativeForecast =
                            dayForecasts.stream()
                                    .min((f1, f2) -> {
                                        int hour1 = Instant.ofEpochSecond(f1.getDt())
                                                .atZone(ZoneId.systemDefault()).getHour();
                                        int hour2 = Instant.ofEpochSecond(f2.getDt())
                                                .atZone(ZoneId.systemDefault()).getHour();
                                        return Integer.compare(Math.abs(hour1 - 12), Math.abs(hour2 - 12));
                                    })
                                    .orElse(dayForecasts.get(0));

                    return convertToWeatherData(representativeForecast, location, date);
                })
                .collect(Collectors.toList());
    }

    /**
     * 단일 예보 데이터 변환
     */
    private WeatherData convertToWeatherData(WeatherData.OpenWeatherResponse.DailyForecast forecast, String location) {
        LocalDate date = Instant.ofEpochSecond(forecast.getDt())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return convertToWeatherData(forecast, location, date);
    }

    /**
     * 단일 예보 데이터 변환 (날짜 지정)
     */
    private WeatherData convertToWeatherData(WeatherData.OpenWeatherResponse.DailyForecast forecast,
                                             String location, LocalDate date) {
        WeatherData.OpenWeatherResponse.Weather weather =
                forecast.getWeather() != null && !forecast.getWeather().isEmpty() ?
                        forecast.getWeather().get(0) : null;

        return WeatherData.builder()
                .location(location)
                .date(date)
                .description(weather != null ? weather.getDescription() : "알 수 없음")
                .icon(weather != null ? weather.getIcon() : "")
                .tempMin(forecast.getMain() != null ? forecast.getMain().getTempMin() : 0.0)
                .tempMax(forecast.getMain() != null ? forecast.getMain().getTempMax() : 0.0)
                .humidity(forecast.getHumidity() != null ? forecast.getHumidity() : 0)
                .precipitationProbability(forecast.getPrecipitationProbability() != null ?
                        (int)(forecast.getPrecipitationProbability() * 100) : 0)
                .windSpeed(forecast.getWind() != null ? forecast.getWind().getSpeed() : 0.0)
                .uvIndex(null) // OpenWeatherMap 기본 API에서는 UV 인덱스 미제공
                .airQuality(null) // 별도 API 호출 필요
                .build();
    }
}