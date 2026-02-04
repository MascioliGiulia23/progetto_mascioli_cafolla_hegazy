package view.panels.search;

import model.gtfs.Fermate;
import model.gtfs.Route;
import model.gtfs.StopTime;
import model.gtfs.Trip;
import org.jxmapviewer.viewer.GeoPosition;
import view.map.BusWaypoint;
import view.map.WaypointDrawer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class LineStopsViewBuilder {

    public void build(JPanel resultsContainer,
                      WaypointDrawer waypointDrawer,
                      Route rotta,
                      Trip direzioneScelta,
                      List<StopTime> stopTimes,
                      List<Fermate> tutteLeFermate,
                      Consumer<Fermate> onStopSelected,
                      Runnable onBackToLines,
                      Runnable onTogglePreferitiLinea) {

        // Crea mappa per ricerche O(1)
        Map<String, Fermate> fermatePerId = new HashMap<>();
        for (Fermate f : tutteLeFermate) {
            fermatePerId.put(f.getStopId(), f);
        }



        // Titolo + bottone preferiti (UI IDENTICA)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        topPanel.setOpaque(false);

        JLabel titolo = new JLabel("\"Linea " + rotta.getRouteShortName() + "\"");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titolo.setOpaque(false);

        JButton favBtn = new JButton(" Preferiti");
        favBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        favBtn.addActionListener(e -> {
            if (onTogglePreferitiLinea != null) onTogglePreferitiLinea.run();
        });

        topPanel.add(titolo);
        topPanel.add(favBtn);
        resultsContainer.add(topPanel);

        JLabel dirLabel = new JLabel("Direzione: \"" + direzioneScelta.getTripHeadsign() + "\"", SwingConstants.CENTER);
        dirLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultsContainer.add(dirLabel);

        // Tabella fermate
        List<StopTime> stopTrip = new ArrayList<>();
        for (StopTime st : stopTimes) {
            if (st.getTripId().equals(direzioneScelta.getTripId())) {
                stopTrip.add(st);
            }
        }

        stopTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));

        String[] colonne = {"Fermata"};
        List<String[]> righe = new ArrayList<>();
        for (StopTime st : stopTrip) {
            Fermate f = fermatePerId.get(st.getStopId());
            if (f != null) {
                righe.add(new String[]{f.getStopName()});
            }
        }

        if (righe.isEmpty()) {
            JLabel noResult = new JLabel("Nessuna fermata trovata per questa direzione.", SwingConstants.CENTER);
            noResult.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noResult.setForeground(Color.DARK_GRAY);
            resultsContainer.add(Box.createVerticalStrut(10));
            resultsContainer.add(noResult);
        } else {
            JTable tabella = new JTable(righe.toArray(new String[0][]), colonne);
            tabella.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tabella.setRowHeight(22);
            tabella.getColumnModel().getColumn(0).setPreferredWidth(300);
            tabella.setEnabled(true);

            tabella.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int riga = tabella.getSelectedRow();
                    if (riga >= 0 && onStopSelected != null) {
                        String nomeFermata = (String) tabella.getValueAt(riga, 0);

                        Fermate fermataSelezionata = fermatePerId.get(
                                tutteLeFermate.stream()
                                        .filter(f -> f.getStopName().equalsIgnoreCase(nomeFermata))
                                        .map(Fermate::getStopId)
                                        .findFirst()
                                        .orElse(null)
                        );

                        if (fermataSelezionata != null) {
                            System.out.println("Fermata cliccata: " + fermataSelezionata.getStopName());
                            onStopSelected.accept(fermataSelezionata);
                        }
                    }
                }
            });

            JScrollPane scroll = new JScrollPane(tabella);
            scroll.setPreferredSize(new Dimension(320, 300));
            resultsContainer.add(scroll);
        }

        JButton backBtn = new JButton("← Torna alle linee");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.addActionListener(e -> {
            if (onBackToLines != null) onBackToLines.run();
            if (waypointDrawer != null) waypointDrawer.clearWaypoints();
        });

        resultsContainer.add(Box.createVerticalStrut(10));
        resultsContainer.add(backBtn);
    }
}
