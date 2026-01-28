package model.gtfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe CalendarDate")
class CalendarDateTest {

    private CalendarDate calendarFeriali;
    private CalendarDate calendarWeekend;
    private CalendarDate eccezione;

    @BeforeEach
    void setUp() {
        // Calendario feriali (lun-ven)
        calendarFeriali = new CalendarDate(
                "WD",
                true, true, true, true, true,  // lun-ven
                false, false,                   // sabato-domenica
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        // Calendario weekend
        calendarWeekend = new CalendarDate(
                "WE",
                false, false, false, false, false,  // lun-ven
                true, true,                          // sabato-domenica
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );

        // Eccezione specifica (1 gennaio 2025 - servizio non disponibile)
        eccezione = new CalendarDate(
                "WD",
                LocalDate.of(2025, 1, 1),
                2  // servizio non disponibile
        );
    }

    @Test
    @DisplayName("Test costruttore calendario ricorrente")
    void testCostruttoreCalendario() {
        assertNotNull(calendarFeriali);
        assertEquals("WD", calendarFeriali.getServiceId());
        assertTrue(calendarFeriali.isMonday());
        assertTrue(calendarFeriali.isFriday());
        assertFalse(calendarFeriali.isSaturday());
    }

    @Test
    @DisplayName("Test costruttore eccezione")
    void testCostruttoreEccezione() {
        assertNotNull(eccezione);
        assertEquals("WD", eccezione.getServiceId());
        assertEquals(LocalDate.of(2025, 1, 1), eccezione.getDate());
        assertEquals(2, eccezione.getExceptionType());
    }

    @Test
    @DisplayName("Test isOperativoSoloFeriali")
    void testIsOperativoSoloFeriali() {
        assertTrue(calendarFeriali.isOperativoSoloFeriali());
        assertFalse(calendarWeekend.isOperativoSoloFeriali());
    }

    @Test
    @DisplayName("Test isOperativoSoloWeekend")
    void testIsOperativoSoloWeekend() {
        assertTrue(calendarWeekend.isOperativoSoloWeekend());
        assertFalse(calendarFeriali.isOperativoSoloWeekend());
    }

    @Test
    @DisplayName("Test isOperativoTuttiIGiorni")
    void testIsOperativoTuttiIGiorni() {
        CalendarDate tuttiGiorni = new CalendarDate(
                "ALL",
                true, true, true, true, true, true, true,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)
        );
        assertTrue(tuttiGiorni.isOperativoTuttiIGiorni());
    }

    @Test
    @DisplayName("Test getTipoServizio")
    void testGetTipoServizio() {
        assertEquals("Giorni feriali", calendarFeriali.getTipoServizio());
        assertEquals("Solo weekend", calendarWeekend.getTipoServizio());
    }

    @Test
    @DisplayName("Test getGiorniOperativiDescrizione")
    void testGetGiorniOperativiDescrizione() {
        String giorni = calendarFeriali.getGiorniOperativiDescrizione();
        assertTrue(giorni.contains("Lun"));
        assertTrue(giorni.contains("Ven"));
        assertFalse(giorni.contains("Sab"));
    }

    @Test
    @DisplayName("Test isOperativoInData - giorno feriale")
    void testIsOperativoInDataFeriale() {
        // Lunedì 5 gennaio 2025
        LocalDate lunedi = LocalDate.of(2025, 1, 6);
        List<CalendarDate> eccezioni = new ArrayList<>();

        assertTrue(calendarFeriali.isOperativoInData(lunedi, eccezioni));
        assertFalse(calendarWeekend.isOperativoInData(lunedi, eccezioni));
    }

    @Test
    @DisplayName("Test isOperativoInData - weekend")
    void testIsOperativoInDataWeekend() {
        // Sabato 4 gennaio 2025
        LocalDate sabato = LocalDate.of(2025, 1, 4);
        List<CalendarDate> eccezioni = new ArrayList<>();

        assertFalse(calendarFeriali.isOperativoInData(sabato, eccezioni));
        assertTrue(calendarWeekend.isOperativoInData(sabato, eccezioni));
    }

    @Test
    @DisplayName("Test isOperativoInData - con eccezione")
    void testIsOperativoInDataConEccezione() {
        // 1 gennaio 2025 normalmente sarebbe operativo (mercoledì)
        LocalDate data = LocalDate.of(2025, 1, 1);
        List<CalendarDate> eccezioni = new ArrayList<>();
        eccezioni.add(eccezione);  // eccezione: servizio NON disponibile

        assertFalse(calendarFeriali.isOperativoInData(data, eccezioni));
    }

    @Test
    @DisplayName("Test isOperativoInData - fuori range")
    void testIsOperativoInDataFuoriRange() {
        LocalDate fuoriRange = LocalDate.of(2026, 1, 1);
        List<CalendarDate> eccezioni = new ArrayList<>();

        assertFalse(calendarFeriali.isOperativoInData(fuoriRange, eccezioni));
    }

    @Test
    @DisplayName("Test getExceptionTypeDescrizione")
    void testGetExceptionTypeDescrizione() {
        assertEquals("Servizio non disponibile", eccezione.getExceptionTypeDescrizione());

        CalendarDate eccDisponibile = new CalendarDate(
                "WD", LocalDate.of(2025, 12, 25), 1
        );
        assertEquals("Servizio disponibile", eccDisponibile.getExceptionTypeDescrizione());
    }

    @Test
    @DisplayName("Test parseDataGTFS formato valido")
    void testParseDataGTFS() {
        LocalDate data = CalendarDate.parseDataGTFS("20250101");
        assertNotNull(data);
        assertEquals(2025, data.getYear());
        assertEquals(1, data.getMonthValue());
        assertEquals(1, data.getDayOfMonth());
    }

    @Test
    @DisplayName("Test parseDataGTFS formato invalido")
    void testParseDataGTFSInvalido() {
        assertNull(CalendarDate.parseDataGTFS("invalid"));
        assertNull(CalendarDate.parseDataGTFS(""));
        assertNull(CalendarDate.parseDataGTFS(null));
    }

    @Test
    @DisplayName("Test parseBooleanGTFS")
    void testParseBooleanGTFS() {
        assertTrue(CalendarDate.parseBooleanGTFS("1"));
        assertFalse(CalendarDate.parseBooleanGTFS("0"));
        assertFalse(CalendarDate.parseBooleanGTFS("invalid"));
    }

    @Test
    @DisplayName("Test toString calendario")
    void testToStringCalendario() {
        String str = calendarFeriali.toString();
        assertTrue(str.contains("WD"));
        assertTrue(str.contains("Giorni feriali"));
    }

    @Test
    @DisplayName("Test toString eccezione")
    void testToStringEccezione() {
        String str = eccezione.toString();
        assertTrue(str.contains("2025-01-01"));
        assertTrue(str.contains("non disponibile"));
    }

    @Test
    @DisplayName("Test equals con stesso serviceId")
    void testEquals() {
        CalendarDate calendarFeriali2 = new CalendarDate(
                "WD",
                false, false, false, false, false, false, false,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
        );
        assertEquals(calendarFeriali, calendarFeriali2);
    }
}
