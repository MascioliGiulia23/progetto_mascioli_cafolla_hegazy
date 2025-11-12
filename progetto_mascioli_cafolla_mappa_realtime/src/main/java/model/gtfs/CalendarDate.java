package model.gtfs;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.DayOfWeek;
import java.util.List;

/**
 * Classe che rappresenta il calendario operativo (GTFS Calendar e Calendar_dates)
 * Gestisce quali giorni una linea/corsa è operativa
 */
public class CalendarDate {

    // Attributi del calendario
    private String serviceId;           // ID univoco del servizio
    private LocalDate date;             // Data specifica
    private int exceptionType;          // 1=servizio disponibile, 2=servizio non disponibile
    private boolean monday;             // Operativo lunedì
    private boolean tuesday;            // Operativo martedì
    private boolean wednesday;          // Operativo mercoledì
    private boolean thursday;           // Operativo giovedì
    private boolean friday;             // Operativo venerdì
    private boolean saturday;           // Operativo sabato
    private boolean sunday;             // Operativo domenica
    private LocalDate startDate;        // Data inizio (da calendar.txt)
    private LocalDate endDate;          // Data fine (da calendar.txt)

    //Costruttore per calendar.txt (calendario ricorrente)

    public CalendarDate(String serviceId, boolean monday, boolean tuesday, boolean wednesday,
                        boolean thursday, boolean friday, boolean saturday, boolean sunday,
                        LocalDate startDate, LocalDate endDate) {
        this.serviceId = null;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.startDate = startDate;
        this.endDate = endDate;
        this.date = null;
        this.exceptionType = 1;
    }

    // Costruttore per calendar.txt (eccezioni specifiche)

    public CalendarDate(String serviceId, LocalDate date, int exceptionType) {
        this.serviceId = serviceId;
        this.date = date;
        this.exceptionType = exceptionType;
        this.startDate = null;
        this.endDate = null;
        this.monday = false;
        this.tuesday = false;
        this.wednesday = false;
        this.thursday = false;
        this.friday = false;
        this.saturday = false;
        this.sunday = false;
    }

    // GETTERS

    public String getServiceId() {
        return serviceId;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getExceptionType() {
        return exceptionType;
    }

    public boolean isMonday() {
        return monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

    public boolean isSunday() {
        return sunday;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    // ==================== SETTERS ====================

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setExceptionType(int exceptionType) {
        this.exceptionType = exceptionType;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    // ==================== METODI UTILI ====================

    /**
     * Verifica se il servizio è operativo in una data specifica
     * Tiene conto sia del calendario ricorrente che delle eccezioni
     */
    public boolean isOperativoInData(LocalDate dataVerifica, List<CalendarDate> eccezioni) {
        // Verifica le eccezioni prima
        for (CalendarDate exc : eccezioni) {
            if (exc.getServiceId().equals(this.serviceId) && exc.getDate().equals(dataVerifica)) {
                return exc.getExceptionType() == 1;
            }
        }

        // Se nessuna eccezione, verifica il calendario ricorrente
        if (dataVerifica.isBefore(startDate) || dataVerifica.isAfter(endDate)) {
            return false;
        }

        DayOfWeek dayOfWeek = dataVerifica.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> monday;
            case TUESDAY -> tuesday;
            case WEDNESDAY -> wednesday;
            case THURSDAY -> thursday;
            case FRIDAY -> friday;
            case SATURDAY -> saturday;
            case SUNDAY -> sunday;
        };
    }

//    /**
//     * Ritorna i giorni operativi come array di booleani
//     */
//    public boolean[] getGiorniOperativi() {
//        return new boolean[]{monday, tuesday, wednesday, thursday, friday, saturday, sunday};
//    }

    /**
     * Ritorna il nome dei giorni operativi come stringa
     */
    public String getGiorniOperativiDescrizione() {
        StringBuilder sb = new StringBuilder();
        if (monday) sb.append("Lun ");
        if (tuesday) sb.append("Mar ");
        if (wednesday) sb.append("Mer ");
        if (thursday) sb.append("Gio ");
        if (friday) sb.append("Ven ");
        if (saturday) sb.append("Sab ");
        if (sunday) sb.append("Dom ");
        return sb.toString().trim();
    }

//    /**
//     * Verifica se il servizio è operativo durante i giorni feriali
//     */
//    public boolean isOperativoFeriali() {
//        return monday && tuesday && wednesday && thursday && friday;
//    }
//
//    /**
//     * Verifica se il servizio è operativo nel fine settimana
//     */
//    public boolean isOperativoWeekend() {
//        return saturday || sunday;
//    }

    /**
     * Verifica se il servizio è operativo tutti i giorni
     */
    public boolean isOperativoTuttiIGiorni() {
        return monday && tuesday && wednesday && thursday && friday && saturday && sunday;
    }

    /**
     * Verifica se il servizio è operativo solo nei giorni feriali
     */
    public boolean isOperativoSoloFeriali() {
        return monday && tuesday && wednesday && thursday && friday && !saturday && !sunday;
    }

    /**
     * Verifica se il servizio è operativo solo nel fine settimana
     */
    public boolean isOperativoSoloWeekend() {
        return !monday && !tuesday && !wednesday && !thursday && !friday && (saturday || sunday);
    }

    /**
     * Ritorna una descrizione del tipo di servizio
     */
    public String getTipoServizio() {
        if (isOperativoTuttiIGiorni()) {
            return "Tutti i giorni";
        } else if (isOperativoSoloFeriali()) {
            return "Giorni feriali";
        } else if (isOperativoSoloWeekend()) {
            return "Solo weekend";
        } else {
            return getGiorniOperativiDescrizione();
        }
    }

    /**
     * Ritorna il tipo di eccezione come stringa
     */
    public String getExceptionTypeDescrizione() {
        return exceptionType == 1 ? "Servizio disponibile" : "Servizio non disponibile";
    }

    /**
     * Converte una stringa di data in formato YYYYMMDD a LocalDate
     */
    public static LocalDate parseDataGTFS(String dataStr) {
        if (dataStr == null || dataStr.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(dataStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            System.err.println("Errore nel parsing della data: " + dataStr);
            return null;
        }
    }

    // Converte una stringa booleana GTFS (0/1) a boolean

    public static boolean parseBooleanGTFS(String value) {
        return "1".equals(value.trim());
    }

    // Ritorna una rappresentazione testuale

    @Override
    public String toString() {
        if (date != null) {
            return "CalendarDate{" +
                    "serviceId='" + serviceId + '\'' +
                    ", date=" + date +
                    ", exceptionType=" + getExceptionTypeDescrizione() +
                    '}';
        } else {
            return "CalendarDate{" +
                    "serviceId='" + serviceId + '\'' +
                    ", giorni=" + getTipoServizio() +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    '}';
        }
    }

    // Verifica se due calendari sono uguali

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CalendarDate that = (CalendarDate) obj;
        return serviceId.equals(that.serviceId);
    }
}
