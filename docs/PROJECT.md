# 项目来源与致谢

## 项目沿革

Smart Netease 源自 [GuitaristRin/Ncrust](https://github.com/GuitaristRin/Ncrust)。本仓库沿用了该项目的 Android 应用代码基础，并在后续开发中持续重构网络、收藏同步、播放器、交互动画、主题和布局等部分。

目前只维护 Android App，应用 ID 为 `com.smart.neteast`。它是一个独立演进的 fork；不是原上游的原样打包，也不代表上游作者的立场。上游项目采用 MIT License，本仓库保留根目录许可文件与原作者版权声明。

## 开源项目参考

| 项目 | 使用或参考方式 |
|---|---|
| [GuitaristRin/Ncrust](https://github.com/GuitaristRin/Ncrust) | 直接上游；Android 应用的基础代码与早期功能来自此项目。 |
| [Moriafly/SaltUI](https://github.com/Moriafly/SaltUI) | Android App 当前使用的 Compose UI 组件库，依赖版本见 `app/build.gradle.kts`。其仓库采用 Apache-2.0 许可。 |
| [Moriafly/SaltPlayerSource](https://github.com/Moriafly/SaltPlayerSource) | 播放器界面布局、控制图标观感及展开/收起交互的设计参考。公开仓库用于问题反馈和官方 Android 包发布；本项目没有把它当作播放器源码库或复制其专有资源。 |
| [NeteaseCloudMusicApiEnhanced/api-enhanced](https://github.com/NeteaseCloudMusicApiEnhanced/api-enhanced) | 网易云接口行为、请求协议和调试思路的参考。Smart Netease 直接从 Android 客户端请求服务，不依赖该项目作为运行时服务端。 |

## 第三方组件

Android App 还使用 Jetpack Compose / AndroidX、Media3、Retrofit、OkHttp、Coil、ZXing 等开源组件。依赖版本由 Gradle 文件管理；各组件的名称、版权及许可证归相应上游项目所有。分发时应以生成的 APK 依赖清单和各上游许可为准。

## 商标与服务

Smart Netease 是非官方第三方客户端，不隶属于 NetEase 或 Moriafly。网易云音乐及 Salt Player 的名称、商标和服务归各自权利人所有。项目代码不包含网易云音乐曲库；播放可用性由官方服务、账号权限、歌曲版权、网络及设备能力决定。
