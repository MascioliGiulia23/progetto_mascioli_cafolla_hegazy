package view.panels.search;

import model.gtfs.Fermate;
import view.panels.ServiceQualityPanel;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

/**
 * Support class per aggiornare il ServiceQualityPanel in modo riusabile,
 * togliendo logica e loop da SearchResultsPanel.
 */
public class ServiceQualitySupport {

    /**
     * Costruisce la lista BusArrivo e aggiorna il qualityPanel sulla EDT.
     *
     * @param qualityPanel pannello qualità (può essere null)
     * @param fermata fermata corrente
     * @param colonne colonne della tabella che hai mostrato (serve per capire se è contesto LINEA o RICERCA)
     * @param data dati della tabella che hai mostrato
     * @param rottaCorrente può essere null (se contesto RICERCA)
     * @param direzioneCorrente può essere null (se contesto RICERCA)
     */
    public void updateQualityPanel(ServiceQualityPanel qualityPanel,
                                   Fermate fermata,
                                   String[] colonne,
                                   String[][] data,
                                   model.gtfs.Route rottaCorrente,
                                   model.gtfs.Trip direzioneCorrente) {

        if (qualityPanel == null || fermata == null || colonne == null || data == null) return;
        if (data.length == 0) return;

        List<ServiceQualityPanel.BusArrivo> busArriviList = new ArrayList<>();

        boolean contestoRicerca = (colonne.length == 3);
        // contesto LINEA => colonne.length == 1 (solo orario)

        for (int i = 0; i < data.length; i++) {
            String nomeLinea;
            String direzione;
            String orarioStr;

            if (contestoRicerca) {
                nomeLinea = data[i][0];
                direzione = data[i][1];
                orarioStr = data[i][2];
            } else {
                nomeLinea = (rottaCorrente != null) ? rottaCorrente.getRouteShortName() : "N/A";
                direzione = (direzioneCorrente != null) ? direzioneCorrente.getTripHeadsign() : "";
                orarioStr = data[i][0];
            }

            int ritardoMin = estraiRitardo(orarioStr);
            ServiceQualityPanel.AffollamentoBus affollamento = stimaAffollamento(ritardoMin, orarioStr);

            busArriviList.add(new ServiceQualityPanel.BusArrivo(
                    nomeLinea,
                    direzione,
                    orarioStr,
                    ritardoMin,
                    affollamento
            ));
        }

        SwingUtilities.invokeLater(() -> {
            qualityPanel.aggiornaPerFermata(fermata, busArriviList, new ArrayList<>());
            if (!qualityPanel.isVisible()) {
                qualityPanel.setVisible(true);
            }
        });
    }

    // ==========================
    // Helpers identici ai tuoi
    // ==========================

    private int estraiRitardo(String orarioFormattato) {
        try {
            if (orarioFormattato == null) return 0;

            if (orarioFormattato.contains("(+")) {
                int start = orarioFormattato.indexOf("(+") + 2;
                int end = orarioFormattato.indexOf(" min");
                if (end > start) {
                    String minStr = orarioFormattato.substring(start, end).trim();
                    return Integer.parseInt(minStr);
                }
            } else if (orarioFormattato.contains("(-")) {
                int start = orarioFormattato.indexOf("(-") + 2;
                int end = orarioFormattato.indexOf(" min");
                if (end > start) {
                    String minStr = orarioFormattato.substring(start, end).trim();
                    return -Integer.parseInt(minStr);
                }
            } else if (orarioFormattato.contains("On Time")) {
                return 0;
            }

            return 0;

        } catch (Exception e) {
            System.err.println("Errore estrazione ritardo da: " + orarioFormattato);
            e.printStackTrace();
            return 0;
        }
    }

    private ServiceQualityPanel.AffollamentoBus stimaAffollamento(int ritardoMinuti, String orarioStr) {
        try {
            if (orarioStr == null || orarioStr.isBlank()) {
                return ServiceQualityPanel.AffollamentoBus.SCONOSCIUTO;
            }

            String oraPart = orarioStr.split("\\s+")[0];
            int ora = Integer.parseInt(oraPart.split(":")[0]);

            boolean oraDiPunta = (ora >= 7 && ora <= 9) || (ora >= 17 && ora <= 19);

            if (oraDiPunta) {
                if (ritardoMinuti >= 15) return ServiceQualityPanel.AffollamentoBus.MOLTO_ALTO;
                if (ritardoMinuti >= 8)  return ServiceQualityPanel.AffollamentoBus.ALTO;
                if (ritardoMinuti >= 3)  return ServiceQualityPanel.AffollamentoBus.MEDIO;
                return ServiceQualityPanel.AffollamentoBus.BASSO;
            } else {
                if (ritardoMinuti >= 15) return ServiceQualityPanel.AffollamentoBus.ALTO;
                if (ritardoMinuti >= 8)  return ServiceQualityPanel.AffollamentoBus.MEDIO;
                if (ritardoMinuti >= 3)  return ServiceQualityPanel.AffollamentoBus.BASSO;
                return ServiceQualityPanel.AffollamentoBus.BASSO;
            }

        } catch (Exception e) {
            System.err.println("ERRORE stimaAffollamento per orario: " + orarioStr);
            e.printStackTrace();
            return ServiceQualityPanel.AffollamentoBus.SCONOSCIUTO;
        }
    }
}
