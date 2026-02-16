<div align="center">

# 🚌 Rome Transit Tracker

### Monitoraggio in tempo reale dei mezzi pubblici di Roma

[![Java](https://img.shields.io/badge/Java-23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Swing](https://img.shields.io/badge/UI-Swing-6DB33F?style=for-the-badge&logo=java&logoColor=white)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![GTFS](https://img.shields.io/badge/GTFS-Static_&_Realtime-brightgreen?style=for-the-badge)](https://gtfs.org/)
[![JUnit](https://img.shields.io/badge/Tests-JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Mockito](https://img.shields.io/badge/Mockito-5.12-9C27B0?style=for-the-badge)](https://site.mockito.org/)
[![Javadoc](https://img.shields.io/badge/Javadoc-Online-blue?style=for-the-badge)](https://mascioligiulia23.github.io/progetto_mascioli_cafolla_hegazy/)

</div>

---

## 📖 Descrizione del Progetto

**Rome Transit Tracker** è un'applicazione desktop sviluppata in Java che permette di visualizzare su mappa interattiva la posizione in tempo reale dei mezzi pubblici di Roma (bus, tram, metro). Il progetto integra i dati statici del feed **GTFS** di Roma Mobilità con i feed **GTFS Realtime** per fornire aggiornamenti live, previsioni di arrivo e un monitoraggio completo della qualità del servizio.

L'applicazione è stata realizzata da un team di tre sviluppatori seguendo i principi di programmazione ad oggetti, design pattern consolidati (MVC, Observer, Service Layer) e best practice di sviluppo collaborativo (Git, Maven, Testing).

🔗 **Repository GitHub:** [https://github.com/MascioliGiulia23/progetto_mascioli_cafolla_hegazy.git](https://github.com/MascioliGiulia23/progetto_mascioli_cafolla_hegazy.git)  
📚 **Documentazione Javadoc:** [https://mascioligiulia23.github.io/progetto_mascioli_cafolla_hegazy/](https://mascioligiulia23.github.io/progetto_mascioli_cafolla_hegazy/)

---

## Funzionalità Principali

L'applicazione copre tutti i livelli richiesti, dal base all'avanzato:

### 🗺️ Mappa e Visualizzazione
- **Mappa interattiva** basata su OpenStreetMap (tramite JXMapViewer2) con funzioni di zoom e pan.
- **Visualizzazione in tempo reale** della posizione dei mezzi, con distinzione per linea e direzione.
- **Aggiornamento automatico** della posizione dei bus (ogni 30 secondi) quando online.

### 🔍 Ricerca e Pianificazione
- **Ricerca fermate e linee** con visualizzazione delle prossime corse in arrivo.
- **Predizione dell'orario di arrivo** basata sia sui dati statici (orario schedulato) che sui dati real-time (ritardi).
- **Sistema di tracking "intelligente"** che tiene conto dei ritardi storici per previsioni più accurate (livello avanzato).

### 👤 Profilo Utente e Personalizzazione
- **Autenticazione utente** e gestione personalizzata dei preferiti (linee e fermate).
- **Salvataggio e sincronizzazione** dei preferiti per ogni utente.
- **Dashboard "Qualità del Servizio"** che mostra statistiche e monitoraggio dei ritardi per linea/fermata (livello avanzato).

### ⚙️ Tecnologie e Affidabilità
- **Switch automatico online/offline:** L'app funziona perfettamente anche in assenza di connessione, utilizzando i dati GTFS statici.
- **Supporto dual-mode:** I dati real-time vengono utilizzati quando disponibili per aggiornare mappa e previsioni.
- **Suite di test completa:** 24 classi di test unitari e di integrazione per garantire stabilità e correttezza.

---

## 🏗️ Architettura del Progetto

Il progetto segue una chiara architettura **Model-View-Controller (MVC)** per garantire separazione delle responsabilità, manutenibilità e testabilità.


---


### Design Pattern Chiave
- **MVC:** `MapController` funge da coordinatore tra `view` e `model`.
- **Observer:** Listener personalizzati per eventi UI e `ConnectivityService` che notifica cambi di stato.
- **Service Layer:** I servizi (`GtfsService`, `RealTimeDelayService`) incapsulano la logica di business.
- **Builder:** `LineStopsViewBuilder` per la costruzione dinamica di viste complesse.
- **Helper/Support:** Classi come `FavoritesSupport` per separare la logica di supporto dai componenti principali.

---

## 🛠️ Tecnologie e Librerie Utilizzate

| Categoria | Tecnologia/Libreria | Scopo |
| :--- | :--- | :--- |
| **Core** | Java 23 | Linguaggio di programmazione |
| **UI** | Java Swing + FlatLaf (3.4) | Interfaccia grafica e look & feel moderno |
| **Mappe** | JXMapViewer2 (2.8) | Visualizzazione mappe OpenStreetMap |
| **Dati Trasporti** | GTFS Realtime Bindings (0.0.4) | Parsing dei feed protobuf di Roma Mobilità |
| **Build & Dipendenze** | Maven | Gestione del ciclo di vita e delle dipendenze |
| **Testing** | JUnit 5, Mockito (5.12) | Suite di test unitari e mocking |
| **Documentazione** | Maven Javadoc Plugin | Generazione automatica della documentazione |

---

## Guida all'Installazione e Avvio

### Prerequisiti
Assicurati di avere installato:
- **Java Development Kit (JDK) 23** o superiore. ([Scarica qui](https://www.oracle.com/java/technologies/javase-downloads.html))
- **Apache Maven 3.6+** ([Guida all'installazione](https://maven.apache.org/install.html))

### Passi per l'esecuzione

1.  **Clona il repository:**
    ```bash
    git clone https://github.com/MascioliGiulia23/progetto_mascioli_cafolla_hegazy.git
    cd progetto_mascioli_cafolla_hegazy
    ```

2.  **Compila il progetto e crea il JAR eseguibile:**
    ```bash
    mvn clean package -DskipTests
    ```
    *(Il flag `-DskipTests` salta l'esecuzione dei test per velocizzare la build. Per eseguire anche i test, rimuoverlo.)*

3.  **Esegui l'applicazione:**
    ```bash
    java -jar target/progetto_mascioli_cafolla-1.0-SNAPSHOT-jar-with-dependencies.jar
    ```

---

## Esecuzione dei Test

Per lanciare la suite completa di test (24 classi) e verificare il corretto funzionamento di tutti i moduli:

```bash
mvn clean test
```

## 👥 Team di Sviluppo

Il progetto è stato realizzato nell'ambito di un esame universitario, con una chiara divisione dei ruoli:

| Nome | Ruolo | Responsabilità Principali |
| :--- | :--- | :--- |
| **Giulia Mascioli** | Backend Developer | Architettura backend, servizi GTFS (statico e real-time), gestione connettività, logica di calcolo ritardi e previsioni, controller principale. |
| **Ludovica Cafolla** | Frontend Developer | Progettazione UI/UX, sviluppo di tutti i pannelli grafici, sistema di preferiti, gestione temi e listener, integrazione della mappa. |
| **Engi Naser Hegazy** | Test Developer | Progettazione e implementazione della suite di test (24 classi), test di integrazione, validazione modelli dati e servizi, utilizzo di Mockito. |

---

## 📄 Licenza

Questo progetto è stato sviluppato per scopi didattici e non ha una licenza specifica. I dati GTFS sono di proprietà di Roma Mobilità e vengono utilizzati in conformità con i loro termini di servizio.
