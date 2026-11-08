<div align="center">

# Smart Netease

### 一款持续演进的网易云音乐 Android 客户端

**Kotlin · Jetpack Compose · Salt UI · Media3**

[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Version](https://img.shields.io/badge/version-1.4.2-20bf6b?style=flat-square)](https://github.com/smartzheng/smart-netease/releases)
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)](LICENSE)

[下载 APK](https://github.com/smartzheng/smart-netease/releases) · [项目来源与致谢](docs/PROJECT.md) · [问题反馈](https://github.com/smartzheng/smart-netease/issues)

</div>

> 本项目是网易云音乐的非官方第三方客户端，与网易公司及 Salt Player 无隶属关系。仅供学习和个人使用，请尊重版权并支持正版音乐。

## 项目说明

Smart Netease 从 [GuitaristRin/Ncrust](https://github.com/GuitaristRin/Ncrust) fork 而来。它保留了原项目的 Android 应用基础，并在此基础上持续重构和扩展；当前仓库只维护 Android App。项目经历了应用包名、界面设计、播放器交互、网络同步和工程文档等多方面的调整，不能视为原项目的原样发行版。

应用 ID：`com.smart.neteast`。详细来源、设计参考和第三方依赖致谢见[项目来源与致谢](docs/PROJECT.md)。

## 功能

- **音乐发现**：每日推荐、推荐歌单、新歌速递和私人 FM。
- **搜索与曲库**：搜索单曲、专辑和艺人；管理喜欢的歌曲、订阅专辑及歌单。
- **播放**：后台播放、系统媒体控制、播放队列、循环/随机/单曲等播放模式及多档音质选择。
- **播放器**：封面主色背景、沉浸式状态栏、歌词与翻译、播放进度拖动、队列管理和展开/收起过渡动画。
- **账号**：网页登录、二维码登录、收藏云端同步。
- **设备支持**：Android Auto / Android Automotive 媒体浏览；适配手机和平板布局。
- **本地化和主题**：多语言，浅色/深色模式及多种强调色。

具体可用能力受网易云账号权益、歌曲版权、网络和设备解码器影响。无缝播放等实验功能可能出现不稳定情况。

## 下载与安装

从 [GitHub Releases](https://github.com/smartzheng/smart-netease/releases) 下载最新 Android APK。首次安装时，Android 可能提示允许从当前来源安装应用。

## 从源码构建

### 环境

- Android Studio
- JDK 21（Gradle daemon toolchain）
- Android SDK Platform 36
- 最低支持 Android 7.0（API 24）

### 命令

```bash
git clone https://github.com/smartzheng/smart-netease.git
cd smart-netease
./gradlew :app:assembleDebug
```

PowerShell 使用仓库自带的 Gradle wrapper：

```powershell
git clone https://github.com/smartzheng/smart-netease.git
cd smart-netease
.\gradlew.bat :app:assembleDebug
```

Debug APK 输出到 `app/build/outputs/apk/debug/`。构建 Release APK：

```powershell
.\gradlew.bat :app:assembleRelease
```

Release 需要本机配置被 `.gitignore` 排除的 `keystore.properties`，不得将签名密钥或口令提交到仓库。宏基准测试位于 `benchmark/`。

## 工程结构

| 路径 | 内容 |
|---|---|
| `app/` | Kotlin Android 应用源码与测试 |
| `benchmark/` | Android Macrobenchmark |
| `spec/api/` | Android 网络接口与协议笔记 |
| `spec/fixtures/` | Android 单元测试使用的协议和行为测试数据 |
| `docs/` | Android 项目说明、开发记录和专项调查 |

应用使用 Jetpack Compose、[Salt UI](https://github.com/Moriafly/SaltUI)、AndroidX Media3、Retrofit、OkHttp 和 Coil。Gradle 依赖版本以 `app/build.gradle.kts` 为准。

## 来源、参考与致谢

- **[GuitaristRin/Ncrust](https://github.com/GuitaristRin/Ncrust)**：本项目的直接上游和 Android 应用基础。原项目采用 MIT License；本仓库保留其许可声明，并在此基础上继续修改。
- **[Moriafly/SaltUI](https://github.com/Moriafly/SaltUI)**：当前 Android 界面使用的 Compose UI 组件库，Apache-2.0。
- **[Moriafly/SaltPlayerSource](https://github.com/Moriafly/SaltPlayerSource)**：播放器布局、图标观感及展开/收起交互的设计参考。本项目参考其公开界面，不包含该仓库的播放器应用源码或专有资源。
- **[NeteaseCloudMusicApiEnhanced/api-enhanced](https://github.com/NeteaseCloudMusicApiEnhanced/api-enhanced)**：网易云接口行为与请求协议的调研参考，不是本 App 的运行时服务端依赖。
- 同时感谢 Jetpack Compose、AndroidX Media3、Retrofit、OkHttp、Coil 及本项目所用的其他开源库维护者。依赖及其许可信息以 Gradle 声明和各上游项目为准。

更多项目演进说明见 [`docs/PROJECT.md`](docs/PROJECT.md)。

## 许可

本仓库遵循根目录 [MIT License](LICENSE)。第三方依赖及其独立许可仍归各自项目所有。网易云音乐名称、商标、曲目和相关服务归其权利人所有。
