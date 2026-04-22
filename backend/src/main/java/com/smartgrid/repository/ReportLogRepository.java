package com.smartgrid.repository;

import com.smartgrid.entity.ReportLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportLogRepository extends JpaRepository<ReportLog, Long> {
    List<ReportLog> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);
    List<ReportLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}
