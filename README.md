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

progetto_mascioli_cafolla_/
├── progetto_mascioli_cafolla_mappa_realtime/
│ ├── src/
│ │ ├── main/
│ │ │ ├── java/
│ │ │ │ ├── controller/
│ │ │ │ │ └── MapController.java
│ │ │ │ ├── model/
│ │ │ │ │ ├── gtfs/ # Entità GTFS
│ │ │ │ │ │ ├── CalendarDate.java
│ │ │ │ │ │ ├── Fermate.java
│ │ │ │ │ │ ├── Route.java
│ │ │ │ │ │ ├── ShapeRoute.java
│ │ │ │ │ │ ├── StopTime.java
│ │ │ │ │ │ └── Trip.java
│ │ │ │ │ ├── user/ # Gestione utenti
│ │ │ │ │ │ ├── Favorite.java
│ │ │ │ │ │ ├── User.java
│ │ │ │ │ │ └── UserManager.java
│ │ │ │ │ └── utils/
│ │ │ │ │ ├── Database.java
│ │ │ │ │ └── GeoUtils.java
│ │ │ │ ├── service/ # Servizi business logic
│ │ │ │ │ ├── GtfsService.java
│ │ │ │ │ ├── GtfsRealtimeService.java
│ │ │ │ │ ├── MapService.java
│ │ │ │ │ ├── RealTimeFetcher.java
│ │ │ │ │ └── RealTimeParser.java
│ │ │ │ └── view/ # Interfaccia grafica
│ │ │ │ ├── Jframe.java
│ │ │ │ ├── frames/
│ │ │ │ │ ├── MapInitializer.java
│ │ │ │ │ └── Mappa.java
│ │ │ │ ├── map/
│ │ │ │ │ ├── BusWaypoint.java
│ │ │ │ │ ├── RouteDrawer.java
│ │ │ │ │ └── WaypointDrawer.java
│ │ │ │ └── panels/
│ │ │ │ ├── FavoritesPanel.java
│ │ │ │ ├── SearchBar.java
│ │ │ │ ├── SearchResultsPanel.java
│ │ │ │ ├── ServiceQualityPanel.java
│ │ │ │ └── UserProfilePanel.java
│ │ │ └── resources/
│ │ │ └── static_gtfs/ # Dati GTFS statici
│ │ │ ├── routes.txt
│ │ │ ├── stops.txt
│ │ │ ├── trips.txt
│ │ │ └── ...
│ │ └── test/ # Unit tests
│ │ └── java/
│ │ └── model/
│ │ ├── gtfs/
│ │ ├── user/
│ │ └── utils/
│ └── pom.xml
└── README.md

## 🛠️ Tecnologie

- Java Swing per l'interfaccia grafica
- JXMapViewer per la mappa
- GTFS Realtime API
- Maven per la gestione dipendenze

## 📄 Licenza

Progetto sviluppato per scopi universitari


