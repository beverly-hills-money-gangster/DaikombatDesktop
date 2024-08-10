# Daikombat Desktop

Daikombat is a Doom-like online shooter game with a non-stop death-match mode. It is built using Maven, LibGdx, and Java 14. Server code is also publicly available on [GitHub](https://github.com/beverly-hills-money-gangster/Daikombat-server).

**WARNING** The game is NOT for commercial use. This repository is for educational purposes only.

## Distribution

Right now, the game can be downloaded from the [official web-site](https://beverly-hills-money-gangster.github.io/DaikombatDesktop/) or [itch.io](https://beverlyhillsmoneygangster.itch.io/daikombat).

![Main menu](/img/ps_one_cover_old_school.png)

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

The player spawns on the map and joins a death-match against other players online. The player who
gets 20(configurable) kills first is declared the winner. Notable features:
- Online mode
- In-game chat
- Leaderboard
- Power-ups
- Teleports
- Multiple weapons
- 3D sound
- Various player skins(blue, yellow, pink, purple, orange, and green)

### Video

[![YouTube](https://img.shields.io/badge/YouTube-%23FF0000.svg?style=for-the-badge&logo=YouTube&logoColor=white)](https://youtu.be/b0ZATG3TXaQ)

### Screenshots

![Main menu](/img/screenshot-main-menu.png)

![Enemy](/img/screenshot-enemy.png)

![Teleport](/img/screenshot-teleport.png)

![Death screen](/img/screenshot-death.png)


## Prerequisites

- Java 14
- Gradle 6.4

## Configuration

Desktop can be configured using the following environment variables:

- `DESKTOP_GAME_HOST` Server host. Default - `64.226.66.192`
- `DESKTOP_GAME_PORT` Server port. Default - `7777`
- `DESKTOP_GAME_ID` Server game id. Default - `0`
- `DESKTOP_FLUSH_ACTIONS_FREQ_MLS` Frequency(in milliseconds) at which desktop sends "MOVE" events to server. Default - `50`.
- `DESKTOP_DEV_MODE` Dev mode. Default - `false`
- `DESKTOP_SECONDARY_CONNECTIONS_TO_OPEN` Number of secondary TCP connections to create. Default - `3`

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

#### Debug controls
- N - see network metrics
- P - log current player position