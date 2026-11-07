# AGENTS.md

Kanesumi(矩隅)—— Kanesumi Design Android UI 库,以直角丈量边缘。协作约定与 Claude Code 版
(`CLAUDE.md`,更详细)并存;本文档面向所有 agent,只留最高信号、不查就猜错的点。

## 术语约定

设计语言为 **Kanesumi Design**(正典:Ether monorepo 仓根 `KANESUMI_DESIGN.md`,取代「Metro Design / 改良 Metro」)。`Metro*` 标识符(MetroText/MetroTheme/MetroIndication…)是代码/组件名,**保留不重命名**;写「Kanesumi 风格 / Kanesumi 铁律」,不写「Metro 风格」;缓动族称「UWP 缓动」;「Metro 时代」仅作历史指称。

## Build

```bash
./gradlew :sample:assembleDebug        # 演示 app → sample/build/outputs/apk/debug/
./gradlew :kanesumi-core:assemble      # 单个模块 AAR
./gradlew assemble                     # 全量构建
```

- **没有测试套件**:任何模块都没有 `test`/`androidTest` 源目录,`./gradlew test` 跑不了东西。
  不要发明 lint/test 命令;CI 尚未定型(无 `.github/`)。
- **Gradle 守护进程跑在 JDK 21**(`gradle/gradle-daemon-jvm.properties`);Kotlin `jvmTarget=11`。
  本地 `java -version` 与编译目标不一致是正常的。
- 工具链:`gradle-9.3.1` wrapper、AGP 8.5.0、Kotlin 1.9.24、Compose BOM 2024.12.01、
  Compose compiler 1.5.14、compileSdk 36 / minSdk 24。
- 网络提示:本机访问 `services.gradle.org` 不稳定,首次构建若 gradle-9.3.1 分发下载超时,
  用腾讯镜像 `https://mirrors.cloud.tencent.com/gradle/gradle-9.3.1-bin.zip` 手动放入
  `~/.gradle/wrapper/dists/gradle-9.3.1-bin/<hash>/`(Windows 为 `%USERPROFILE%\.gradle\...`)。
- 修改依赖一律走 `gradle/libs.versions.toml` 版本目录,不在模块里散写版本号。

## Commit Convention

Conventional Commits + **小写 type 前缀 + 中文单句主题**(scope 可选,如 `feat(controls):`):

| 前缀 | 用途 |
|---|---|
| `feat:` | 用户可见新能力(组件/API 新增) |
| `fix:` | 修 bug,不引入新行为 |
| `chore:` | 删文件、改名、gitignore |
| `docs:` | 纯文档 |
| `build:` | 构建系统/依赖/版本 |
| `refactor:` / `perf:` / `style:` | 常规语义 |

Body 写 *why*,关键设计决策必须记录原因,让一年后读 git log 不需要开 PR。
**不要建 `*_log.md` 之类的平行日志文件** —— 全部历史在 git log。

## Versioning

唯一事实源:`gradle.properties` 的 `VERSION_NAME`(当前 `0.1.0-SNAPSHOT`)与 `GROUP`
(`io.github.takahashirinta`)。发布坐标(计划):`io.github.takahashirinta:kanesumi-*`,
模块内不硬编码版本号。设计决策与 ADR 见 `README.md` 的「为什么叫 Kanesumi / 设计理念 / 关键决策」。

## 模块与依赖方向

| 模块 | 依赖 | 职责 |
|---|---|---|
| `:kanesumi-core` | 仅 foundation/ui/runtime/ui-graphics | 层0 — `MetroInsets` 安全区、`MetroBottomStack` 叠层栈、`MetroTheme/Colors/Typography`、`MetroText/MetroIcon`、`MetroIndication` |
| `:kanesumi-anim` | 仅 foundation/ui/runtime | 层1 — UWP easing 全家族、`sokuouSpring` 桥接、`SokuouPresets/SokuouTweens`、`MetroFling` |
| `:kanesumi-controls` | api → core, anim | 控件(MetroButton/MetroSwitch/MetroDialog/MetroBottomSheet/…) |
| `:kanesumi-structure` | api → core, anim, controls | 结构元件(MetroShell/MetroAppBar/MetroBottomNav/MetroDetailScaffold/…) |
| `:sample` | 全部 | 演示 app |

**注意分层编号陷阱**:README 的模块表把 `structure` 编号为层2、`controls` 编号为层3,但
`:kanesumi-structure` **依赖** `:kanesumi-controls`(脚手架的空态/错误态要按钮、surface)。
方向无环即可,勿按编号"从下往上"理解;严禁新增 controls→structure 反向边。
跨模块依赖用 `api(project(...))`,让下游(含 sample)只需 import 一个模块就能触达
`MetroText`/`MetroSurface` 等。

## 铁律

- **零 M3**:库模块不得依赖 `material3` / `material` / `material-icons-core`。**例外**:
  `:sample` 允许依赖 `material-icons-core:1.7.5`(只有 `ImageVector` 资产,无主题/组件)。
  新组件先确认 M3 等价物并说明"为什么不用它"。
- **GPU 零重组**:动画单一 `progress: Float`(`Animatable`)驱动,视觉只在
  `graphicsLayer { }` / `drawBehind` / draw phase 读取;**绝不在组合阶段用
  `animateFloatAsState`/`animateColorAsState`**(逐帧重组)。`MetroIndication`
  (Modifier.Node + DrawModifierNode)是参考实现。
- **用 `:kanesumi-anim` 的 Sokuou 预设,不散写 `tween(300, CubicBezierEasing(...))`**。
  Kanesumi Design 无弹簧/回弹/ripple/elevation 联动,受控减速(MetroCubic 等);颜色插值用
  "叠层 alpha"(如 `MetroBottomNav` 双 Icon 叠 Alpha),不用颜色动画。
- **贴边走层0**:`MetroShell` 把 bottomBar 作为悬浮 overlay 画在 content 之上;底部元件
  通过 `rememberBottomStackReservation(key, heightDp)` 登记进 `MetroBottomStack`,内容侧
  `bottomOverlayPadding()` 一句自适应留白。在 `MetroShell` 之外调用这些 helper 会按设计崩溃。
  安全区一律读 `MetroInsets`(`rememberMetroInsets()`),不用裸 `WindowInsets.*`;新 inset API
  必须收敛进 core/insets。
- **包在 `MetroTheme` 里**:注入 `LocalIndication = MetroIndication`,树内所有
  `.clickable {}` 免费获得直角闪切。不要显式传 indication 除非有意覆盖。
- **Kanesumi 外观**:无圆角、无阴影、无边框(除非信息需要),直角切一切;Cover/图片铺满不裁圆角。
- **默认即 Kanesumi Design**:新组件零配置即得 Kanesumi Design 外观;需要"调教"才能成型的 API 是设计缺陷。
- **a11y 不可妥协**:控件必须带 `semantics`(role/contentDescription);`MetroIndication`
  只负责视觉反馈。
- **键盘焦点**:`MetroIndication` 默认叠加 2dp 向内 primary 焦点框,焦点 / InputMode
  仅 draw 读取;Touch 下显式不画框,不能仅靠 clickable 不接收触摸焦点的假设。
  构造参数 `focusEnabled=true`、`focusColor=Color.Unspecified`(取当前主题 primary)
  追加在旧参数之后。自定义焦点目标与 indication 必须共享 InteractionSource;
  TextField 已显式连接。pointerInput-only 的 Switch / 歌词行不会自动变成可获焦控件。
- **小屏底栏**:`MetroBottomNav.labelMode` 默认 `MetroBottomNavLabelMode.Always`(旧行为),
  另有 SelectedOnly / Never;Never + `heightDp=48.dp` 只布局居中的 22dp 图标。
  SelectedOnly 标签用 QuickSwitch 叠 alpha,动画 / 测量 state 只在 graphicsLayer 读。
  现有指示条 / 颜色动画不变。sample 首页「焦点 / 小屏」可切模式 / 56、48dp 高度;
  完整焦点覆盖表与 API 默认值见 README「键盘焦点与小屏底栏」。
- **注释写 why 不写 what**;参数给出来源(如 SOKUOU_ENGINE.md 的典型值表),中英混合。

## 快速上手(按序读)

1. `kanesumi-core/.../insets/MetroInsets.kt` + `MetroBottomStack.kt` —— "护城河",解释
   `MetroShell` 为何长这样、底部叠层为何走 scope 而非 prop-drilling。
2. `kanesumi-core/.../theme/MetroTheme.kt` + `MetroIndication.kt` —— "默认即 Kanesumi Design"如何
   经 `LocalIndication` 落地;零重组 draw 模式的参照。
3. `kanesumi-anim/.../sokuou/Sokuou.kt` —— 动画词汇表,全部用这些名字。
4. `kanesumi-structure/.../MetroShell.kt` —— **没有 topBar 槽**:Kanesumi Design 约定"顶栏 = 滚动列表
   第一项",`MetroAppBar` 是 LazyColumn 里的 item,不是 Scaffold 槽。
5. `kanesumi-controls/` —— 每个 `Metro*` 文件头注明它取代的 M3/Ncrust 组件。

## 与 Ncrust 的关系

- **Ncrust 是独立 app 项目**(不在本仓库),通过 Gradle 组合构建
  `includeBuild` + `dependencySubstitution` 把 `io.github.takahashirinta:kanesumi-*` 映射到
  本仓库的 project。改公共 API 时注意 Ncrust 消费方式;未来发 Maven Central 后 Ncrust 只需
  删掉 includeBuild 块即可切换。
- **Sokuou 是独立的桌面端动画引擎**(Rust,仓库 `GuitaristRin/Sokuou`)。`:kanesumi-anim`
  是移植分支,与 `PezMax-One/src/sokuou` 保持同名 API 与参数惯例,上游改动时同步移植,
  不做私有重命名。
- 组件层完整度以 README 的映射表为准(`Text`→`MetroText`、`SongMenuSheet`→`MetroBottomSheet`
  等);新增组件前先查 Ncrust 是否已有需要映射的 Material 等价物。
- **播放器三层图形架构**是 app 独有形态,不进库 —— 那是 Ncrust 的领域,不是通用控件。
