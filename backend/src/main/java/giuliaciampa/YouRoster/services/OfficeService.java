package giuliaciampa.YouRoster.services;

import giuliaciampa.YouRoster.dto.requests.OfficeDTO;
import giuliaciampa.YouRoster.dto.requests.UpdateOfficeDTO;
import giuliaciampa.YouRoster.dto.responses.OfficeResponseDTO;
import giuliaciampa.YouRoster.entities.Office;
import giuliaciampa.YouRoster.entities.OfficeStatus;
import giuliaciampa.YouRoster.exceptions.BadRequestException;
import giuliaciampa.YouRoster.exceptions.NotFoundException;
import giuliaciampa.YouRoster.repositories.OfficeRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
public class OfficeService {

    private final OfficeRepository officeRepository;

    public OfficeService(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    //CREA SEDI DI DEFAULT
    public void createDefaultOffices(String name, String street, String houseNumber, String zipCode, String city, String province, LocalTime openingTime, LocalTime closingTime, BigDecimal latitude, BigDecimal longitude) {
        //1. controllo che la sede non esiste già

        if (officeRepository.existsByName(name)) {
            return;
        }

        //2. creo la sede
        Office newOffice = new Office();
        newOffice.setName(name);
        newOffice.setStreet(street);
        newOffice.setHouseNumber(houseNumber);
        newOffice.setZipCode(zipCode);
        newOffice.setCity(city);
        newOffice.setProvince(province);
        newOffice.setOpeningTime(openingTime);
        newOffice.setClosingTime(closingTime);
        newOffice.setLongitude(longitude);
        newOffice.setLatitude(latitude);


        officeRepository.save(newOffice);
    }

    //FIND BY ID
    public Office findById(UUID id) {
        return officeRepository.findById(id).orElseThrow(() -> new NotFoundException("L'ufficio con id " + id + " non è stato trovato"));
    }

    //FIND BY NAME
    public Office findByName(String name) {
        return officeRepository.findByNameIgnoreCase(name).orElseThrow(() -> new NotFoundException("L'ufficio con il nome " + name + " non esiste."));
    }

    //---------------------------------------------------------------------

    //1. CREA NUOVA SEDE

    public OfficeResponseDTO saveNewOffice(OfficeDTO payload) {

        Office newOffice = new Office();

        newOffice.setName(payload.name());
        newOffice.setStreet(payload.street());
        newOffice.setHouseNumber(payload.houseNumber());
        newOffice.setZipCode(payload.zipCode());
        newOffice.setCity(payload.city());
        newOffice.setProvince(payload.province());
        newOffice.setOpeningTime(payload.openingTime());
        newOffice.setClosingTime(payload.closingTime());
        newOffice.setLatitude(payload.latitude());
        newOffice.setLongitude(payload.longitude());

        Office savedOffice = officeRepository.save(newOffice);

        return new OfficeResponseDTO(savedOffice.getId(), savedOffice.getName(), savedOffice.getStreet(), savedOffice.getHouseNumber(), savedOffice.getZipCode(), savedOffice.getCity(), savedOffice.getProvince(), savedOffice.getOpeningTime(), savedOffice.getClosingTime(), savedOffice.getStatus(), savedOffice.getLatitude(), savedOffice.getLongitude());
    }

    //2. MODIFICA SEDE
    public OfficeResponseDTO updateOffice(UUID id, UpdateOfficeDTO payload) {
        Office existingOffice = findById(id);

        if (payload.name() != null && !payload.name().isBlank()) {
            existingOffice.setName(payload.name());
        }
        if (payload.street() != null && !payload.street().isBlank()) {
            existingOffice.setStreet(payload.street());
        }
        if (payload.houseNumber() != null && !payload.houseNumber().isBlank()) {
            existingOffice.setHouseNumber(payload.houseNumber());
        }
        if (payload.zipCode() != null && !payload.zipCode().isBlank()) {
            existingOffice.setZipCode(payload.zipCode());
        }
        if (payload.city() != null && !payload.city().isBlank()) {
            existingOffice.setCity(payload.city());
        }
        if (payload.province() != null && !payload.province().isBlank()) {
            existingOffice.setProvince(payload.province());
        }

        //controllo sugli orari
        LocalTime opening = payload.openingTime() != null ? payload.openingTime() : existingOffice.getOpeningTime();
        LocalTime closing = payload.closingTime() != null ? payload.closingTime() : existingOffice.getClosingTime();

        if (opening.isAfter(closing) || opening.equals(closing)) {
            throw new BadRequestException("L'orario di apertura deve essere precedente all'orario di chiusura");
        }

        if (payload.openingTime() != null) {
            existingOffice.setOpeningTime(payload.openingTime());
        }
        if (payload.closingTime() != null) {
            existingOffice.setClosingTime(payload.closingTime());
        }

        //controllo status
        if (payload.status() != null) {
            existingOffice.setStatus(payload.status());
        }

        //controllo latitudine e longitudine
        if (payload.latitude() != null) {
            existingOffice.setLatitude(payload.latitude());
        }

        if (payload.longitude() != null) {
            existingOffice.setLongitude(payload.longitude());
        }

        Office officeSaved = officeRepository.save(existingOffice);

        return new OfficeResponseDTO(officeSaved.getId(), officeSaved.getName(), officeSaved.getStreet(), officeSaved.getHouseNumber(), officeSaved.getZipCode(), officeSaved.getCity(), officeSaved.getProvince(), officeSaved.getOpeningTime(), officeSaved.getClosingTime(), officeSaved.getStatus(), officeSaved.getLatitude(), officeSaved.getLongitude());
    }

    //3. GET ALL OFFICES BY STATUS E IN ORDINE ALFABETICO
    public List<OfficeResponseDTO> getAllOfficesByStatus(OfficeStatus status) {
        List<Office> offices;
        if (status != null) {
            offices = officeRepository.findByStatusOrderByNameAsc(status);
        } else {
            offices = officeRepository.findAllByOrderByNameAsc();
        }

        return offices.stream()
                .map(office -> new OfficeResponseDTO(
                        office.getId(),
                        office.getName(),
                        office.getStreet(),
                        office.getHouseNumber(),
                        office.getZipCode(),
                        office.getCity(),
                        office.getProvince(),
                        office.getOpeningTime(),
                        office.getClosingTime(),
                        office.getStatus(),
                        office.getLatitude(),
                        office.getLongitude())).toList();
    }

    //4. GET ACTIVE OFFICES
    public List<OfficeResponseDTO> getActiveOffice() {
        List<Office> offices = officeRepository.findByStatus(OfficeStatus.ACTIVE);

        return offices.stream()
                .map(office -> new OfficeResponseDTO(
                        office.getId(),
                        office.getName(),
                        office.getStreet(),
                        office.getHouseNumber(),
                        office.getZipCode(),
                        office.getCity(),
                        office.getProvince(),
                        office.getOpeningTime(),
                        office.getClosingTime(),
                        office.getStatus(),
                        office.getLatitude(),
                        office.getLongitude()
                ))
                .toList();
    }

    //5. TROVA SEDE PER NOME - TUTTE LE SEDI(ADMIN, MANAGER, HR, PAYROLL)
    public OfficeResponseDTO getOfficeByName(String name) {
        Office office = officeRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("L'ufficio con nome '" + name + "' non è stato trovato"));

        return new OfficeResponseDTO(
                office.getId(),
                office.getName(),
                office.getStreet(),
                office.getHouseNumber(),
                office.getZipCode(),
                office.getCity(),
                office.getProvince(),
                office.getOpeningTime(),
                office.getClosingTime(),
                office.getStatus(),
                office.getLatitude(),
                office.getLongitude()
        );
    }

    public OfficeResponseDTO getActiveOfficeByName(String name) {
        Office office = officeRepository.findByNameIgnoreCaseAndStatus(name, OfficeStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Sede attiva '" + name + "' non trovata."));

        return new OfficeResponseDTO(
                office.getId(),
                office.getName(),
                office.getStreet(),
                office.getHouseNumber(),
                office.getZipCode(),
                office.getCity(),
                office.getProvince(),
                office.getOpeningTime(),
                office.getClosingTime(),
                office.getStatus(),
                office.getLatitude(),
                office.getLongitude()
        );
    }
    

}
