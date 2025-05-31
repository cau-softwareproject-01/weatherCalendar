package com.weathercalendar.repository;

import com.weathercalendar.entity.UserSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, Long> {

    /**
     * 사용자 ID로 설정 조회
     */
    Optional<UserSettings> findByUserId(String userId);

    /**
     * 이메일로 설정 조회
     */
    Optional<UserSettings> findByEmail(String email);

    /**
     * 자동 업데이트가 활성화된 모든 사용자 설정 조회
     */
    List<UserSettings> findByIsAutoUpdateEnabledTrue();

    /**
     * 특정 위치를 사용하는 사용자 설정 조회
     */
    List<UserSettings> findByDefaultLocation(String defaultLocation);

    /**
     * 자동 업데이트가 활성화되고 캘린더 ID가 설정된 사용자 설정 조회
     */
    @Query("SELECT u FROM UserSettings u WHERE u.isAutoUpdateEnabled = true AND u.calendarId IS NOT NULL")
    List<UserSettings> findActiveUsersForScheduledUpdate();

    /**
     * 사용자 ID 존재 여부 확인
     */
    boolean existsByUserId(String userId);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}