package com.weathercalendar.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_settings")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId; // 구글 계정 ID

    @Column(nullable = false)
    private String email;

    @Column(name = "calendar_id")
    private String calendarId; // 기본 캘린더 ID

    @Column(name = "default_location")
    private String defaultLocation; // 기본 위치 (예: "Seoul,KR")

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "is_auto_update_enabled")
    @Builder.Default
    private Boolean isAutoUpdateEnabled = true;

    @Column(name = "forecast_days")
    @Builder.Default
    private Integer forecastDays = 3; // 향후 예보 일수

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getter/Setter 메서드들
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getCalendarId() { return calendarId; }
    public void setCalendarId(String calendarId) { this.calendarId = calendarId; }

    public String getDefaultLocation() { return defaultLocation; }
    public void setDefaultLocation(String defaultLocation) { this.defaultLocation = defaultLocation; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Boolean getIsAutoUpdateEnabled() { return isAutoUpdateEnabled; }
    public void setIsAutoUpdateEnabled(Boolean isAutoUpdateEnabled) { this.isAutoUpdateEnabled = isAutoUpdateEnabled; }

    public Integer getForecastDays() { return forecastDays; }
    public void setForecastDays(Integer forecastDays) { this.forecastDays = forecastDays; }

    // Builder 메서드
    public static UserSettingsBuilder builder() {
        return new UserSettingsBuilder();
    }

    // Builder 클래스
    public static class UserSettingsBuilder {
        private String userId;
        private String email;
        private String calendarId;
        private String defaultLocation;
        private Double latitude;
        private Double longitude;
        private Boolean isAutoUpdateEnabled = true;
        private Integer forecastDays = 3;

        public UserSettingsBuilder userId(String userId) { this.userId = userId; return this; }
        public UserSettingsBuilder email(String email) { this.email = email; return this; }
        public UserSettingsBuilder calendarId(String calendarId) { this.calendarId = calendarId; return this; }
        public UserSettingsBuilder defaultLocation(String defaultLocation) { this.defaultLocation = defaultLocation; return this; }
        public UserSettingsBuilder latitude(Double latitude) { this.latitude = latitude; return this; }
        public UserSettingsBuilder longitude(Double longitude) { this.longitude = longitude; return this; }
        public UserSettingsBuilder isAutoUpdateEnabled(Boolean isAutoUpdateEnabled) { this.isAutoUpdateEnabled = isAutoUpdateEnabled; return this; }
        public UserSettingsBuilder forecastDays(Integer forecastDays) { this.forecastDays = forecastDays; return this; }

        public UserSettings build() {
            UserSettings userSettings = new UserSettings();
            userSettings.userId = this.userId;
            userSettings.email = this.email;
            userSettings.calendarId = this.calendarId;
            userSettings.defaultLocation = this.defaultLocation;
            userSettings.latitude = this.latitude;
            userSettings.longitude = this.longitude;
            userSettings.isAutoUpdateEnabled = this.isAutoUpdateEnabled;
            userSettings.forecastDays = this.forecastDays;
            return userSettings;
        }
    }
}