package com.weathercalendar.service;

import com.weathercalendar.client.GoogleCalendarClient;
import com.weathercalendar.dto.WeatherData;
import com.weathercalendar.entity.UserSettings;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CalendarService {

    private static final Logger log = LoggerFactory.getLogger(CalendarService.class);
    private final GoogleCalendarClient googleCalendarClient;

    // 생성자 추가
    public CalendarService(GoogleCalendarClient googleCalendarClient) {
        this.googleCalendarClient = googleCalendarClient;
    }

    /**
     * 캘린더 연동 설정 (UC-03)
     */
    public void setupCalendarIntegration(UserSettings userSettings) {
        log.info("Setting up calendar integration for user: {}", userSettings.getEmail());

        try {
            // 기본 캘린더 ID가 설정되지 않은 경우 primary 캘린더 사용
            if (userSettings.getCalendarId() == null) {
                String primaryCalendarId = googleCalendarClient.getPrimaryCalendarId();
                userSettings.setCalendarId(primaryCalendarId);
                log.info("Set primary calendar ID: {}", primaryCalendarId);
            }

            // 캘린더 접근 권한 테스트
            testCalendarAccess(userSettings.getCalendarId());

        } catch (Exception e) {
            log.error("Failed to set up calendar integration for user: {}", userSettings.getEmail(), e);
            throw new RuntimeException("Failed to set up calendar integration", e);
        }
    }

    /**
     * 날씨 정보 캘린더 자동 업데이트 (UC-04)
     */
    public void updateWeatherInCalendar(UserSettings userSettings, List<WeatherData> weatherDataList) {
        log.info("Updating weather information in calendar for user: {}", userSettings.getEmail());

        try {
            String calendarId = userSettings.getCalendarId();

            for (WeatherData weatherData : weatherDataList) {
                // 기존 날씨 이벤트 확인
                String existingEventId = googleCalendarClient.findWeatherEventId(calendarId, weatherData.getDate());

                if (existingEventId != null) {
                    // 기존 이벤트 업데이트
                    googleCalendarClient.updateWeatherEvent(calendarId, existingEventId, weatherData);
                    log.debug("Updated existing weather event for date: {}", weatherData.getDate());
                } else {
                    // 새 이벤트 생성
                    String newEventId = googleCalendarClient.createWeatherEvent(calendarId, weatherData);
                    log.debug("Created new weather event for date: {}, eventId: {}", weatherData.getDate(), newEventId);
                }
            }

            log.info("Successfully updated {} weather events in calendar", weatherDataList.size());

        } catch (Exception e) {
            log.error("Failed to update weather information in calendar for user: {}", userSettings.getEmail(), e);
            throw new RuntimeException("Failed to update calendar with weather information", e);
        }
    }

    /**
     * 캘린더 접근 권한 테스트
     */
    public void testCalendarAccess(String calendarId) {
        try {
            googleCalendarClient.getCalendarList();
            log.info("Calendar access test successful for calendarId: {}", calendarId);
        } catch (Exception e) {
            log.error("Calendar access test failed for calendarId: {}", calendarId, e);
            throw new RuntimeException("Calendar access denied or invalid", e);
        }
    }

    /**
     * 사용자의 캘린더 목록 조회
     */
    public java.util.List<Object> getUserCalendars() {
        try {
            return googleCalendarClient.getCalendarList();
        } catch (Exception e) {
            log.error("Failed to get user calendars", e);
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 특정 날짜 범위의 날씨 이벤트 삭제
     */
    public void deleteWeatherEvents(String calendarId, LocalDate startDate, LocalDate endDate) {
        log.info("Deleting weather events from {} to {} in calendar: {}", startDate, endDate, calendarId);

        try {
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                String eventId = googleCalendarClient.findWeatherEventId(calendarId, currentDate);
                if (eventId != null) {
                    googleCalendarClient.deleteWeatherEvent(calendarId, eventId);
                    log.debug("Deleted weather event for date: {}", currentDate);
                }
                currentDate = currentDate.plusDays(1);
            }

        } catch (Exception e) {
            log.error("Failed to delete weather events", e);
            throw new RuntimeException("Failed to delete weather events", e);
        }
    }

    /**
     * 캘린더 연동 비활성화
     */
    public void disableCalendarIntegration(UserSettings userSettings) {
        log.info("Disabling calendar integration for user: {}", userSettings.getEmail());

        try {
            // 향후 3일간의 날씨 이벤트 삭제
            LocalDate today = LocalDate.now();
            deleteWeatherEvents(userSettings.getCalendarId(), today, today.plusDays(userSettings.getForecastDays() - 1));

            // 자동 업데이트 비활성화
            userSettings.setIsAutoUpdateEnabled(false);

            log.info("Calendar integration disabled for user: {}", userSettings.getEmail());

        } catch (Exception e) {
            log.error("Failed to disable calendar integration for user: {}", userSettings.getEmail(), e);
            throw new RuntimeException("Failed to disable calendar integration", e);
        }
    }
}