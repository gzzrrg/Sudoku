<p align="center">
  <a href="../README.md">🇨🇳 中文</a>
  &nbsp;|&nbsp;
  <a href="README_EN.md">🇬🇧 English</a>
</p>

<p align="center">
  <img alt="GitHub License" src="https://img.shields.io/github/license/gzzrrg/Sudoku?style=flat">
  <img alt="GitHub stars" src="https://img.shields.io/github/stars/gzzrrg/Sudoku?style=flat">
  <img alt="GitHub last commit" src="https://img.shields.io/github/last-commit/gzzrrg/Sudoku?style=flat">
  <img alt="Android API" src="https://img.shields.io/badge/Android-36%2B-green?style=flat&logo=android">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.2.10-purple?style=flat&logo=kotlin">
</p>

<p align="center">
  <img src="images/图标.png" alt="Sudoku Icon" width="140" />
</p>

<h1 align="center">Sudoku</h1>

<p align="center">
  <b>A modern, feature-rich Sudoku game for Android.</b><br>
  Built with Jetpack Compose + Material 3 · MVVM Architecture · Offline Ready
</p>

---

## Screenshots

<p align="center">
  <img src="images/主页效果.jpg" alt="Home Screen" width="30%" />
  <img src="images/选择数字效果.jpg" alt="Gameplay" width="30%" />
  <img src="images/设置页.jpg" alt="Settings" width="30%" />
</p>

## Features

### Core Gameplay

- **Three Difficulty Levels** — Easy (38–42 givens), Medium (28–32), and Hard (22–26). Each puzzle is guaranteed to have a unique solution.

- **Remote Puzzles + Offline Fallback** — Puzzles are fetched from the dosuku API by default. When the network is unavailable, the built-in backtracking generator seamlessly takes over — you can always play.

  <p align="center">
    <img src="images/api请求页.jpg" alt="API Request" width="60%" />
  </p>

- **Pencil Marks** — Tap the note toggle to enter pencil-mark mode. Each cell displays candidate numbers in a clean 3×3 sub-grid layout. The NumberPad highlights digits you've already noted, making it easy to track your candidates.

  <p align="center">
    <img src="images/笔记功能效果.jpg" alt="Pencil Marks" width="30%" />
  </p>

- **Undo / Redo** — Full operation history with unlimited undo and redo. Every action is tracked, including cell values and pencil marks. Rewind as far back as you need.

### Assistance & Feedback

- **Hint System** — 3 hints per game. Press the hint button and the currently selected empty cell is filled with the correct answer. A badge on the toolbar shows how many hints remain.

- **Conflict Detection** — Invalid placements are highlighted in real-time with a distinct error color. Toggle conflict warnings on or off in settings.

- **Error Limit** — The game ends after 3 cumulative mistakes (toggleable in settings). Turn it off for a relaxed experience.

  <p align="center">
    <img src="images/输入错误提示.jpg" alt="Error Feedback" width="30%" />
    <img src="images/失败页面效果.jpg" alt="Game Over" width="30%" />
  </p>

### Progress & Data

- **Auto Save** — Every move is automatically persisted to the local Room database. Close the app, switch tasks, or reboot your phone — your game is right where you left it.

- **Real-time Timer** — Tracks your solve time with second-level precision. Automatically pauses when you leave the game screen and resumes when you come back.

- **Statistics Dashboard** — Per-difficulty stats including games played, wins, win rate, error-free wins, win streak, best time, and average time. Canvas-drawn bar charts visualize your progress.

  <p align="center">
    <img src="images/数据统计页效果.jpg" alt="Statistics" width="40%" />
  </p>

### Visual Design

- **Three Complete Themes** — Every component adapts across all three themes. Switch anytime or follow your system preference.

  <p align="center">
    <img src="images/浅色主题.jpg" alt="Light (Green)" width="30%" />
    <img src="images/深色主题效果.jpg" alt="Dark" width="30%" />
    <img src="images/白色主题效果.jpg" alt="White" width="30%" />
  </p>

  | Theme | Background | Primary | Board BG | Grid Line | Selected | Best For |
  |-------|-----------|---------|----------|-----------|----------|----------|
  | Light (Green) | `#DFECD1` | `#8CB85C` | `#F7FAF2` | `#5D6A52` | `#8CB85C` | Daily use |
  | Dark | `#1C1C1E` | `#8DC563` | `#2C2C2E` | `#636366` | `#8DC563` | Night use |
  | White | `#FFFFFF` | `#8B7D6B` | `#F5F5F5` | `#999999` | `#E8E0D8` | Minimal look |

- **Responsive Layout** — Phones in portrait mode use a single-column layout. Rotate to landscape or use a tablet and the UI switches to a dual-pane layout (board 65% + controls 35%).

  <p align="center">
    <img src="images/横屏效果.jpg" alt="Landscape Gameplay" width="50%" />
    <img src="images/横屏笔记效果.jpg" alt="Landscape Notes" width="50%" />
  </p>

## Architecture

```
┌─────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)                 │
│  HomeScreen / GameScreen / SettingsScreen   │
│         ↕ StateFlow                         │
│  ViewModel (HomeVM / GameVM / ProfileVM)    │
│         ↕                                   │
│  Repository (SudokuRepository)              │
│    ↙        ↓         ↘                    │
│  Room     DataStore    Retrofit             │
│ (saves)  (prefs)      (remote API)          │
└─────────────────────────────────────────────┘
```

| Category | Technology |
|----------|------------|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose + Material 3 (BOM 2026.02.01) |
| Architecture | MVVM + Repository (manual DI via `AppContainer`) |
| Database | Room (game sessions + history) |
| Preferences | DataStore (theme, sound, difficulty, stats) |
| Networking | Retrofit 2 + Gson |
| Navigation | Compose Navigation |
| Async | Kotlin Coroutines + StateFlow |
| Build System | Gradle 9.4.1 + AGP 9.2.1 + KSP |
| Min / Target SDK | Android 16 (API 36) |

```
com.zir.sudoku/
├── di/AppContainer.kt              # Manual DI container
├── data/
│   ├── local/                      # Room database, DAOs, entities, DataStore
│   ├── remote/                     # Retrofit API service
│   └── repository/                 # Unified data access + offline fallback
├── domain/
│   ├── model/                      # BoardState, Cell, Difficulty, Operation
│   └── engine/                     # SudokuGenerator, SudokuValidator
└── ui/
    ├── navigation/NavGraph.kt      # Route definitions
    ├── screen/
    │   ├── home/                   # Home screen + ViewModel
    │   ├── game/                   # Game screen + components
    │   ├── profile/                # Statistics dashboard
    │   └── settings/               # Settings screen
    └── theme/                      # Color palettes, Theme, Typography
```

## Build & Run

**Prerequisites:** Android Studio (latest stable), Android SDK 36, JDK 11+

```bash
git clone https://github.com/gzzrrg/Sudoku.git
cd Sudoku

./gradlew assembleDebug      # Debug build
./gradlew installDebug       # Install to connected device
./gradlew test               # Unit tests
./gradlew lint               # Lint checks
```

## License

MIT License © [@gzzrrg](https://github.com/gzzrrg)
