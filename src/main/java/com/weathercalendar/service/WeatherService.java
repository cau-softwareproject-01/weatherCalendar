package com.weathercalendar.service;

import com.weathercalendar.client.WeatherApiClient;
import com.weathercalendar.dto.WeatherData;
import com.weathercalendar.entity.UserSettings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);
    private final WeatherApiClient weatherApiClient;

    // 생성자 추가
    public WeatherService(WeatherApiClient weatherApiClient) {
        this.weatherApiClient = weatherApiClient;
    }

    /**
     * 현재 위치 날씨 조회 (UC-01)
     */
    public Mono<WeatherData> getCurrentWeather(String location) {
        log.info("Getting current weather for location: {}", location);
        return weatherApiClient.getCurrentWeather(location);
    }

    /**
     * 특정 위치 날씨 검색 및 조회 (UC-02)
     */
    public Mono<List<WeatherData>> getWeatherForecast(String location, int days) {
        log.info("Getting weather forecast for location: {}, days: {}", location, days);
        return weatherApiClient.getWeatherForecast(location, days);
    }

    /**
     * 좌표 기반 날씨 예보 조회
     */
    public Mono<List<WeatherData>> getWeatherForecast(double latitude, double longitude, int days) {
        log.info("Getting weather forecast for coordinates: {}, {}, days: {}", latitude, longitude, days);
        return weatherApiClient.getWeatherForecast(latitude, longitude, days);
    }

    /**
     * 사용자 설정 기반 날씨 예보 조회
     */
    public Mono<List<WeatherData>> getWeatherForecastForUser(UserSettings userSettings) {
        log.info("Getting weather forecast for user: {}", userSettings.getEmail());

        // 좌표가 설정되어 있으면 좌표 기반 조회, 아니면 위치명 기반 조회
        if (userSettings.getLatitude() != null && userSettings.getLongitude() != null) {
            return getWeatherForecast(
                    userSettings.getLatitude(),
                    userSettings.getLongitude(),
                    userSettings.getForecastDays()
            );
        } else if (userSettings.getDefaultLocation() != null) {
            return getWeatherForecast(
                    userSettings.getDefaultLocation(),
                    userSettings.getForecastDays()
            );
        } else {
            log.warn("No location information available for user: {}", userSettings.getEmail());
            return Mono.empty();
        }
    }

    /**
     * 위치명 검색 및 유효성 검증
     */
    public Mono<Boolean> validateLocation(String location) {
        log.info("Validating location: {}", location);
        return weatherApiClient.getCurrentWeather(location)
                .map(weatherData -> true)
                .onErrorReturn(false);
    }

    /**
     * 날씨 데이터 캐싱 (추후 구현을 위한 메서드)
     */
    public void cacheWeatherData(String location, List<WeatherData> weatherDataList) {
        // Redis 또는 다른 캐싱 솔루션을 사용하여 구현 가능
        log.debug("Caching weather data for location: {} with {} items", location, weatherDataList.size());
    }

    /**
     * 캐시된 날씨 데이터 조회 (추후 구현을 위한 메서드)
     */
    public Mono<List<WeatherData>> getCachedWeatherData(String location) {
        // 캐시에서 데이터 조회 로직 구현
        log.debug("Getting cached weather data for location: {}", location);
        return Mono.empty(); // 현재는 빈 결과 반환
    }
}