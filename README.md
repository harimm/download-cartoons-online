[![Licence](https://img.shields.io/github/license/Ileriayo/markdown-badges?style=for-the-badge)](./LICENSE)
[![Known Vulnerabilities](https://snyk.io/test/github/harimm/download-cartoons-online/badge.svg)](https://snyk.io/test/github/harimm/download-cartoons-online)

# download-cartoons-online

Java 21 + Spring Context/Core application for downloading cartoon episodes by crawling series pages, resolving video stream URLs, and saving media files locally.

> **Project status:** The original target site (`watchcartoonsonline`) is currently unavailable/changed, so this project may require selector and extraction updates before it works against a live source again.

## Table of Contents
- [Overview](#overview)
- [How it Works](#how-it-works)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Configuration](#configuration)
- [Run the Application](#run-the-application)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Overview

`download-cartoons-online` is a non-Spring-Boot Java application that:
1. reads external configuration from `application.properties`,
2. discovers episode links for configured cartoon series,
3. finds playable/downloadable media URLs from episode pages,
4. downloads episode files to configured folders,
5. tracks downloaded and ignored episodes to avoid duplicate work.

## How it Works

Main runtime flow:

1. `CartoonDownloader` bootstraps the Spring context.
2. `DownloadManager` iterates configured cartoon IDs.
3. `PageWalker` fetches a series page and extracts episode links.
4. `EpisodeTracker` checks if each episode is `Pending`, `Downloaded`, or `Ignored`.
5. For pending episodes:
   - `VideoFinder` resolves the final video URL from episode HTML,
   - `DownloadWorker` downloads `<episode>.mp4` to the target folder,
   - `EpisodeTracker` appends the episode name to `episodeList`.

Depending on configuration, downloads can run per-series asynchronously and optionally wait for completion before starting the next series.

## Tech Stack

- **Language:** Java 21
- **Build:** Gradle (Groovy DSL)
- **Framework:** Spring Context/Core (non-Boot)
- **HTTP client:** Apache HttpClient 5
- **HTML parsing:** Jsoup
- **Logging:** SLF4J + Log4j
- **Testing:** JUnit 5 + Mockito

## Project Structure

```text
src/main/java/dev/harimohan/app/toondownload/
  CartoonDownloader.java                 # Entry point
  config/BaseConfig.java                 # Bean/config setup
  model/EpisodeStatus.java               # Episode state enum
  util/CloseUtil.java, util/TaskUtil.java
  worker/DownloadManager.java            # Orchestration
  worker/PageWalker.java                 # Episode discovery
  worker/VideoFinder.java                # Video URL extraction
  worker/DownloadWorker.java             # File download
  worker/EpisodeTracker.java             # Track downloaded/ignored episodes

src/main/resources/
  application.properties
  log4j.properties

src/test/java/dev/harimohan/app/toondownload/
  ... unit tests for config/util/worker flow
```

## Prerequisites

- Java 21 installed and available on `PATH`
- No separate app server required
- Internet access (for live target-site scraping/downloading)

## Setup

1. Clone repository:
   ```bash
   git clone https://github.com/harimm/download-cartoons-online.git
   cd download-cartoons-online
   ```
2. Edit runtime config:
   - `src/main/resources/application.properties`
3. Make sure your target directories exist or are creatable by the process.

## Configuration

All runtime configuration is externalized in:

- `src/main/resources/application.properties` (main runtime)
- `src/test/resources/application.properties` (test defaults)

### Required Keys

| Key | Required | Default in repo | Format / Example | Description |
|---|---|---|---|---|
| `toon_download.base.url` | Yes | `https://www16.watchcartoonsonline.eu/` | Absolute URL ending with `/` | Base website URL used by `PageWalker`. |
| `toon_download.cartoons_to_download` | Yes | `jackie-chan,captain-planet,tom-and-jerry-classic` | Comma-separated cartoon IDs | Ordered list of cartoons to process. |
| `toon_download.download.pool.size` | Yes | `10` | Integer (`>=1`) | Thread pool size used for scheduling downloads. |
| `toon_download.download.wait_until_series_complete` | Yes | `true` | `true` / `false` | If `true`, waits for current series tasks before moving to next. |

### Per-Cartoon Keys

For each value in `toon_download.cartoons_to_download`, define the keys below:

| Key Pattern | Required | Default | Format / Example | Description |
|---|---|---|---|---|
| `toon_download.download.<cartoon>.url` | Yes (per listed cartoon) | None | `jackie-chan-adventures-2000-2005-full-episodes/` | Cartoon-relative path appended to `toon_download.base.url`. |
| `toon_download.download.<cartoon>.target.path.root` | Yes (per listed cartoon) | None | `/data/cartoons/jackie-chan/` or `D:/media/cartoons/jackie-chan/` | Absolute folder where episodes + tracking files are stored. |

### Tracking Files

Inside each configured `<target.path.root>`, the app maintains:

- `episodeList` — one downloaded episode name per line.
- `ignoreList` — one episode name per line that should be skipped.

### Example Configuration

```properties
toon_download.base.url=https://www16.watchcartoonsonline.eu/
toon_download.cartoons_to_download=jackie-chan
toon_download.download.jackie-chan.url=jackie-chan-adventures-2000-2005-full-episodes/
toon_download.download.jackie-chan.target.path.root=/absolute/path/to/cartoons/jackie-chan/
toon_download.download.pool.size=4
toon_download.download.wait_until_series_complete=true
```

## Run the Application

### Using Gradle

Windows:
```bash
gradlew.bat clean build
gradlew.bat run
```

Linux/macOS:
```bash
./gradlew clean build
./gradlew run
```

> If no `run` task is configured in your local Gradle setup, run using `java` as shown below.

### Run with Java

```bash
gradlew.bat clean build
java -cp build/libs/* dev.harimohan.app.toondownload.CartoonDownloader
```

## Testing

Run all tests:

Windows:
```bash
gradlew.bat test
```

Linux/macOS:
```bash
./gradlew test
```

Test coverage includes:
- configuration/bootstrap behavior,
- episode discovery and parsing behavior,
- video URL resolution (primary and fallback strategies),
- download workflow and tracker persistence behavior.

## Troubleshooting

- **No episodes found / parsing failures**
  - The target website likely changed markup/selectors. Update `PageWalker`/`VideoFinder` extraction logic.
- **Missing property errors**
  - Verify all required keys exist for each configured cartoon ID.
- **Files not saved**
  - Check folder permissions and ensure absolute paths are valid on your machine.
- **App appears to skip episodes**
  - Review `episodeList` and `ignoreList` in target folders.

## Contributing

- Keep package namespace under `dev.harimohan.app.toondownload`.
- Prefer focused, minimal changes.
- Preserve existing config key formats for backward compatibility.
- Add/update tests under `src/test/java` for behavior changes.
- Update this README for any configuration or behavior changes.

## License

Licensed under the terms in [LICENSE](./LICENSE).
