# AlterEgo

A Fabric mod for Minecraft 26.2 that lets you become any entity: your character model is swapped for the chosen mob, your nametag can be hidden (configurable), and you gain the abilities of the entity you embody — become a creeper and you can explode.

## Requirements

- Minecraft 26.2 (Fabric loader >= 0.19.3)
- Fabric API
- Java 25

## Development

```
./gradlew build          # build the mod jar (build/libs)
./gradlew runClient      # launch a dev client
./gradlew runServer      # launch a dev server
```

## Multiplayer

AlterEgo is designed to run on both client and server. Transformations are handled server-side and synced to all players, so it works in singleplayer, LAN, and dedicated servers — as long as the mod is installed on both the server and every client.
