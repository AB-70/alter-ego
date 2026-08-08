# AlterEgo

A Fabric mod for **Minecraft 26.2** that lets you become any living entity. Your character model is swapped for the chosen mob (visible to you in third person and to every other player), your nametag can be hidden, and many mobs grant you their signature abilities — become a creeper and you can explode.

## Features

- **Morph into any living entity** — every living mob in the game is selectable (~80 types), with full animation mirroring: walking, sneaking, sprinting, head/body rotation, and limb swing all track your real movement.
- **Fullscreen picker UI** (default key **G**): live 3D preview of the selected entity, searchable grid of all entities, per-ability toggles, and a nametag visibility option.
- **Ability system**: iconic mobs grant active abilities (fired with the ability key, default **V**) and/or passive abilities that apply continuously while morphed. Every ability can be individually disabled in the picker before applying.
- **Multiplayer-ready**: transformations are server-authoritative and broadcast to all clients. Works in singleplayer, LAN, and dedicated servers. Late joiners see everyone's morphs correctly.
- **Equipment mirroring**: humanoid morphs (skeleton, zombie, piglin...) visibly hold your items, draw bows when you do, and wear your armor. Endermen carry the block you're holding.
- **Vanilla-faithful visuals**: the creeper explosion uses the real swell + white-flash priming animation, the enderman teleport plays the vanilla sound and particle trail, projectiles use the authentic mob sounds.

## Controls

| Key | Action |
|-----|--------|
| **G** | Open the AlterEgo menu |
| **V** | Use your morph's active ability |

Both rebindable under Options → Controls → AlterEgo.

## Abilities

### Active abilities (V key)

| Mob | Ability | Notes |
|-----|---------|-------|
| Creeper | Explosion | Hiss + 1.5s fuse with swell animation, then a real explosion (respects `mobGriefing`). 5s cooldown |
| Enderman | Teleport | Blinks up to 32 blocks to where you're looking, with vanilla safety checks |
| Blaze | Fireball | Small fireball |
| Ghast | Ghast Fireball | Explosive large fireball |
| Wither | Wither Skull | |
| Snow Golem | Snowball | Rapid fire |
| Llama / Trader Llama | Spit | |

### Item grants

| Mob | Grant |
|-----|-------|
| Skeleton / Stray / Bogged | An Infinity-enchanted bow + arrow while morphed; automatically reclaimed on revert, respawn, or disconnect |

### Passive abilities

| Ability | Mobs |
|---------|------|
| Wall Climbing (push against a wall to climb, sneak to hang) | Spider, Cave Spider |
| Flight (creative-style, double-tap jump) | Allay, Bat, Bee, Parrot, Vex, Phantom, Ghast, Blaze, Ender Dragon, Wither |
| Fire Immunity | Blaze, Ghast, Wither, Magma Cube, Strider, Wither Skeleton, Zombified Piglin |
| Water Breathing | Squid, Glow Squid, Cod, Salmon, Tropical Fish, Pufferfish, Tadpole, Guardian, Elder Guardian, Axolotl, Turtle, Drowned, Dolphin |
| No Fall Damage | Cat, Ocelot, Chicken, Goat, Rabbit |
| Strong Jump | Rabbit, Goat |
| Swiftness | Horse, Fox, Cat, Ocelot |
| Strength (+8 melee damage) | Iron Golem, Ravager, Polar Bear, Warden |

All other living entities are fully morphable cosmetically and simply grant no abilities (yet — adding one is a few lines in `AbilityRegistry` + `EgoManager`/`ActiveAbilities`).

## Requirements

- Minecraft **26.2**
- **Fabric Loader** 0.19.3+
- **Fabric API**
- Java 25 (bundled with the official launcher / CurseForge)

For multiplayer, the mod must be installed on the **server and every client**.

## Known limitations

- Morphs are not persisted — relogging or a server restart reverts everyone to their own body.
- Your hitbox stays player-sized regardless of the morph.
- First-person view is unchanged by design (you see your own hands; the morph shows in third person and to others).

## Building from source

```
./gradlew build        # jar lands in build/libs
./gradlew runClient    # launch a dev client
./gradlew runServer    # launch a dev server
```

Requires JDK 25. The project uses Fabric Loom with Mojang mappings.

## License

**All Rights Reserved.** You may download and play with the mod; redistribution, re-uploading, or bundling in modpacks requires the author's permission. See [LICENSE](LICENSE).
