package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.AttendanceStatus;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;

public record ClockingResponseDTO(
        UUID id,
        UUID shiftAssignmentId,
        String officeName,

        @JsonFormat(pattern = "HH:mm")
        LocalTime actualStartTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime actualEndTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime shiftStartTime,

        @JsonFormat(pattern = "HH:mm")
        LocalTime shiftEndTime,

        AttendanceStatus attendanceStatus,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean positionValid,
        String note,
        Integer lateMinutes,
        Integer workedMinutes,
        String workedHours,
        Integer earlyDepartureMinutes,
        Integer balanceMinutes
) {
}
