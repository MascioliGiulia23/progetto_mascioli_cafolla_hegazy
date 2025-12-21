<div align="center">
  
# 🚌 Transit App Roma

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![GTFS](https://img.shields.io/badge/GTFS-Realtime-brightgreen?style=for-the-badge)

Applicazione desktop per visualizzare in tempo reale i mezzi pubblici di Roma

</div>

## ✨ Funzionalità

- 🗺️ Mappa interattiva di Roma con JXMapViewer
- 🚌 Visualizzazione in tempo reale dei mezzi ATAC
- 📊 Dashboard di monitoraggio delle linee
- 💾 Integrazione completa dati GTFS
- 🔍 Ricerca fermate e percorsi

## 🚀 Come iniziare

### Prerequisiti
- Java 11 o superiore
- Maven 3.6+

### Installazione

# Clona il repository
git clone https://github.com/yourusername/damose.git](https://github.com/MascioliGiulia23/progetto_mascioli_cafolla_.git
cd progetto_mascioli_cafolla_

# Compila il progetto
mvn clean install

# Avvia l'applicazione
mvn exec:java

## 📁 Struttura del progetto

- **src/main/java/**
  - **controller/**
    - `MapController.java` - Gestione logica della mappa
  - **model/**
    - **gtfs/** - Entità dati GTFS
      - `CalendarDate.java` - Date di servizio
      - `Fermate.java` - Fermate del trasporto
      - `Route.java` - Linee di trasporto
      - `ShapeRoute.java` - Forma geografica percorsi
      - `StopTime.java` - Orari delle fermate
      - `Trip.java` - Viaggi programmati
    - **user/** - Gestione utenti e preferenze
      - `Favorite.java` - Fermate preferite
      - `User.java` - Modello utente
      - `UserManager.java` - Gestione utenti
    - **utils/** - Utilità
      - `Database.java` - Connessione database
      - `GeoUtils.java` - Calcoli geografici
  - **service/** - Business logic e integrazione API
    - `ConnectivityService.java` - Verifica connessione
    - `GtfsService.java` - Caricamento dati GTFS statici
    - `GtfsRealtimeService.java` - API GTFS Realtime
    - `GtfsRealtimeVehicleService.java` - Posizioni veicoli in tempo reale
    - `MapService.java` - Servizi per la mappa
    - `RealTimeDelayService.java` - Calcolo ritardi
    - `RealTimeFetcher.java` - Recupero dati realtime
    - `RealTimeParser.java` - Parsing dati realtime
  - **view/** - Interfaccia grafica Swing
    - `Jframe.java` - Finestra principale
    - **frames/**
      - `MapInitializer.java` - Inizializzazione mappa
      - `Mappa.java` - Frame mappa principale
    - **map/**
      - `BusWaypoint.java` - Marker bus sulla mappa
      - `RouteDrawer.java` - Disegno percorsi
      - `WaypointDrawer.java` - Rendering waypoints
    - **panels/**
      - `FavoritesPanel.java` - Pannello preferiti
      - `SearchBar.java` - Barra di ricerca
      - `SearchResultsPanel.java` - Risultati ricerca
      - `ServiceQualityPanel.java` - Qualità del servizio
      - `SettingsPanel.java` - Impostazioni
      - `TopRightPanel.java` - Pannello superiore destro
      - `UserProfilePanel.java` - Profilo utente
- **src/main/resources/**
  - **static_gtfs/** - Dati GTFS statici di ATAC Roma
    - `agency.txt` - Informazioni agenzia
    - `calendar_dates.txt` - Calendario servizio
    - `routes.txt` - Linee di trasporto
    - `shapes.txt` - Forme geografiche
    - `stops.txt` - Fermate
    - `stop_times.txt` - Orari
    - `trips.txt` - Viaggi
- **src/test/java/** - Unit tests
  - **model/gtfs/** - Test entità GTFS
  - **model/user/** - Test gestione utenti
  - **model/utils/** - Test utilità
- `pom.xml` - Configurazione Maven e dipendenze
- `README.md` - Documentazione progetto


## 🛠️ Tecnologie

- Java Swing per l'interfaccia grafica
- JXMapViewer per la mappa
- GTFS Realtime API
- Maven per la gestione dipendenze

## 📄 Licenza

Progetto sviluppato per scopi universitari


