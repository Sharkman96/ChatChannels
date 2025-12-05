# ChatChannels

**ChatChannels** is an advanced, server‑side chat channels system for **Minecraft 1.20.1 (Forge)**.
It adds configurable global/local/trade/staff channels, private messages, rate limiting, logging, and integration with popular permission systems.

---

## ✨ Features

- **Multiple chat channels out of the box**
  - `global` – server‑wide chat
  - `local` – proximity chat with configurable radius
  - `trade` – trading / market channel
  - `staff` – staff‑only channel (permission‑gated)
  - `pm` – private messages channel (used internally)
- **Fully configurable channels**
  - Custom `id`, display `name`, `type`, `prefix`, color, radius and default channel
  - Per‑channel **permission node** (optional) – restrict who can see/use each channel
- **Permission system integration**
  - **LuckPerms** support (via official API)
  - **FTB Ranks** support (`ranks.snbt` permissions)
  - Fallback to **vanilla op level 4** if no permission plugin is available
- **Per‑player chat state**
  - Remember last used channel per player
  - Mute specific channels
  - Ignore specific players
- **Rate limiting / anti‑spam**
  - Configurable messages‑per‑time window per player
- **Logging**
  - All channel messages can be logged for moderation (server‑side)
- **Server config options**
  - Local chat radius
  - Rate limit (messages / seconds)
  - Optional **“joined/left channel”** system messages

---

## 🔌 Installation / Sides

ChatChannels is a **server‑focused mod**, but it must be installed on **both the server and all connecting clients**.

- **Singleplayer / Open to LAN**
  - Install the mod on the client. The integrated server will automatically run with the same mod.

- **Dedicated servers**
  - Put the mod JAR into the `mods` folder on the **server**.
  - Also include the same JAR in the **client modpack** for all players who join the server.

The mod is built as a standard Forge mod with `side = BOTH`:
it contains server‑side logic (channels, permissions, configs, logging) and client‑side code for handling network packets and displaying chat channels.

---

## 🧩 Requirements & Compatibility

- **Minecraft:** 1.20.1  
- **Loader:** Forge 47.x (built and tested against **Forge 47.4.0**)  
- **Java:** 17

**Works on:**

- **Dedicated servers** – core logic runs server‑side
- **Clients:** the mod is intended to be installed on both **server and client** for best experience (channel UI, packets, etc.)

**Optional integrations (not required, but supported):**

- **LuckPerms** – for permission checks like `chat.channels.global`
- **FTB Ranks** – for permission nodes and rank‑based name formatting:
  - Permission nodes in `ranks.snbt` such as:
    - `chat.channels.global`
    - `chat.channels.local`
    - `chat.channels.trade`
    - `chat.channels.staff`
  - Rank name formats via `ftbranks.name_format` (used as privilege tags)

If neither LuckPerms nor FTB Ranks are present, the mod falls back to vanilla operator level checks (`hasPermissions(4)`).

---

## ⚙️ Configuration Overview

The mod generates its configs on first run. There are **three main config files**:

### 1. `config/chatchannels.json`

Defines all chat channels and global rate limiting.

**Fields per channel:**

- `id` – internal channel ID (e.g. `"global"`, `"local"`, `"trade"`, `"staff"`, `"pm"`)
- `name` – display name shown in the UI (e.g. `"Global"`, `"Local"`)
- `type` – how the channel behaves:
  - `GLOBAL`, `LOCAL`, `TRADE`, `STAFF`, `TEAM`, `PRIVATE`, `CUSTOM`
- `prefix` – short prefix before messages, e.g. `"[G]"`, `"[Local]"`, `"[Trade]"`
- `color` – hex color string, e.g. `"#FFFFFF"`, `"#FFD700"`
- `radius` – for `LOCAL` channels: radius in blocks; `0` = global‑like broadcast
- `default` – `true` if this should be the default channel for new players
- `permission` – **permission node required** to use the channel
  - Example defaults:
    - `chat.channels.global`
    - `chat.channels.local`
    - `chat.channels.trade`
    - `chat.channels.staff`
  - If set to an empty string `""`, **no permission is required** for that channel.

**Rate limit section:**

```json
"rate_limit": {
  "messages": 5,
  "per_seconds": 3
}
```

- `messages` – how many messages a player may send
- `per_seconds` – time window in seconds  
If the limit is exceeded, the player receives a system message like "You are sending messages too fast."

---

### 2. `serverconfig/chatchannels-server.toml`

Server‑side gameplay settings:

- `localChat.radius`  
  Default radius (in blocks) for the default `LOCAL` channel.

- `rateLimit.messages`  
  Same as in `chatchannels.json`, but exposed as Forge server config.

- `rateLimit.seconds`  
  Time window for rate limiting.

- `channels.showSwitchMessages`  
  - `true` (default) – show server messages when a player joins or leaves a channel:
    - `Joined channel <id>`
    - `Left channel <id>`
  - `false` – do not send these system messages.

These messages are sent when the player uses the channel command / UI that triggers **JOIN/LEAVE** actions.

---

### 3. `config/chatchannels_privileges.json`

Controls **colors for privilege tags** (rank labels) shown before player names in chat.

- `defaultColor` – hex color for unknown/unspecified tags (e.g. `"#FFD700"`)
- `tags` – map from **privilege tag string** to a hex color:
  - The tag is derived from:
    1. LuckPerms prefix (if available), OR  
    2. `ftbranks.name_format` (if available), OR  
    3. `"OP"` for server operators

**Default example:**

```json
{
  "defaultColor": "#FFD700",
  "tags": {
    "Игрок": "#AAAAAA",
    "VIP": "#55FFFF",
    "Модератор": "#55FF55",
    "Админ": "#FF5555",
    "OP": "#FF5555"
  }
}
```

You can customize this file to match your server’s rank names and colors.

---

## 🔑 Permissions

By default, the following permission nodes are used:

- `chat.channels.global` – talk in the global channel
- `chat.channels.local` – talk in the local channel
- `chat.channels.trade` – talk in the trade channel
- `chat.channels.staff` – access the staff channel (for moderators/admins)

These nodes can be controlled via:

- **LuckPerms** (assign them to users/groups), or
- **FTB Ranks** (`ranks.snbt`), for example:

```snbt
default: {
  ...
  chat.channels.global: true
  chat.channels.local: true
  chat.channels.trade: true
}

vip: {
  ...
  chat.channels.global: true
  chat.channels.local: true
  chat.channels.trade: true
}
```

If you want a channel to be available to everyone without permissions, simply set its `permission` field in `chatchannels.json` to an empty string `""`.

---

## 📝 Notes

- All config files are **generated automatically** on first server start.
- Safe to customize and reload between server restarts.
- The mod does **not** add commands for channel creation yet — custom channels are defined purely through `chatchannels.json`.
