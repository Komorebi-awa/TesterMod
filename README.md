# Tester Mod

A Minecraft NeoForge 1.21.1 mod that provides a configurable damage testing entity.

## Features

### Tester Entity

- **High Health**: 1000 HP, sufficient for extensive damage testing
- **Damage Tracking**: Real-time damage output including raw damage and actual damage (after invulnerability reduction)
- **Invulnerability**: Tester won't take real damage but simulates damage invulnerability logic
- **Multi-Tester Identification**: Damage output includes tester name prefix for distinguishing multiple testers
- **Fire Resistance**: Ignores fire-type damage if the tester has Fire Resistance effect

### Tester Setter Item

Obtain from the **Tools** creative tab, or use command `/give @s tester_mod:tester_setter`

#### Usage

| Action | Effect |
|--------|--------|
| **Right-click on block** | Spawn a tester entity at the clicked position |
| **Right-click on tester** | Toggle knockback state (enabled/disabled) |
| **Shift + Right-click on tester** | Toggle zero-damage output visibility |

### Damage Output Format

```
[Tester Name] Damage: actual_damage/raw_damage, Type: damage_type, Tick: game_tick
```

### Commands

| Command | Description |
|---------|-------------|
| `/remove_tester` | Remove all testers created by yourself |
| `/remove_tester all` | Remove all testers in the world |

## Technical Details

### Entity Attributes

- Movement Speed: 0 (stationary)
- Knockback Resistance: 0 (configurable)
- Invulnerability Duration: 10 ticks (~0.5 seconds)

### Damage Invulnerability Logic

When the tester receives damage during invulnerability:
- If new damage ≤ last damage → actual damage = 0
- If new damage > last damage → actual damage = new damage - last damage

## Installation

1. Clone this repository
2. Open the project in IntelliJ IDEA or Eclipse
3. Run `gradlew --refresh-dependencies` to refresh dependencies
4. Run `gradlew runClient` to start the client for testing

## Building

```bash
gradlew build
```

Built artifacts are located in `build/libs/`.

## Resources

- Project uses NeoForge MDK template
- Mapping names use official Mojang mappings

## Additional Resources

- NeoForged Documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/