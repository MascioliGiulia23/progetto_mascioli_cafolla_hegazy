package view.panels.search;

import model.gtfs.Fermate;
import model.gtfs.Route;
import model.gtfs.StopTime;
import model.gtfs.Trip;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Engine che contiene la logica di calcolo orari per SearchResultsPanel.
 * Non usa Swing: restituisce direttamente le righe per la tabella.
 */
public class StopScheduleEngine {

    private service.RealTimeDelayService delayService;

    public StopScheduleEngine(service.RealTimeDelayService delayService) {
        this.delayService = delayService;
    }

    public void setDelayService(service.RealTimeDelayService delayService) {
        this.delayService = delayService;
    }

    // PARO PARO la tua classe
    public static class OrarioRow {
        public String nomeLinea;
        public String direzione;
        public String orarioFormattato;
        public String tripId;  //CHIAVE per matching real-time

        public OrarioRow(String nomeLinea, String direzione, String orarioFormattato, String tripId) {
            this.nomeLinea = nomeLinea;
            this.direzione = direzione;
            this.orarioFormattato = orarioFormattato;
            this.tripId = tripId;
        }

        public String[] toArray() {
            return new String[]{nomeLinea, direzione, orarioFormattato};
        }
    }

    /**
     * - costruisce righe
     * - costruisce righeConRT
     * - sceglie righeDaMostrare (RT se c'è, altrimenti statico)
     * - restituisce righeTabella
     */
    public List<String[]> calcolaRigheTabella(
            Fermate fermata,
            List<StopTime> stopTimes,
            List<Trip> trips,
            List<Route> rotte,
            List<Fermate> tutteLeFermate
    ) {

        // Mappe hash per lookups O(1)
        Map<String, Trip> tripMap = new HashMap<>(trips.size());
        Map<String, Route> routeMap = new HashMap<>(rotte.size());
        Map<String, Fermate> fermatePerId = new HashMap<>(tutteLeFermate.size());

        for (Trip trip : trips) tripMap.put(trip.getTripId(), trip);
        for (Route route : rotte) routeMap.put(route.getRouteId(), route);
        for (Fermate f : tutteLeFermate) fermatePerId.put(f.getStopId(), f);

        // Indice stopTimes per fermata
        Map<String, List<StopTime>> stopTimePerFermata = new HashMap<>();
        Map<String, StopTime> ultimoStopPerTrip = new HashMap<>();

        LocalTime oraCorrente = LocalTime.now().minusMinutes(5);
        LocalTime oraMax = oraCorrente.plusMinutes(65);

        for (StopTime st : stopTimes) {
            stopTimePerFermata.computeIfAbsent(st.getStopId(), k -> new ArrayList<>()).add(st);

            String tripId = st.getTripId();
            StopTime existing = ultimoStopPerTrip.get(tripId);
            if (existing == null || st.getStopSequence() > existing.getStopSequence()) {
                ultimoStopPerTrip.put(tripId, st);
            }
        }

        // PRE-CALCOLO CAPOLINEA
        Map<String, String> capolineaPerTrip = new HashMap<>();
        for (Map.Entry<String, StopTime> entry : ultimoStopPerTrip.entrySet()) {
            Fermate capolinea = fermatePerId.get(entry.getValue().getStopId());
            if (capolinea != null) {
                capolineaPerTrip.put(entry.getKey(), capolinea.getStopName());
            }
        }

        // RACCOLTA DATI - Unico loop ottimizzato
        List<OrarioRow> righe = new ArrayList<>();
        Set<String> orariGiaAggiunti = new HashSet<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        List<StopTime> fermataStops = stopTimePerFermata.get(fermata.getStopId());
        if (fermataStops != null) {
            for (StopTime st : fermataStops) {
                if (st.getArrivalTime() == null) continue;

                LocalTime arrivo = st.getArrivalTime();
                if (arrivo.isBefore(oraCorrente) || arrivo.isAfter(oraMax)) continue;

                Trip trip = tripMap.get(st.getTripId());
                if (trip == null) continue;

                Route route = routeMap.get(trip.getRouteId());
                if (route == null) continue;

                String nomeLinea = route.getRouteShortName();
                String orarioFormattato = arrivo.format(fmt);
                String tripId = st.getTripId();
                String chiave = nomeLinea + "|" + orarioFormattato + "|" + tripId;

                if (orariGiaAggiunti.add(chiave)) {
                    String capolineaNome = capolineaPerTrip.getOrDefault(tripId, "?");
                    String direzione = " → " + capolineaNome;
                    righe.add(new OrarioRow(nomeLinea, direzione, orarioFormattato, tripId));
                }
            }
        }

        //CARICA RITARDI REAL-TIME
        List<OrarioRow> righeConRT = new ArrayList<>();

        if (delayService != null && service.ConnectivityService.isOnline()) {
            try {
                System.out.println("═══════════════════════════════════════════════");
                System.out.println("[SearchResultsPanel] Richiesta ritardi per fermata: " + fermata.getStopId());

                Map<String, Integer> delaysByLineaOrario = delayService.getDelaysByTripId(fermata.getStopId());
                System.out.println("[SearchResultsPanel] Ritardi ricevuti per " + delaysByLineaOrario.size() + " combinazioni");

                //FILTRA: Tieni SOLO le righe con dati RT
                for (OrarioRow riga : righe) {
                    String chiaveRT = riga.nomeLinea + "#" + riga.orarioFormattato;
                    Integer delaySeconds = delaysByLineaOrario.get(chiaveRT);

                    if (delaySeconds != null) {
                        int minutes = delaySeconds / 60;

                        if (Math.abs(minutes) > 1) {
                            if (minutes > 0) {
                                riga.orarioFormattato = riga.orarioFormattato + "  (+" + minutes + " min)";
                            } else {
                                riga.orarioFormattato = riga.orarioFormattato + "  (" + minutes + " min)";
                            }
                        } else {
                            riga.orarioFormattato = riga.orarioFormattato + "  (On Time)";
                        }

                        righeConRT.add(riga);
                        System.out.println("[SearchResultsPanel] ✓ Linea " + riga.nomeLinea + " orario " + riga.orarioFormattato);
                    }
                }

                System.out.println("═══════════════════════════════════════════════");

            } catch (Exception e) {
                System.err.println("[SearchResultsPanel] ✗ Errore caricamento ritardi: " + e.getMessage());
                e.printStackTrace();
            }
        }

        //  DECISIONE: Mostra RT se disponibili, altrimenti fallback a statico
        List<OrarioRow> righeDaMostrare;

        if (!righeConRT.isEmpty()) {
            //  RIMUOVI DUPLICATI: Una sola riga per linea+orario
            Map<String, OrarioRow> mappaUnica = new LinkedHashMap<>();
            for (OrarioRow riga : righeConRT) {
                String chiaveUnica = riga.nomeLinea + "#" + riga.orarioFormattato;
                mappaUnica.putIfAbsent(chiaveUnica, riga); // Tiene solo la prima
            }
            righeDaMostrare = new ArrayList<>(mappaUnica.values());
            System.out.println("[SearchResultsPanel] ✓ Mostrando " + righeDaMostrare.size() + " linee REAL-TIME (rimosse " + (righeConRT.size() - righeDaMostrare.size()) + " duplicati)");
        } else {
            // Anche per lo statico: rimuovi duplicati linea+orario
            Map<String, OrarioRow> mappaUnica = new LinkedHashMap<>();
            for (OrarioRow riga : righe) {
                String chiaveUnica = riga.nomeLinea + "#" + riga.orarioFormattato.replaceAll("\\s*\\([^)]*\\)", "");
                mappaUnica.putIfAbsent(chiaveUnica, riga);
            }
            righeDaMostrare = new ArrayList<>(mappaUnica.values());
            System.out.println("[SearchResultsPanel] ⚠ Nessun dato RT, mostrando " + righeDaMostrare.size() + " orari statici (rimosse " + (righe.size() - righeDaMostrare.size()) + " duplicati)");
        }

        //  Converti in String[] per la tabella
        List<String[]> righeTabella = righeDaMostrare.stream()
                .map(OrarioRow::toArray)
                .collect(java.util.stream.Collectors.toList());

        righeTabella.sort(Comparator.comparing(arr -> arr[2]));

        return righeTabella;
    }
}
