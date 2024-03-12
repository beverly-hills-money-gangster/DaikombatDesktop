# Daikombat Desktop

Daikombat is a Doom-like online shooter game with a non-stop death-match mode. It is built using Maven, LibGdx, and Java 14.


## Plot

In a world where the boundaries between heaven and hell blur, the once-heroic Doom Guy finds himself plunged into an eternal nightmare.
Consumed by his lust for power and vengeance, he defied the divine order, earning the wrath of God himself.
Cursed with immortality and condemned to an existence as a vampire, Doom Guy is trapped in a perpetual cycle of violence and suffering.

As his punishment, he is thrust into the depths of an otherworldly arena, where sinners and fallen warriors alike
are condemned to an eternal deathmatch. Here, amidst the swirling chaos and bloodshed, Doom Guy must confront his
own inner demons while battling against the vilest of sinners.

Each match is a brutal struggle for survival, with the stakes higher than ever before. Every victory brings him closer
to redemption, yet every defeat plunges him deeper into the abyss of his own damnation.

## Gameplay

[![YouTube](https://img.shields.io/badge/YouTube-%23FF0000.svg?style=for-the-badge&logo=YouTube&logoColor=white)](http://www.youtube.com/watch?v=4c-oMjbMRY4)


![Main menu](/screenshot-main-menu.png)

![Gameplay](/screenshot-gameplay.png)


## Prerequisites

- Java 14
- Gradle 6.4

## Configuration

Desktop can be configured using the following environment variables:

- `DESKTOP_GAME_HOST` Server host. Default - `localhost`
- `DESKTOP_GAME_PORT` Server port. Default - `7777`
- `DESKTOP_GAME_ID` Server game id. Default - `0`
- `DESKTOP_FLUSH_ACTIONS_FREQ_MLS` Frequency(in milliseconds) at which desktop sends "MOVE" events to server. Default - `50`.
- `DESKTOP_DEV_MODE` Dev mode. Default - `false`

## Development

### Handy Gradle commands

Here are some commands that might be useful while coding.

#### Create runnable jar file
```
gradle desktop:dist
```

### Testing and QA

To turn dev mode on, please run the game with `DESKTOP_DEV_MODE=true` environment variable. Dev mode features:
- Smaller window size, so you can run multiple game windows at the same time and see IDE logs on-fly
- Cursor gets "un-caught" when "Esc" is pressed during death-match, so you can use the mouse to go back to IDE or run one more game window
- Basic network metrics are rendered on the screen during death-match

## Distribution

Right now, the game can be downloaded from the [official web-site](https://beverly-hills-money-gangster.github.io/DaikombatDesktop/).
