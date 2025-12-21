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
- git clone https://github.com/yourusername/damose.git](https://github.com/MascioliGiulia23/progetto_mascioli_cafolla_.git
- cd progetto_mascioli_cafolla_

# Compila il progetto
mvn clean install

# Avvia l'applicazione
mvn exec:java

## 📁 Struttura del progetto

- **src/main/java/**
  - **controller/** - Gestione logica della mappa
    - `MapController.java`
  - **model/** - Modelli dati dell'applicazione
    - **gtfs/** - Entità dati GTFS (fermate, percorsi, orari)
      - `CalendarDate.java`
      - `Fermate.java`
      - `Route.java`
      - `ShapeRoute.java`
      - `StopTime.java`
      - `Trip.java`
    - **user/** - Gestione utenti e preferenze
      - `Favorite.java`
      - `User.java`
      - `UserManager.java`
    - **utils/** - Utilità database e calcoli geografici
      - `Database.java`
      - `GeoUtils.java`
  - **service/** - Business logic e integrazione API GTFS Realtime
    - `ConnectivityService.java`
    - `GtfsService.java`
    - `GtfsRealtimeService.java`
    - `GtfsRealtimeVehicleService.java`
    - `MapService.java`
    - `RealTimeDelayService.java`
    - `RealTimeFetcher.java`
    - `RealTimeParser.java`
  - **view/** - Interfaccia grafica Swing
    - `Jframe.java`
    - **frames/** - Finestre principali
      - `MapInitializer.java`
      - `Mappa.java`
    - **map/** - Componenti per la visualizzazione della mappa
      - `BusWaypoint.java`
      - `RouteDrawer.java`
      - `WaypointDrawer.java`
    - **panels/** - Pannelli UI (ricerca, preferiti, profilo)
      - `FavoritesPanel.java`
      - `SearchBar.java`
      - `SearchResultsPanel.java`
      - `ServiceQualityPanel.java`
      - `SettingsPanel.java`
      - `TopRightPanel.java`
      - `UserProfilePanel.java`
- **src/main/resources/** - Risorse statiche
  - **static_gtfs/** - Dati GTFS statici di ATAC Roma
    - `agency.txt`
    - `calendar_dates.txt`
    - `routes.txt`
    - `shapes.txt`
    - `stops.txt`
    - `stop_times.txt`
    - `trips.txt`
- **src/test/java/** - Unit tests
  - **model/gtfs/**
  - **model/user/**
  - **model/utils/**
- `pom.xml` - Configurazione Maven e dipendenze


## 🛠️ Tecnologie

- Java Swing per l'interfaccia grafica
- JXMapViewer per la mappa
- GTFS Realtime API
- Maven per la gestione dipendenze

## 📄 Licenza

Progetto sviluppato per scopi universitari


