[readme (1).md](https://github.com/user-attachments/files/34198048/readme.1.md)

# ThermalPulse ⚡🌡️

**ThermalPulse** — современное Android-приложение для мониторинга аппаратной телеметрии устройства (температурные зоны, троттлинг, загрузка и частоты) в реальном времени. Интерфейс построен на **Jetpack Compose** в строгой инженерной эстетике (Swiss typography + осциллограф реального времени).

---

## ✨ Основные возможности

* **Live Oscilloscope**: Анимированный холст (`OscilloscopeCanvas`) для визуализации динамики изменения температур и частотных всплесков без лишних рекомпозиций.
* **Швейцарская сетка телеметрии**: Информативные блоки (`SwissTelemetryBlocks`) с чёткой типографикой (Space Grotesk & JetBrains Mono), отображающие критические показатели чипсета и батареи.
* **Мониторинг троттлинга**: Отслеживание перехода устройства в состояние перегрева и троттлинга с оповещением об изменении статуса.
* **Энергоэффективный опрос**: Асинхронное получение данных телеметрии через Kotlin Coroutines & Flow с минимальным расходом батареи.

---

## 🛠️ Стек технологий

* **Язык**: Kotlin
* **UI**: Jetpack Compose (Material 3 + Custom Canvas)
* **Архитектура**: Clean Architecture + MVVM (`TelemetryViewModel`, StateFlow)
* **Шрифты**: JetBrains Mono, Space Grotesk
* **Сборка**: Gradle Kotlin DSL (`build.gradle.kts`) + Version Catalogs (`libs.versions.toml`)
* **Тестирование**: JUnit, Robolectric

---

## 📂 Структура проекта

```text
ThermalPulse/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── data/
│   │   │   │   │   ├── TelemetryModels.kt        # Доменные модели показателей
│   │   │   │   │   └── ThermalHardwareReader.kt  # Чтение сенсоров и системных узлов
│   │   │   │   ├── ui/
│   │   │   │   │   ├── TelemetryScreen.kt        # Основной экран телеметрии
│   │   │   │   │   ├── TelemetryViewModel.kt     # Управление состоянием экрана
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── OscilloscopeCanvas.kt # Отрисовка графиков на Canvas
│   │   │   │   │   │   └── SwissTelemetryBlocks.kt# Карточки показателей в строгом стиле
│   │   │   │   │   └── theme/                    # Цветовая палитра, типографика
│   │   │   │   └── MainActivity.kt
│   │   │   └── res/                              # Шрифты, иконки и ресурсы
│   │   └── test/                                 # Unit и Robolectric тесты
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml                        # Централизованные версии зависимостей
└── README.md
```

---

## ⚙️ Аппаратный доступ к данным

Чтение телеметрии реализовано в `ThermalHardwareReader`:
1. **Public Android API**: Использование `PowerManager.OnThermalStatusChangedListener` и `HardwarePropertiesManager` (для стандартных устройств без повышенных привилегий).
2. **Fallback / Direct Nodes**: Чтение зон `/sys/class/thermal/` и узлов SoC (актуально для кастомных сборок или устройств с правами суперпользователя).

---

## 🚀 Сборка и запуск

### Требования
* Android Studio Iguana / Jellyfish (или новее)
* JDK 17+
* Android SDK (API Level 26–34+)

### Сборка через терминал

```bash
# Клонирование репозитория
git clone https://github.com/llmchick/ThermalPulse.git
cd ThermalPulse

# Сборка Debug APK
./gradlew assembleDebug

# Запуск тестов
./gradlew testDebugUnitTest
```

Готовый APK-файл появится по пути:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Лицензия

Распространяется под лицензией MIT. Подробности см. в файле `LICENSE`.
