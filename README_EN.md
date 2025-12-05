# ChatChannels

**Minecraft Forge 1.20.1 Mod** — Advanced chat channels system with UI, permissions, logging and rate-limiting.

## ✨ Features

- **Chat Channels** — Configurable channels for organized communication (global, local, trade, etc.)
- **Private Messages** — Direct messaging between players
- **Permissions System** — Channel access control via permissions
- **UI Screen** — Convenient interface for switching channels
- **Rate-limiting** — Spam protection
- **Logging** — Message history recording

## 📋 Commands

| Command | Description |
|---------|-------------|
| `/channel list` | Show available channels |
| `/channel join <channel>` | Join a channel |
| `/channel leave <channel>` | Leave a channel |
| `/ch <message>` | Send message to current channel |
| `/pm <player> <message>` | Send private message to player |

## 🔧 Installation

### For Server/Client
1. Install [Minecraft Forge 1.20.1](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html) (version 47.4.0+)
2. Download the `.jar` file from [Releases](https://github.com/Sharkman96/ChatChannels/releases)
3. Place it in the `mods/` folder

### For Development
```bash
git clone https://github.com/Sharkman96/ChatChannels.git
cd ChatChannels
./gradlew genIntellijRuns   # for IntelliJ IDEA
./gradlew genEclipseRuns    # for Eclipse
```

## ⚙️ Configuration

Configuration files are automatically created in the `config/` folder on first launch.

## 📦 Requirements

- Minecraft: **1.20.1**
- Forge: **47.4.0+**

## 📄 License

All Rights Reserved © ChatChannels Team

## 🤝 Authors

- **Sharkman96** — Lead Developer
