package com.weathercalendar.controller;

import com.weathercalendar.dto.WeatherData;
import com.weathercalendar.entity.UserSettings;
import com.weathercalendar.repository.UserSettingsRepository;
import com.weathercalendar.service.CalendarService;
import com.weathercalendar.service.SchedulerService;
import com.weathercalendar.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);
    private final WeatherService weatherService;
    private final CalendarService calendarService;
    private final SchedulerService schedulerService;
    private final UserSettingsRepository userSettingsRepository;

    // 생성자
    public WeatherController(WeatherService weatherService, CalendarService calendarService,
                             SchedulerService schedulerService, UserSettingsRepository userSettingsRepository) {
        this.weatherService = weatherService;
        this.calendarService = calendarService;
        this.schedulerService = schedulerService;
        this.userSettingsRepository = userSettingsRepository;
    }

    /**
     * 메인 페이지
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "캘린더 연동 날씨 알리미");
        return "index";
    }

    /**
     * 현재 위치 날씨 조회 (UC-01)
     */
    @GetMapping("/api/weather/current")
    @ResponseBody
    public ResponseEntity<WeatherData> getCurrentWeather(@RequestParam String location) {
        log.info("Request for current weather: {}", location);

        try {
            WeatherData weatherData = weatherService.getCurrentWeather(location).block();
            return ResponseEntity.ok(weatherData);
        } catch (Exception e) {
            log.error("Failed to get current weather for location: {}", location, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 특정 위치 날씨 검색 및 조회 (UC-02)
     */
    @GetMapping("/api/weather/forecast")
    @ResponseBody
    public ResponseEntity<List<WeatherData>> getWeatherForecast(
            @RequestParam String location,
            @RequestParam(defaultValue = "3") int days) {
        log.info("Request for weather forecast: {}, days: {}", location, days);

        try {
            List<WeatherData> weatherDataList = weatherService.getWeatherForecast(location, days).block();
            return ResponseEntity.ok(weatherDataList);
        } catch (Exception e) {
            log.error("Failed to get weather forecast for location: {}", location, e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 설정 페이지
     */
    @GetMapping("/settings")
    public String settings(Model model) {
        // 임시로 기본 사용자 설정 생성 (실제로는 OAuth 인증 후 사용자 정보 사용)
        UserSettings userSettings = UserSettings.builder()
                .userId("default-user")
                .email("user@example.com")
                .defaultLocation("Seoul,KR")
                .isAutoUpdateEnabled(true)
                .forecastDays(3)
                .build();

        model.addAttribute("userSettings", userSettings);
        model.addAttribute("calendars", calendarService.getUserCalendars());
        return "settings";
    }

    /**
     * 캘린더 연동 설정 (UC-03)
     */
    @PostMapping("/api/settings/calendar")
    @ResponseBody
    public ResponseEntity<String> setupCalendarIntegration(@RequestBody UserSettings userSettings) {
        log.info("Setting up calendar integration for user: {}", userSettings.getEmail());

        try {
            // 사용자 설정 저장
            UserSettings savedSettings = userSettingsRepository.save(userSettings);

            // 캘린더 연동 설정
            calendarService.setupCalendarIntegration(savedSettings);

            return ResponseEntity.ok("Calendar integration setup successful");
        } catch (Exception e) {
            log.error("Failed to setup calendar integration", e);
            return ResponseEntity.badRequest().body("Failed to setup calendar integration: " + e.getMessage());
        }
    }

    /**
     * 수동 날씨 업데이트 트리거
     */
    @PostMapping("/api/weather/update")
    @ResponseBody
    public ResponseEntity<String> triggerWeatherUpdate(@RequestParam String userId) {
        log.info("Manual weather update triggered for user: {}", userId);

        try {
            schedulerService.triggerManualWeatherUpdate(userId);
            return ResponseEntity.ok("Weather update triggered successfully");
        } catch (Exception e) {
            log.error("Failed to trigger weather update", e);
            return ResponseEntity.badRequest().body("Failed to trigger weather update: " + e.getMessage());
        }
    }

    /**
     * 스케줄러 상태 조회
     */
    @GetMapping("/api/scheduler/status")
    @ResponseBody
    public ResponseEntity<String> getSchedulerStatus() {
        try {
            String status = schedulerService.getSchedulerStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to get scheduler status", e);
            return ResponseEntity.badRequest().body("Failed to get scheduler status");
        }
    }

    /**
     * 사용자 설정 조회
     */
    @GetMapping("/api/settings/{userId}")
    @ResponseBody
    public ResponseEntity<UserSettings> getUserSettings(@PathVariable String userId) {
        Optional<UserSettings> userSettings = userSettingsRepository.findByUserId(userId);
        return userSettings.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 위치 유효성 검증
     */
    @GetMapping("/api/weather/validate")
    @ResponseBody
    public ResponseEntity<Boolean> validateLocation(@RequestParam String location) {
        try {
            Boolean isValid = weatherService.validateLocation(location).block();
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Failed to validate location: {}", location, e);
            return ResponseEntity.ok(false);
        }
    }
}