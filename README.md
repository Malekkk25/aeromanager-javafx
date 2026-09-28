# AeroManager: Flight Management (JavaFX)

Desktop application for managing an airline's operations: **airports, flights, stopovers, reservations, employees and statistics**. Built with JavaFX and SQLite, and designed around object-oriented principles (sealed classes, interfaces, enums, custom exceptions).

## Features

- **Authentication** with roles: administrator, flight agent, check-in agent
- **Employees**: manage staff accounts (admin)
- **Airports**: add, edit and delete airports
- **Flights**: national and international flights with a status lifecycle (scheduled, check-in, boarding, delayed, closed, in flight, cancelled...)
- **Stopovers (escales)**: ordered stops per flight, with validation of arrival and departure times
- **Reservations**: book seats by class (first, business...), with a price computed from the class
- **Statistics dashboard**: charts of flights per destination and national vs. international split
- SQLite database created and initialised automatically on first launch

## OOP concepts used

| Concept | Where |
|---------|-------|
| Sealed classes | `Vol` permits `VolNational` and `VolInternational`; `Employe` permits `AgentVol` and `AgentEnregistrement` |
| Abstract classes | `Vol`, `Employe` (`afficherRole()`) |
| Interface | `Reservable` (price calculation by class) |
| Enums | `StatutVol`, `ClasseVol` |
| Custom exceptions | `ReservationException`, `EscaleInvalideException`, `VolInexistantException` |
| MVC | Model / Controller packages and FXML views |

## Tech stack

- Java 17, JavaFX 17 (FXML, CSS, charts)
- SQLite (`sqlite-jdbc`)
- Maven (with wrapper)

## Getting started

### Prerequisites

- JDK 17 or higher

### Run

```bash
git clone https://github.com/Malekkk25/aeromanager-javafx.git
cd aeromanager-javafx
./mvnw clean javafx:run
```

On Windows use `mvnw.cmd clean javafx:run`.

The database file `gestionvols.db` is created in the project folder and the tables (`employe`, `aeroport`, `escale`, `vol`, `reservation`) are initialised on startup. A default administrator account is created so you can sign in the first time; change its credentials before any real use.

## Project structure

```
src/main/java/com/example/projet_java_vols/
├── ConnexionDB.java                 # SQLite connection and schema setup
├── HelloApplication.java            # entry point
├── Gestion_des_utilisateurs/        # employees, reservations, login
│   ├── Controller/
│   └── Model/
└── Gestion_des_vols/                # airports, flights, stopovers, statistics
    ├── Controller/
    └── Model/
src/main/resources/.../              # FXML views and style.css
```

## Author

**Malek** ([@Malekkk25](https://github.com/Malekkk25))
