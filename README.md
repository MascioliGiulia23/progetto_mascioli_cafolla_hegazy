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

```text
progetto_mascioli_cafolla_mappa_realtime/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   ├── controller/
        │   │   └── MapController.java
        │   │
        │   ├── model/
        │   │   ├── gtfs/
        │   │   │   ├── CalendarDate.java
        │   │   │   ├── Fermate.java
        │   │   │   ├── Route.java
        │   │   │   ├── ShapeRoute.java
        │   │   │   ├── StopTime.java
        │   │   │   └── Trip.java
        │   │   │
        │   │   ├── user/
        │   │   │   ├── Favorite.java
        │   │   │   ├── User.java
        │   │   │   └── UserManager.java
        │   │   │
        │   │   └── utils/
        │   │       ├── Database.java
        │   │       └── GeoUtils.java
        │   │
        │   ├── service/
        │   │   ├── ConnectivityService.java
        │   │   ├── GtfsService.java
        │   │   ├── GtfsRealtimeService.java
        │   │   ├── GtfsRealtimeVehicleService.java
        │   │   ├── MapService.java
        │   │   ├── RealTimeDelayService.java
        │   │   ├── RealTimeFetcher.java
        │   │   └── RealTimeParser.java
        │   │
        │   └── view/
        │       ├── Jframe.java
        │       ├── frames/
        │       │   ├── MapInitializer.java
        │       │   └── Mappa.java
        │       │
        │       ├── map/
        │       │   ├── BusWaypoint.java
        │       │   ├── RouteDrawer.java
        │       │   └── WaypointDrawer.java
        │       │
        │       └── panels/
        │           ├── FavoritesPanel.java
        │           ├── SearchBar.java
        │           ├── SearchResultsPanel.java
        │           ├── ServiceQualityPanel.java
        │           ├── SettingsPanel.java
        │           ├── TopRightPanel.java
        │           └── UserProfilePanel.java
        │
        └── resources/
            └── static_gtfs/
                ├── agency.txt
                ├── calendar_dates.txt
                ├── routes.txt
                ├── shapes.txt
                ├── stops.txt
                ├── stop_times.txt
                └── trips.txt```



## 🛠️ Tecnologie

- Java Swing per l'interfaccia grafica
- JXMapViewer per la mappa
- GTFS Realtime API
- Maven per la gestione dipendenze

## 📄 Licenza

Progetto sviluppato per scopi universitari


