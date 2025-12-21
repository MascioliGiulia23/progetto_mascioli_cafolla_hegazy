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

## 📁 Struttura del progetto

- **src/main/java/**
  - `controller/` - Gestione logica mappa
  - `model/` - Modelli dati (GTFS, utenti, utils)
  - `service/` - Servizi business logic e API GTFS Realtime
  - `view/` - Interfaccia grafica Swing (frames, map, panels)
- **src/main/resources/**
  - `static_gtfs/` - Dati GTFS statici di ATAC Roma
- **src/test/java/**
  - Unit tests per model e utils
- `pom.xml` - Configurazione Maven

## 🛠️ Tecnologie

- Java Swing per l'interfaccia grafica
- JXMapViewer per la mappa
- GTFS Realtime API
- Maven per la gestione dipendenze

## 📄 Licenza

Progetto sviluppato per scopi universitari


