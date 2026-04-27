# FriendZone Android MVP

This folder contains a minimal Android MVP client (Kotlin + Compose + Hilt + Retrofit + OSM).

## Modules
- `app`: single module app with Compose UI, Retrofit API client, DataStore storage, and basic background location upload.

## Key features implemented
- `installId` + `clientId` stored in DataStore and registered via `POST /clients/register`.
- Zone CRUD: create, list, update, delete.
- Location upload (foreground stream) and event handling.
- Events history screen.
- Notifications for `ENTER/EXIT`.

## Notes
- Base URL defaults from `app/src/main/assets/config.yaml` and can be changed from the Zones screen.
- Location provider is abstracted behind `LocationProvider`.
- Map picker uses osmdroid `MapView` via `AndroidView` and defaults to Moscow center.
- Runtime permissions are requested on first launch.

## Local development
- Emulator uses `10.0.2.2` to access host machine.
- Physical device should use host machine IP in the same local network.
