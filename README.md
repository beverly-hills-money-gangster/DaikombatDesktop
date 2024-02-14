# Daikombat Desktop

Daikombat Desktop is a Doom-like online shooter game with a non-stop death-match mode. It is built using Maven, LibGdx, and Java 14.

**Warning!** Assets are not included

## Distribution

Right now, the game is not published to any major platform. Please contact to the dev team to get a game archive.

## Screenshots

![Main menu](/screenshot-main-menu.png)

![Main menu](/screenshot-gameplay.png)

## Prerequisites

- Java 14
- Gradle 6.4

## Configuration

Desktop can be configured using the following environment variables:

- `GAME_HOST` Server host. Default - `localhost`
- `GAME_PORT` Server port. Default - `7777`
- `GAME_PASSWORD` Server password. Default - `daikombat`
- `GAME_ID` Server game id. Default - `0`
- `DEV_MODE` Dev mode. Default - `false`

## Development

### Handy Gradle commands

Here are some commands that might be useful while coding.

#### Create runnable jar file
```
gradle desktop:dist
```

### Testing and QA

To turn dev mode on, please run the game with `DEV_MODE=true` environment variable. Dev mode features:
- Smaller window size, so you can run multiple game windows at the same time and see IDE logs on-fly
- Cursor gets "un-caught" when "Esc" is pressed during death-match, so you can use the mouse to go back to IDE or run one more game window
- Basic network metrics are rendered on the screen during death-match
- No sound, so you can listen to music while coding