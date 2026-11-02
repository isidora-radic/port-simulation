# Port Simulation

A multithreaded desktop application built with Java and JavaFX to simulate vessel movement and docking across multiple port terminals.

Developed independently by **Isidora Radić** as an academic project at the University of Banja Luka, Faculty of Electrical Engineering. The original project name is **Luka** (Port). The application interface and most source identifiers are in Serbian.

## Features

- Manage vessels through administrative interfaces: add, edit, and remove vessel records.
- Model container ships, passenger cruisers, and tankers using object-oriented inheritance.
- Run vessels as separate threads and coordinate access to shared terminal resources.
- Display terminal grids and vessel movement in a JavaFX simulation interface.
- Simulate maritime incidents and service responses, including checks against a vessel watchlist.
- Save and restore port state with Java serialization.
- Calculate port fees and export payment records to CSV.

## Technologies

Java, JavaFX, Java threads and synchronization, Java Serialization, Java Properties, CSV export, Eclipse.

## Project structure

| Directory | Responsibility |
| --- | --- |
| `src/application` | Application entry point (`application.Main`) |
| `src/gui/admin` | Vessel administration and editing forms |
| `src/gui/user` | Simulation interface |
| `src/model` | Port, terminals, vessels, incidents, services, and payment records |
| `src/util` | Configuration, generators, persistence, logging, and CSV export |
| `src/config` | Simulation settings |

## Running the application

This is an Eclipse Java project with a manually configured JavaFX library; it does not include a Maven or Gradle build.

### Requirements

- JDK 22 (the version configured in the included Eclipse project).
- A JavaFX SDK compatible with JDK 22 and your operating system, for example JavaFX 22.
- Eclipse with Java support.

The JavaFX SDK is not bundled in this repository. Its exact original version was not recorded in the uploaded project.

### Eclipse setup

1. Extract or clone the repository to a local folder.
2. Import it through **File > Import > General > Existing Projects into Workspace**, selecting the folder containing `.project`.
3. Configure the project's Java runtime to use JDK 22.
4. Create an Eclipse **User Library** named `JavaFX` and add the JAR files from your JavaFX SDK's `lib` directory. The included `.classpath` references this library.
5. Create a Java Application run configuration for `application.Main`.
6. Add these **VM arguments**, replacing the example path with your own JavaFX SDK location:

   ```text
   --module-path "C:\path\to\javafx-sdk-22\lib" --add-modules javafx.controls
   ```

7. Set the working directory to the project root and run the application.

The `src/config/luka.properties` resource must be included in the build output. Eclipse normally copies it when building the `src` folder.

### First run

The application starts with the administration window. Select a terminal to manage vessels, or use **Pokreni korisnički dio** to open the simulation window. In that window, enter the vessel count per terminal and use **Pokreni simulaciju** to start. Use **Preuzmi CSV** in the administration window to export fee records.

## Configuration and local files

Edit `src/config/luka.properties` to configure the simulation:

```properties
broj.terminala = 7
milisekundi.po.satu=1000
```

- `broj.terminala`: number of terminals.
- `milisekundi.po.satu`: real milliseconds representing one simulated hour.

The application loads `luka.ser` from the working directory if it exists; otherwise it creates a new port. Saved state must match the configured terminal count. To start fresh, close the application and move your existing `luka.ser` to a backup location before relaunching.

Logs and serialized state are generated locally and excluded from version control. Incident reports are written as `uvidjaj-*.bin` and `potjernica-*.bin` in the user's home directory. CSV exports are saved to the location selected in the application.

## Scope

This repository contains an academic simulation, not a production port management system. The setup instructions reflect the source code and Eclipse configuration. The GitHub preparation review did not include an interactive run under JDK 22 with JavaFX.
