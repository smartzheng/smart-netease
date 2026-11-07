# Kanesumi(矩隅)

一个 Kanesumi Design Android UI 库。以直角丈量边缘。

- 独立 repo,**零 material3 依赖** —— 只用 `androidx.compose.foundation` + `androidx.compose.ui`
- 目标:「默认即 Kanesumi Design」—— 不套主题,默认元件就是直角、无边框、信息优先的 Kanesumi Design
- GPU 零重组 —— 动画单一 `progress: Float` 驱动,视觉只在 `graphicsLayer` / `drawBehind` 内读取
- 命名坐标(计划):`io.github.takahashirinta:kanesumi-*`
- License:Apache-2.0

> Kanesumi Design 正典（跨平台唯一定义）见 Ether monorepo 仓根 `KANESUMI_DESIGN.md`；本文为 sec-a 扇区的落地说明。

## 为什么叫 Kanesumi(命名缘由)

| 字 | 读音 | 含义 | 与库的对应 |
|---|---|---|---|
| 矩 | かね(kane) | 矩尺,画出直角的工具 | "以直角丈量边缘" —— 几何身份 |
| 隅 | すみ(sumi) | 角落、边缘(同音"墨",日本墨的东方意境) | 直角、无边框、贴边 |

合意:**"以直角丈量边缘"** —— 直角 / 无边框 / 贴边,浓缩了库的整个几何身份。

为何不用 Sumikane(隅矩):`すみかね` 恰好是日语现成短语 **"住みかね"**(难以居住 / 犹豫不决),有负面歧义;且"隅(墨)"打头会先导向"颜色"而非"几何"。以"矩"立住几何,再谈墨色。

## 设计理念(Kanesumi Design)

三大优先级:

1. **直角(No Curves)** —— 无圆角、无曲线,一切由直角切出;
2. **无边框(Borderless)** —— 去装饰,信息优先,大量元件无缝贴边;
3. **扁平与克制(Flat & Controlled)** —— 非物理动画(UWP easing,无弹簧/回弹),受控减速。

Kanesumi Design 的「改良」在于:不是 Windows 8 磁贴式堆叠,而是**信息优先**的直角版式;无边框 + 贴边追求屏幕利用率;动画用 Sokuou 的受控减速,而非系统默认的物理弹性。

### 性能原则(GPU 零重组)

- 单一 `progress: Float` 驱动所有动画;
- 视觉属性一律在 `graphicsLayer { }` / `drawBehind` / draw phase 读取,**绝不在组合阶段用 `animateFloatAsState`**(逐帧重组);
- 类稳定性:`@Stable` / `@Immutable` 显式标注,避免重组作用域扩张;
- 库的 API **强制**此约定:不提供 state 驱动动画的高层组件,动画值只从 `Animatable` 进 `graphicsLayer`。

### 关键决策(ADR)

| 决策 | 结论 | 原因 |
|---|---|---|
| 技术路线 | Compose 之上、Material 之下 | 保留布局/文本/手势/a11y 能力,摆脱 M3 魔改,不必 Skia 自绘 |
| 动画策略 | 单 progress + graphicsLayer,零重组 | GPU 性能核心 |
| 贴边策略 | 统一 `MetroInsets` 抽象 | Android 几何分裂(挖孔/刘海/手势条),需一次解而非逐组件算 |
| 无障碍 | 层0 起自带语义树 | 不可妥协,`MetroIndication` 只负责视觉反馈 |
| 与 Sokuou 关系 | Sokuou 独立(桌面),本库为其 Kotlin 分支 | 各自独立演进,本库消费曲线不吞并引擎 |

## 模块

| 模块 | 职责 |
|---|---|
| `:kanesumi-core` | 层 0 —— MetroInsets 安全区抽象、MetroBottomStack 底部叠层栈、主题基座 (MetroTheme/MetroColors/MetroTypography)、MetroText/MetroIcon、MetroIndication 直角闪切 |
| `:kanesumi-anim` | 层 1 —— Sokuou 分支:UWP easing 全家族、sokuouSpring 桥接、SokuouPresets/SokuouTweens、MetroFlingBehavior |
| `:kanesumi-structure` | 层 2 —— MetroShell / MetroAppBar / MetroDetailScaffold / MetroTopScrim / MetroBottomNav |
| `:kanesumi-controls` | 层 3 —— MetroSurface / MetroButton / MetroListRow / MetroSwitch / MetroTabRow / MetroIconButton / MetroDivider / MetroProgressIndicator / MetroDialog / MetroDropdownMenu / MetroBottomSheet / MetroResponsiveContent / MetroTextField / MetroDrawer / MetroChatInputBar |
| `:sample` | 演示 app —— 覆盖所有组件的真实交互流 |

注意:`:kanesumi-structure` **依赖** `:kanesumi-controls`(脚手架的空态/错误态要按钮、surface),方向无环即可,与层编号无关。

## 现状

v0.1.0-SNAPSHOT —— **组件层第一轮完成 + 首屏 Ncrust 反向迁移落地**。

Kanesumi 已被 Ncrust 通过 Gradle 组合构建 (`includeBuild`) 实际消费,SearchScreen
作为首个迁移屏跑通:M3 `Text` / `Icon` / `DropdownMenu` / `DropdownMenuItem` /
`NcrustIconButton` / `NcrustProgressIndicator` / `NcrustTabRow` / `TextButton` /
`LocalTextStyle` 全部换成 Metro* 对应物,包含 MetroTheme 从 NcrustColors 派生的
主题桥接。基础设施验证通过 —— 后续屏是重复劳动,不是新的架构风险。

组件覆盖范围对齐 Ncrust 的全部 M3 组件依赖与手滚组件:
`Text`/`Icon` → `MetroText`/`MetroIcon`,`DropdownMenu` → `MetroDropdownMenu`,
`HorizontalDivider` → `MetroDivider`,`NcrustPivotNav` → `MetroBottomNav`,
`NcrustTabRow` → `MetroTabRow`,`NcrustIconButton` → `MetroIconButton`,
`NcrustProgressIndicator` → `MetroProgressIndicator`,`PlayAllDialog` → `MetroDialog`,
`SongMenuSheet` → `MetroBottomSheet`,`DetailScaffold`/`ResponsiveContent` →
`MetroDetailScaffold`/`MetroResponsiveContent`。

播放器三层图形架构(PlayerCard 手势联动)不在库内 —— 那是 app 独有形态,不是通用控件。

### v0.1 里 API 打磨点(反向迁移驱动的补丁)

- `MetroText` 补齐 `maxLines / overflow / softWrap / minLines` 参数透传 —— 之前
  只能靠 BasicText 兜底,阻断了任何需要单行截断的迁移场景。
- `MetroTypography` 补 lineHeight 到全部样式(消除中文长段落行距过松),并追加
  M3 命名尺度轴 (`headlineMedium` / `titleLarge` / `titleMedium` / `bodyLarge` /
  `bodyMedium` / `bodySmall`) 与语义轴 (`pageHeading` / `title` / `body` /
  `caption` / `label`) 并存 —— M3/Ncrust 代码迁进来可以一比一替换 style 引用。
- `MetroColors.onSurfaceMuted` → `onSurfaceVariant`,对齐 M3 / 社区通用词汇,
  减少迁移期 sed 摩擦。
- `MetroTabRow` 顶部指示条与文字之间从 0dp 改成 12dp 呼吸,对齐 UWP Pivot 手感。

## Ncrust 消费方式(组合构建)

Ncrust 侧 `settings.gradle.kts` 加:

```kotlin
includeBuild("../../projects/Kanesumi") {
    dependencySubstitution {
        substitute(module("io.github.takahashirinta:kanesumi-core"))
            .using(project(":kanesumi-core"))
        substitute(module("io.github.takahashirinta:kanesumi-anim"))
            .using(project(":kanesumi-anim"))
        substitute(module("io.github.takahashirinta:kanesumi-controls"))
            .using(project(":kanesumi-controls"))
        substitute(module("io.github.takahashirinta:kanesumi-structure"))
            .using(project(":kanesumi-structure"))
    }
}
```

`app/build.gradle.kts` 加:

```kotlin
implementation("io.github.takahashirinta:kanesumi-controls:0.1.0-SNAPSHOT")
implementation("io.github.takahashirinta:kanesumi-structure:0.1.0-SNAPSHOT")
```

Kanesumi 侧无需 `maven-publish` —— 显式 `dependencySubstitution` 让 Gradle 直接
把符号坐标映射到 includeBuild 的项目。未来发到 Maven Central 时,Ncrust 只需
删掉整个 `includeBuild` 块,`implementation(...)` 的坐标一字不改就能切。

## 使用方法

```kotlin
MetroTheme {
    MetroShell(bottomBar = { MetroBottomNav(items = ..., selectedIndex = ..., onSelected = ...) }) {
        LazyColumn(
            contentPadding = bottomOverlayPadding(),  // 自动避开底部叠层
        ) {
            item { MetroAppBar(title = "Home") }
            item { MetroButton(text = "Action", onClick = { ... }) }
        }
    }
}
```

- 所有 `.clickable {}` 默认走 MetroIndication 直角闪切(由 MetroTheme 注入 LocalIndication),无需显式传 indication
- 结构元件(Nav / mini bar)通过 `rememberBottomStackReservation` 自动登记进底部叠层栈,内容侧 `bottomOverlayPadding()` 一句避开
- 动画一律用 `:kanesumi-anim` 的 Sokuou 预设,不散写 `tween(300, CubicBezierEasing(...))`

### 键盘焦点与小屏底栏

`MetroIndication` 在键盘 / 方向键模式下叠加 **2dp、向内、直角、primary 色**的焦点框。
焦点切换无动画;先画内容和原有按压闪切,再画焦点框,按住确认键时两者都保留。
焦点 snapshot state 与输入模式只在 draw phase 读取,不带来组合期状态订阅。
Compose clickable 内置 non-touch canFocus,但不覆盖 BasicTextField、自定义 focusable 或外层
focusProperties 覆盖;即使实际有焦点,`InputMode.Touch` 也显式屏蔽键盘焦点框。
它不创建 focus target,自定义控件必须让焦点目标与 indication 使用同一个 InteractionSource。

新增 API (均追加在原参数之后,旧的 Kotlin 位置 / 命名调用保持兼容):

| API | 默认值 | 含义 |
|---|---|---|
| `MetroIndication.focusEnabled: Boolean` | `true` | 关闭时只保留旧按压反馈,供自行绘制焦点的控件使用 |
| `MetroIndication.focusColor: Color` | `Color.Unspecified` | 未指定时在 draw 中取所在 `LocalMetroColors.primary` |
| `MetroBottomNav.labelMode: MetroBottomNavLabelMode` | `Always` | `Always` 图标 + 标签(旧默认);`SelectedOnly` 仅选中项标签可见;`Never` 只布局图标 |

`SelectedOnly` 用 `SokuouTweens.QuickSwitch` 淡入 / 淡出标签,图标随之回到 / 离开垂直中心;
颜色仍用原双 Icon / Text 叠层 alpha,顶部指示条仍为原 200ms MetroCubic。
隐藏标签不重复进入语义树,tab 始终保留 label 名称、Tab role 和 selected 状态。
左右键移焦(含 RTL),确认键才选择。`heightDp` 仍默认 56dp;小屏可用:

```kotlin
MetroBottomNav(
    items = items,
    selectedIndex = selectedIndex,
    onSelected = onSelected,
    heightDp = 48.dp,
    labelMode = MetroBottomNavLabelMode.Never,
)
```

Never 不测量 / 布局文字和间距,22dp 图标在 48dp 高度居中(上下各 13dp),顶部 2dp 指示条不占图标空间。

默认 `MetroTheme` 中的键盘焦点覆盖情况(调用方额外的 Modifier / LocalIndication 覆盖可能改变行为):

| 控件 / 条件 | 能获键盘焦点 | 默认画焦点框 | 原因 |
|---|---|---|---|
| 启用的 `MetroButton`、`MetroIconButton`、`MetroDropdownMenuItem` | 是 | 是 | clickable 自动发 FocusInteraction 给 LocalIndication |
| 有 onClick 的 `MetroListRow`、`MetroSurface` | 是 | 是 | 同上;无 onClick 则没有焦点目标 |
| `MetroTabRow`、`MetroBottomNav`、`MetroSidebar` 的项;`MetroSelectorFlyout` 的选项 | 是 | 是 | 每个项各有 clickable / 共享交互源 |
| 启用的 `MetroTextField`(含 readOnly) | 是 | 是 | BasicTextField 与外容器 indication 显式共用 interactionSource |
| `MetroChatInputBar` | 输入框是;发送按钮仅 active 时是 | 对应子控件是 | 复用 TextField / Button / IconButton |
| 禁用按钮 / 菜单项 / 文本框;无 onClick 的行 / Surface | 否 | 否 | 未创建或禁用了交互焦点目标 |
| `MetroTopScrim` 的返回按钮(含 `MetroDetailScaffold` 默认返回按钮) | 是 | 否 | 旧默认 `indication = null`;直接用 TopScrim 可传 `LocalIndication.current` 开启 |
| `MetroDrawer` / `MetroBottomSheet` 的背景遮罩 | 是 | 否 | 原 clickable 仍在,但显式 `indication = null`;不把无视觉误写成不可获焦 |
| `MetroSwitch`、`MetroLyricsPanel` 的歌词行 | 否 | 否 | 现有交互仅 pointerInput,没有 focusable / clickable,本次未改手势行为 |
| `MetroText` / `MetroIcon` / Divider / ProgressIndicator / 纯结构容器 | 自身否 | 自身否 | 没有交互目标;内部或调用方提供的可点击子项按其自身规则处理 |

sample: 首页顶部按钮 **「焦点 / 小屏」** → 可滚动演示页。纵向按钮与 12 个可点击列表行可遍历,
禁用按钮 / 只读行用于对照。标签模式按钮轮换三策略,高度按钮切换 56 / 48dp,
同时更新真实 shell 底栏和三种可操作的内联预览(内联预览 `autoReserveBottomStack = false`)。
页面提供返回首页按钮与 BackHandler;列表使用 bottomOverlayPadding,末行可滚出底栏覆盖区。

## 构建

```bash
./gradlew :sample:assembleDebug       # 演示 app
./gradlew :kanesumi-core:assemble     # 单个模块 AAR
./gradlew assemble                    # 全量构建
```

## License

[Apache License 2.0](LICENSE) —— © 2026 TakahashiRinta
