package com.weathercalendar.client;

import com.weathercalendar.dto.WeatherData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class GoogleCalendarClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarClient.class);
    private static final String APPLICATION_NAME = "Weather Calendar Notifier";

    @Value("${google.calendar.credentials-file:credentials.json}")
    private String credentialsFilePath;

    @Value("${google.calendar.tokens-directory:tokens}")
    private String tokensDirectoryPath;

    /**
     * 임시로 Google Calendar 기능을 비활성화합니다.
     * credentials.json 파일이 없어도 애플리케이션이 시작됩니다.
     */
    public String createWeatherEvent(String calendarId, WeatherData weatherData) {
        log.info("Google Calendar integration is temporarily disabled. Would create event for: {}", weatherData.getDate());
        // 실제 구현은 credentials.json 설정 후 활성화
        return "temp-event-id";
    }

    public void updateWeatherEvent(String calendarId, String eventId, WeatherData weatherData) {
        log.info("Google Calendar integration is temporarily disabled. Would update event for: {}", weatherData.getDate());
    }

    public String findWeatherEventId(String calendarId, LocalDate date) {
        log.info("Google Calendar integration is temporarily disabled. Would find event for: {}", date);
        return null;
    }

    public List<Object> getCalendarList() {
        log.info("Google Calendar integration is temporarily disabled.");
        return new ArrayList<>();
    }

    public String getPrimaryCalendarId() {
        return "primary";
    }

    public void deleteWeatherEvent(String calendarId, String eventId) {
        log.info("Google Calendar integration is temporarily disabled. Would delete event: {}", eventId);
    }
}