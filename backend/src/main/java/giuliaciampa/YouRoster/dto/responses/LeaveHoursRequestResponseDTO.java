package giuliaciampa.YouRoster.dto.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import giuliaciampa.YouRoster.entities.LeaveHoursType;
import giuliaciampa.YouRoster.entities.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record LeaveHoursRequestResponseDTO(
        UUID id,
        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate date,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
        LeaveHoursType leaveHoursType,
        String employeeNotes,
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
        LocalDateTime createdAt,
        RequestStatus requestStatus,
        String reviewerNotes,
        BigDecimal totalHours,
        String employeeName

) {
}
