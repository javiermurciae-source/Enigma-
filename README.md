# Enigma 🔒

Android privacy browser with fingerprint spoofing and session isolation.

## Features

- 🎭 **Fingerprint Spoofing** — Generate unique device identities
- 🔄 **Session Isolation** — Each profile has its own isolated session
- 🌐 **Proxy Support** — HTTP/SOCKS5 proxy per profile
- 🗑️ **Panic Wipe** — Instant session destruction
- 📍 **IP Monitoring** — Track IP changes and react automatically
- 🔒 **Clipboard Clear** — Auto-clear sensitive data
- 🚫 **Ad Blocking** — Lightweight built-in blocker

## Build

```bash
./gradlew assembleRelease
```

## Tech Stack

- Kotlin + Jetpack Compose
- WebView with custom fingerprint injection
- Material 3 with Dynamic Color
- MVVM Architecture

## License

MIT