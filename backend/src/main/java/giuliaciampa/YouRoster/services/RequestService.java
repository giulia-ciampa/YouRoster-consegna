package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.responses.ChangeRequestResponseDTO;
import giuliaciampa.YouRoster.entities.*;
import giuliaciampa.YouRoster.repositories.ChangeRequestRepository;
import giuliaciampa.YouRoster.repositories.RequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final ChangeRequestRepository changeRequestRepository;

    public RequestService(RequestRepository requestRepository, ChangeRequestRepository changeRequestRepository) {
        this.requestRepository = requestRepository;
        this.changeRequestRepository = changeRequestRepository;
    }

    //METODO HELPER PER LA RISPOSTA RICHIESTE NORMALI(non modificate) METODO ADMIN E SHIFT MANAGER
    private ChangeRequestResponseDTO mapToResponse(Request request) {

        String employeeName = request.getEmployee() != null
                ? request.getEmployee().getName() + " " + request.getEmployee().getSurname()
                : null;

        String reviewerName = null;

        if (request.getReviewer() != null) {
            reviewerName = request.getReviewer().getName()
                    + " "
                    + request.getReviewer().getSurname();
        }

        RequestType requestType = null;

        switch (request) {
            case HolidayRequest holidayRequest -> requestType = RequestType.HOLIDAY;
            case LeaveHoursRequest leaveHoursRequest -> requestType = RequestType.LEAVE_HOURS;
            case AbsenceCertificationRequest absenceCertificationRequest -> requestType = RequestType.CERTIFICATION;


            default -> {
            }
        }


        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate date = null;
        LocalTime startTime = null;
        LocalTime endTime = null;

        switch (request) {
            case HolidayRequest holidayRequest -> {

                startDate = holidayRequest.getStartDate();
                endDate = holidayRequest.getEndDate();
            }
            case AbsenceCertificationRequest certificationRequest -> {

                startDate = certificationRequest.getStartDate();
                endDate = certificationRequest.getEndDate();
            }
            case LeaveHoursRequest leaveHoursRequest -> {

                date = leaveHoursRequest.getDate();
                startTime = leaveHoursRequest.getStartTime();
                endTime = leaveHoursRequest.getEndTime();
            }
            default -> {
            }
        }

        Integer totalDays = null;
        BigDecimal totalHours = null;

        switch (request) {
            case HolidayRequest holidayRequest -> totalDays = holidayRequest.getTotalDaysRequested();
            case AbsenceCertificationRequest certificationRequest -> totalDays = certificationRequest.getTotalDays();
            case LeaveHoursRequest leaveHoursRequest -> totalHours = leaveHoursRequest.getTotalHours();
            default -> {
            }
        }

        String protocolCode = null;
        CertificateType certificateType = null;

        if (request instanceof AbsenceCertificationRequest certificationRequest) {
            protocolCode = certificationRequest.getProtocolCode();
            certificateType = certificationRequest.getCertificateType();
        }
        System.out.println("REQUEST TYPE FINALE = " + requestType);
        System.out.println("REQUEST CLASS = " + request.getClass().getSimpleName());
        System.out.println("REQUEST TYPE FINALE = " + requestType);
        System.out.println("CERTIFICATE TYPE = " + certificateType);
        return new ChangeRequestResponseDTO(request.getId(), request.getEmployee().getName() + " " + request.getEmployee().getSurname(), request.getRequestStatus(), requestType, request.getCreatedAt(), request.getEmployeeNotes(), request.getReviewerNotes(), reviewerName, startDate, endDate, startTime, endTime, date, totalHours, totalDays, protocolCode, null, null, null, null, request.getResponseDate(), null, null, null, null, null, null, null, certificateType);
    }

    //HELPER PER GET TUTTE LE RICHIESTE
    private ChangeRequestResponseDTO mapChangeRequestToResponse(ChangeRequest request) {

        String employeeName = request.getOriginalRequest().getEmployee().getName()
                + " "
                + request.getOriginalRequest().getEmployee().getSurname();

        String reviewerName = null;

        if (request.getReviewer() != null) {
            reviewerName = request.getReviewer().getName()
                    + " "
                    + request.getReviewer().getSurname();
        }

        RequestType requestType = null;

        switch (request) {
            case ChangeHolidayRequest changeHolidayRequest -> requestType = RequestType.CHANGE_HOLIDAY;

            case ChangeLeaveHoursRequest changeLeaveHoursRequest -> requestType = RequestType.CHANGE_LEAVE_HOURS;

            default -> {
            }
        }

        LocalDate startDate = null;
        LocalDate endDate = null;
        LocalDate date = null;
        LocalTime startTime = null;
        LocalTime endTime = null;

        switch (request) {
            case ChangeHolidayRequest changeHolidayRequest -> {
                startDate = changeHolidayRequest.getStartDate();
                endDate = changeHolidayRequest.getEndDate();
            }
            case ChangeLeaveHoursRequest changeLeaveHoursRequest -> {
                date = changeLeaveHoursRequest.getDate();
                startTime = changeLeaveHoursRequest.getStartTime();
                endTime = changeLeaveHoursRequest.getEndTime();
            }
            default -> {
            }
        }

        BigDecimal totalHours = null;
        Integer totalDays = null;

        switch (request) {
            case ChangeHolidayRequest changeHolidayRequest -> totalDays = changeHolidayRequest.getTotalDays();
            case ChangeLeaveHoursRequest changeLeaveHoursRequest ->
                    totalHours = changeLeaveHoursRequest.getTotalHours();
            default -> {
            }
        }


        LocalDate originalResponseDate = null;
        LocalDate startDateOriginalRequest = null;
        LocalDate endDateOriginalRequest = null;
        Integer originalRequestTotalDays = null;
        String originalRequestEmployeeNotes = null;
        String originalReviewerNotes = null;
        BigDecimal originalRequestTotalHours = null;
        LocalDate originalDate = null;
        LocalTime startTimeOriginalRequest = null;
        LocalTime endTimeOriginalRequest = null;

        if (request instanceof ChangeHolidayRequest changeHolidayRequest &&
                changeHolidayRequest.getOriginalRequest() instanceof HolidayRequest originalHolidayRequest) {


            originalResponseDate = originalHolidayRequest.getResponseDate();
            startDateOriginalRequest = originalHolidayRequest.getStartDate();
            endDateOriginalRequest = originalHolidayRequest.getEndDate();
            originalRequestTotalDays = originalHolidayRequest.getTotalDaysRequested();
            originalRequestEmployeeNotes = originalHolidayRequest.getEmployeeNotes();
            originalReviewerNotes = originalHolidayRequest.getReviewerNotes();
        }

        if (request instanceof ChangeLeaveHoursRequest changeLeaveHoursRequest && changeLeaveHoursRequest.getOriginalRequest() instanceof LeaveHoursRequest originalLeaveHoursRequest) {
            originalRequestTotalHours = originalLeaveHoursRequest.getTotalHours();
            originalDate = originalLeaveHoursRequest.getDate();
            startTimeOriginalRequest = originalLeaveHoursRequest.getStartTime();
            endTimeOriginalRequest = originalLeaveHoursRequest.getEndTime();
        }


        return new ChangeRequestResponseDTO(request.getId(), request.getOriginalRequest().getEmployee().getName() + " " + request.getOriginalRequest().getEmployee().getSurname(), request.getChangeRequestStatus(), requestType, request.getCreatedAt(), request.getEmployeeNotes(), request.getReviewerNotes(), reviewerName, startDate, endDate, startTime, endTime, date, totalHours, totalDays, null, request.getOriginalRequest().getCreatedAt(), endDateOriginalRequest, startDateOriginalRequest, originalResponseDate, request.getResponseDate(), originalRequestEmployeeNotes, originalReviewerNotes, originalRequestTotalDays, originalRequestTotalHours, originalDate, startTimeOriginalRequest, endTimeOriginalRequest, null);
    }


    //METODO REQUESTS NORMALI FOR SHIFT MANAGER AND ADMIN
    public Page<ChangeRequestResponseDTO> getRequestsForShiftManager(Pageable pageable) {

        System.out.println(">>> INIZIO GET REQUESTS");

        Page<Request> requests = requestRepository.findRequestsForShiftManager(
                RequestStatus.APPROVED,
                pageable
        );

        System.out.println(">>> REPOSITORY OK");

        Page<ChangeRequestResponseDTO> response =
                requests.map(this::mapToResponse);

        System.out.println(">>> MAPPING OK");

        return response;
    }

    //METODO GET REQUESTS NORMALI E CON LE MODIFICHE
    public Page<ChangeRequestResponseDTO> getAllRequests(Pageable pageable) {

        // Richieste normali
        Page<Request> requestPage = requestRepository.findRequestsForShiftManager(
                RequestStatus.APPROVED,
                Pageable.unpaged()
        );

        List<ChangeRequestResponseDTO> requests = requestPage
                .getContent()
                .stream()
                .map(this::mapToResponse)
                .toList();

        // Richieste di modifica
        List<ChangeRequestResponseDTO> changeRequests = changeRequestRepository
                .findAll()
                .stream()
                .map(this::mapChangeRequestToResponse)
                .toList();

        // Unisco
        List<ChangeRequestResponseDTO> allRequests = new ArrayList<>();
        allRequests.addAll(requests);
        allRequests.addAll(changeRequests);

        // Ordino dalla più recente alla più vecchia
        allRequests.sort(
                Comparator.comparing(
                        ChangeRequestResponseDTO::createdAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                )
        );

        // Paginazione finale
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allRequests.size());

        List<ChangeRequestResponseDTO> pageContent =
                start >= allRequests.size()
                        ? List.of()
                        : allRequests.subList(start, end);

        return new PageImpl<>(
                pageContent,
                pageable,
                allRequests.size()
        );
    }


}