package service;

import model.gtfs.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceQualityMonitor {

    // Storico ritardi per linea (ultimi 7 giorni)
    private Map<String, List<DelayRecord>> ritardiPerLinea;

    // Affidabilità linee (% arrivi on-time)
    private Map<String, Double> affidabilitaLinee;

    // Statistiche globali
    private int totaleArriviMonitorati = 0;
    private int arriviInOrario = 0;
    private int arriviInRitardo = 0;
    private int arriviInAnticipo = 0;

    private RealTimeDelayService delayService;

    public ServiceQualityMonitor(RealTimeDelayService delayService) {
        this.delayService = delayService;
        this.ritardiPerLinea = new ConcurrentHashMap<>();
        this.affidabilitaLinee = new ConcurrentHashMap<>();
    }

    // Classe interna per registrare singolo ritardo
    public static class DelayRecord {
        String lineaId;
        String lineaNome;
        LocalDateTime timestamp;
        int ritardoSecondi;
        String stopId;
        String stopName;

        public DelayRecord(String lineaId, String lineaNome, int ritardoSecondi,
                           String stopId, String stopName) {
            this.lineaId = lineaId;
            this.lineaNome = lineaNome;
            this.timestamp = LocalDateTime.now();
            this.ritardoSecondi = ritardoSecondi;
            this.stopId = stopId;
            this.stopName = stopName;
        }

        public String getLineaId() { return lineaId; }
        public String getLineaNome() { return lineaNome; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public int getRitardoSecondi() { return ritardoSecondi; }
        public int getRitardoMinuti() { return ritardoSecondi / 60; }
        public String getStopId() { return stopId; }
        public String getStopName() { return stopName; }
    }

    // Registra nuovo dato di qualità
    public void registraRitardo(String lineaId, String lineaNome, int ritardoSecondi,
                                String stopId, String stopName) {
        DelayRecord record = new DelayRecord(lineaId, lineaNome, ritardoSecondi, stopId, stopName);

        ritardiPerLinea.computeIfAbsent(lineaId, k -> new ArrayList<>()).add(record);

        // Aggiorna statistiche globali
        totaleArriviMonitorati++;
        if (Math.abs(ritardoSecondi) <= 60) arriviInOrario++;
        else if (ritardoSecondi > 60) arriviInRitardo++;
        else arriviInAnticipo++;

        // Pulisci dati vecchi (> 7 giorni)
        pulisciDatiVecchi();
    }

    // Calcola affidabilità linea (% on-time)
    public double calcolaAffidabilita(String lineaId) {
        List<DelayRecord> records = ritardiPerLinea.get(lineaId);
        if (records == null || records.isEmpty()) return 0.0;

        long onTime = records.stream()
                .filter(r -> Math.abs(r.ritardoSecondi) <= 60)
                .count();

        double affidabilita = (onTime * 100.0) / records.size();
        affidabilitaLinee.put(lineaId, affidabilita);
        return affidabilita;
    }

    // Previsione intelligente: ritardo medio ultimi 3 giorni
    public int prevediRitardo(String lineaId, String stopId, LocalTime orarioPrevisto) {
        List<DelayRecord> records = ritardiPerLinea.get(lineaId);
        if (records == null || records.isEmpty()) return 0;

        LocalDateTime treGiorniFa = LocalDateTime.now().minusDays(3);

        // Filtra ritardi recenti per questa fermata nella stessa fascia oraria (+/- 30 min)
        List<Integer> ritardiRecenti = new ArrayList<>();

        for (DelayRecord r : records) {
            if (r.timestamp.isAfter(treGiorniFa) && r.stopId.equals(stopId)) {
                LocalTime orarioRecord = r.timestamp.toLocalTime();
                if (Math.abs(orarioRecord.toSecondOfDay() - orarioPrevisto.toSecondOfDay()) <= 1800) {
                    ritardiRecenti.add(r.ritardoSecondi);
                }
            }
        }

        if (ritardiRecenti.isEmpty()) return 0;

        // Media ritardi recenti
        return (int) ritardiRecenti.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    // Top 5 linee più affidabili
    public List<Map.Entry<String, Double>> getTop5LineePiuAffidabili() {
        return affidabilitaLinee.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .toList();
    }

    // Top 5 linee meno affidabili
    public List<Map.Entry<String, Double>> getTop5LineeMenoAffidabili() {
        return affidabilitaLinee.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(5)
                .toList();
    }

    // Ritardo medio globale
    public double getRitardoMedioGlobale() {
        if (ritardiPerLinea.isEmpty()) return 0.0;

        double sommaRitardi = ritardiPerLinea.values().stream()
                .flatMap(List::stream)
                .mapToInt(r -> r.ritardoSecondi)
                .average()
                .orElse(0.0);

        return sommaRitardi / 60.0; // in minuti
    }

    // Percentuale affidabilità globale
    public double getAffidabilitaGlobale() {
        if (totaleArriviMonitorati == 0) return 0.0;
        return (arriviInOrario * 100.0) / totaleArriviMonitorati;
    }

    // Pulisci dati vecchi (> 7 giorni)
    private void pulisciDatiVecchi() {
        LocalDateTime setteGiorniFa = LocalDateTime.now().minusDays(7);

        ritardiPerLinea.forEach((lineaId, records) ->
                records.removeIf(r -> r.timestamp.isBefore(setteGiorniFa))
        );
    }

    // Getters statistiche
    public int getTotaleArriviMonitorati() { return totaleArriviMonitorati; }
    public int getArriviInOrario() { return arriviInOrario; }
    public int getArriviInRitardo() { return arriviInRitardo; }
    public int getArriviInAnticipo() { return arriviInAnticipo; }

    public Map<String, List<DelayRecord>> getRitardiPerLinea() {
        return new HashMap<>(ritardiPerLinea);
    }
}
