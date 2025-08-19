# SpeedrunnerSwap

SpeedrunnerSwap is a feature-rich, production-ready Minecraft plugin for Paper/Spigot 1.20.6, designed to be the definitive, open-source version of the "Speedrunner Swap VS Hunters" challenge inspired by Dream's popular video series.

## Overview

The plugin facilitates a contest between two teams: **Runners** and **Hunters**.

The **Runners** share a single player "body." This means they share the same inventory, health, hunger, location, and all other player states. Control of this shared body swaps between the runners on a configurable timer. The goal of the runners is to beat the game (e.g., defeat the Ender Dragon).

The **Hunters'** goal is to eliminate the active runner before they can achieve their objective.

When a runner is not in control, they are "blacked out"—rendered blind and immobile until it's their turn again.

## Features

*   **Full State Synchronization**: On every swap, the active runner's entire state is deeply copied to the next runner, including inventory, health, food, XP, location, and potion effects.
*   **Advanced Swap Timer**: Supports both `FIXED` and `RANDOM` swap intervals. The random mode uses a Gaussian distribution for more natural-feeling, less predictable swaps.
*   **Game State Persistence**: The game state is saved to a `game_state.json` file on pause or server shutdown. The game can be safely resumed exactly where it left off, even after a full server restart.
*   **Immersive Blackout State**: Inactive runners are rendered completely blind and immobile, and are isolated from runner-to-runner communication in chat and private messages.
*   **Configurable Hunter Tracker**: Hunters can track the runner using a combination of modes:
    *   `COMPASS`: A standard compass that points to the runner's last location. Can be configured to only work in the same world.
    *   `GLOWING`: Applies a glowing effect to the runner that is only visible to hunters.
    *   `ACTION_BAR`: Displays the runner's coordinates in the hunter's action bar.
*   **Fairness and Administration**:
    *   **Safe Swaps**: Ensures players are not swapped into dangerous blocks like lava or inside a wall.
    *   **Grace Period**: A configurable period of invulnerability for the newly swapped-in runner.
    *   **World Whitelist**: Restrict game creation to specific worlds.
*   **Comprehensive GUI**: A full GUI system (`/swap gui`) for managing teams and changing key game settings live without needing to edit the config file.
*   **Extensibility for Developers**:
    *   **PlaceholderAPI Support**: Exposes placeholders for use in other plugins.
    *   **Custom Events**: Fires a rich set of custom Bukkit events for other plugins to hook into.

## Commands and Permissions

The main command is `/swap`. All subcommands require the `speedrunnerswap.command` permission, which is granted to OPs by default.

*   `/swap gui`: Opens the main graphical user interface.
*   `/swap start`: Starts the game.
*   `/swap stop`: Stops the game.
*   `/swap pause`: Pauses the game.
*   `/swap resume`: Resumes a paused game.
*   `/swap status`: Shows the current status of the game.
*   `/swap setrunners <player1> [player2] ...`: Sets the players for the runner team.
*   `/swap sethunters <player1> [player2] ...`: Sets the players for the hunter team.
*   `/swap reload`: Reloads the configuration file (this will stop any running game). Requires `speedrunnerswap.admin`.

## PlaceholderAPI Placeholders

If you have PlaceholderAPI installed, you can use the following placeholders:

*   `%speedrunnerswap_active_runner%`: Displays the name of the current active runner.
*   `%speedrunnerswap_time_left%`: Displays the number of seconds until the next swap.
*   `%speedrunnerswap_time_left_formatted%`: Displays the time until the next swap in `MM:SS` format.
*   `%speedrunnerswap_game_status%`: Displays the current game status (Running, Paused, or Stopped).

## API for Developers (Custom Events)

You can hook into the plugin's logic by listening for these custom Bukkit events:

*   `GameStartEvent`: Fired when a game starts.
*   `GameStopEvent`: Fired when a game is stopped.
*   `GamePauseEvent`: Fired when a game is paused.
*   `GameResumeEvent`: Fired when a game is resumed.
*   `PlayerSwapEvent`: Fired when control swaps between runners.
    *   `getPreviousRunner()`: Returns the `Player` who was previously active.
    *   `getNewRunner()`: Returns the `Player` who is now active.

## Sample `config.yml`

```yaml
# SpeedrunnerSwap Configuration
# Version 3.0.0

# General Game Rules
# A list of worlds where the game is allowed to be started and played.
# If the list is empty, all worlds are allowed.
allowed-worlds:
  - "world"
  - "world_nether"
  - "world_the_end"

# Teams Configuration
teams:
  runners: []  # List of runner player names
  hunters: []  # List of hunter player names

# Swap System Configuration
swap:
  # Fixed interval or random swap with Gaussian jitter
  randomize: true
  # Base interval in seconds (or mean if randomized)
  interval: 60
  # Min and max interval in seconds (only used if randomize is true)
  min_interval: 30
  max_interval: 90
  # Jitter settings (only used if randomize is true)
  jitter:
    # Standard deviation in seconds
    stddev: 15
    # Whether to clamp within min/max limits
    clamp: true
  # Grace period after swaps (in ticks, 20 ticks = 1 second)
  grace_period_ticks: 40
  # Auto-pause if a runner disconnects
  pause_on_disconnect: true

# Safe Swap Configuration
safe_swap:
  # Whether to enable safe swap feature
  enabled: true
  # Horizontal scan radius (blocks)
  horizontal_radius: 5
  # Vertical scan distance (blocks)
  vertical_distance: 10
  # Dangerous blocks to avoid (material names)
  dangerous_blocks:
    - LAVA
    - MAGMA_BLOCK
    - CACTUS
    - CAMPFIRE
    - FIRE
    - SOUL_FIRE
    - SOUL_CAMPFIRE
    - POINTED_DRIPSTONE

# Freeze Mode for Inactive Runners
# Options: EFFECTS, SPECTATOR
freeze_mode: EFFECTS

# Movement and Interaction Control
cancel:
  # Cancel movement for inactive runners
  movement: true
  # Cancel interactions for inactive runners
  interactions: true

# Hunter Tracker Configuration
tracker:
  # How often to update the tracker (in ticks, 20 ticks = 1 second)
  update_ticks: 20
  # Modes can be a combination of: COMPASS, GLOWING, ACTION_BAR
  modes:
    - COMPASS
    - ACTION_BAR
  # If true, the compass will only point to the runner if the hunter is in the same world.
  compass-requires-same-world: true

# GUI Configuration
gui:
  main_menu:
    title: "SpeedrunnerSwap Menu"
    rows: 3
  team_selector:
    title: "Team Selector"
    rows: 4
  settings:
    title: "Settings"
    rows: 5

# Broadcast Configuration
broadcasts:
  # Whether to broadcast swap events to all players
  enabled: true
  # Whether to broadcast game start/stop events
  game_events: true
  # Whether to broadcast team changes
  team_changes: true

# Simple Voice Chat Integration (if available)
voice_chat:
  # Whether to integrate with Simple Voice Chat
  enabled: true
  # Whether to mute inactive runners
  mute_inactive_runners: true

# Audio-Visual Effects Configuration
effects:
  swap-warning:
    enabled: true
    sound: "BLOCK_NOTE_BLOCK_PLING"
    volume: 1.0
    pitch: 1.5
    title: "&e&lSWAP IN 10 SECONDS"
    subtitle: ""
  swap-execute:
    enabled: true
    sound: "ENTITY_ENDERMAN_TELEPORT"
    volume: 1.0
    pitch: 1.0
    title: "&c&lSWAPPED!"
    subtitle: "&fTo %new_runner%"
  game-start:
    enabled: true
    sound: "ENTITY_PLAYER_LEVELUP"
    volume: 1.0
    pitch: 1.0
    title: "&a&lGAME STARTED"
    subtitle: "&eGood luck, runners!"
  game-end:
    enabled: true
    sound: "ENTITY_WITHER_DEATH"
    volume: 1.0
    pitch: 1.0
    title: "&c&lGAME OVER"
    subtitle: ""

# Chat & Communication Rules
chat-isolation:
  # If true, runners will not be able to see chat messages from each other.
  block-runner-chat: true
  # If true, runners will not be able to send private messages to each other (e.g. /msg, /tell).
  block-runner-pms: true
  # Message sent to a runner if they try to message another runner.
  message-block-reply: "&cYou cannot communicate with other runners during the game."
```