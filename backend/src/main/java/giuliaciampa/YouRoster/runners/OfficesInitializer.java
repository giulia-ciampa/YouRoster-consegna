package giuliaciampa.YouRoster.runners;

import giuliaciampa.YouRoster.services.OfficeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalTime;

@Component
public class OfficesInitializer implements CommandLineRunner {

    private final OfficeService officeService;

    public OfficesInitializer(OfficeService officeService) {
        this.officeService = officeService;
    }


    @Override
    public void run(String... args) throws Exception {
        officeService.createDefaultOffices("NAVONA", "Piazza Navona", "1", "00186", "Roma", "RM", LocalTime.of(9, 0), LocalTime.of(19, 0), BigDecimal.valueOf(41.899500), BigDecimal.valueOf(12.473012));
        officeService.createDefaultOffices("COLOSSEO", "Piazza Colosseo", "1", "00186", "Roma", "RM", LocalTime.of(8, 0), LocalTime.of(18, 0), BigDecimal.valueOf(41.890305), BigDecimal.valueOf(12.494839));
        officeService.createDefaultOffices("VATICANO 1", "Via Vaticano 1", "1", "00186", "Roma", "RM", LocalTime.of(8, 0), LocalTime.of(19, 0), BigDecimal.valueOf(41.906755), BigDecimal.valueOf(12.452053));
        officeService.createDefaultOffices("VATICANO 2", "Via Vaticano 2", "1", "00186", "Roma", "RM", LocalTime.of(8, 0), LocalTime.of(19, 0), BigDecimal.valueOf(41.906795), BigDecimal.valueOf(12.452975));
        officeService.createDefaultOffices("VATICANO 3", "Via Vaticano 3", "1", "00186", "Roma", "RM", LocalTime.of(8, 0), LocalTime.of(19, 0), BigDecimal.valueOf(41.906572), BigDecimal.valueOf(12.451892));


        System.out.println("sedi salvate correttamente all'avvio");
    }
}
