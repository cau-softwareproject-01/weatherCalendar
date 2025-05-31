package com.weathercalendar.service;

import com.weathercalendar.dto.WeatherData;
import com.weathercalendar.entity.UserSettings;
import com.weathercalendar.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "scheduler.weather-update.enabled", havingValue = "true", matchIfMissing = true)
public class SchedulerService {

    private static final Logger log = LoggerFactory.getLogger(SchedulerService.class);
    private final UserSettingsRepository userSettingsRepository;
    private final WeatherService weatherService;
    private final CalendarService calendarService;

    // 생성자 추가
    public SchedulerService(UserSettingsRepository userSettingsRepository,
                            WeatherService weatherService,
                            CalendarService calendarService) {
        this.userSettingsRepository = userSettingsRepository;
        this.weatherService = weatherService;
        this.calendarService = calendarService;
    }

    /**
     * 날씨 정보 자동 업데이트 스케줄러 (UC-04)
     * 매일 오전 1시에 실행
     */
    @Scheduled(cron = "${scheduler.weather-update.cron:0 0 1 * * ?}")
    public void scheduleWeatherUpdate() {
        log.info("Starting scheduled weather update process");

        try {
            // 자동 업데이트가 활성화된 사용자 목록 조회
            List<UserSettings> activeUsers = userSettingsRepository.findActiveUsersForScheduledUpdate();
            log.info("Found {} active users for weather update", activeUsers.size());

            if (activeUsers.isEmpty()) {
                log.info("No active users found, skipping weather update");
                return;
            }

            // 각 사용자별로 순차 처리 (비동기 제거)
            for (UserSettings userSettings : activeUsers) {
                updateWeatherForUserSync(userSettings);
            }

            log.info("Completed scheduled weather update for all users");

        } catch (Exception e) {
            log.error("Error occurred during scheduled weather update", e);
        }
    }

    /**
     * 개별 사용자의 날씨 정보 업데이트 (동기 처리)
     */
    public void updateWeatherForUserSync(UserSettings userSettings) {
        log.info("Updating weather for user: {}", userSettings.getEmail());

        try {
            // 1. 날씨 정보 조회
            List<WeatherData> weatherDataList = weatherService.getWeatherForecastForUser(userSettings)
                    .block(); // 동기 처리로 변환

            if (weatherDataList == null || weatherDataList.isEmpty()) {
                log.warn("No weather data available for user: {}", userSettings.getEmail());
                return;
            }

            // 2. 캘린더에 날씨 정보 업데이트
            calendarService.updateWeatherInCalendar(userSettings, weatherDataList);

            log.info("Successfully updated weather for user: {} with {} events",
                    userSettings.getEmail(), weatherDataList.size());

        } catch (Exception e) {
            log.error("Failed to update weather for user: {}", userSettings.getEmail(), e);
            // 개별 사용자 실패가 전체 프로세스를 중단시키지 않도록 예외를 잡음
        }
    }

    /**
     * 수동 날씨 업데이트 트리거
     */
    public void triggerManualWeatherUpdate(String userId) {
        log.info("Triggering manual weather update for user: {}", userId);

        userSettingsRepository.findByUserId(userId)
                .ifPresentOrElse(
                        userSettings -> {
                            if (userSettings.getIsAutoUpdateEnabled()) {
                                updateWeatherForUserSync(userSettings);
                            } else {
                                log.warn("Auto update is disabled for user: {}", userId);
                            }
                        },
                        () -> log.warn("User settings not found for userId: {}", userId)
                );
    }

    /**
     * 전체 사용자 수동 업데이트 트리거 (관리자 기능)
     */
    public void triggerManualWeatherUpdateForAllUsers() {
        log.info("Triggering manual weather update for all active users");
        scheduleWeatherUpdate();
    }

    /**
     * 스케줄러 상태 확인
     */
    public String getSchedulerStatus() {
        List<UserSettings> activeUsers = userSettingsRepository.findActiveUsersForScheduledUpdate();
        return String.format("Scheduler is active. %d users configured for automatic updates.", activeUsers.size());
    }

    /**
     * 시스템 헬스 체크 (매 시간마다 실행)
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void healthCheck() {
        try {
            int activeUserCount = userSettingsRepository.findActiveUsersForScheduledUpdate().size();
            log.info("System health check - Active users: {}", activeUserCount);
        } catch (Exception e) {
            log.error("Health check failed", e);
        }
    }
}