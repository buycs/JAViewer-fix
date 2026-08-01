# JAViewer-fix

> 质感设计 · 更优雅的驾车体验

基于 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 二次开发的 Android 电影元数据浏览器。在保留原有浏览、搜索、播放等核心功能的基础上，进行了大量修复与功能增强，并完成新版本 Android 系统的兼容性适配。

- 原项目源码: https://github.com/SplashCodes/JAViewer
- 本项目源码: https://github.com/buycs/JAViewer-fix

---

## 功能特性

### 核心浏览
- 多数据源（骑兵 / 步兵 / 欧美），启动时从 `assets/properties.json` 加载
- 侧边栏一键切换数据源，支持编辑数据源域名（保存后自动重建网络层并重启）
- 旧域名（legacies）自动映射到当前活跃域名
- 首页 / 热门 / 已发布 电影列表，无限滚动分页 + 下拉刷新
- 女优列表（封面 Palette 取色）
- 分类标签页（保留日文原文，加载失败自动重试）

### 影片详情
- 详情元数据（发行日期 / 时长 / 导演 / 制作商 / 发行商 / 系列）
- 点击"影片番号" Header → 跳转磁力搜索页
- 截图缩略图 → 全屏画廊（ViewPager + PinchImageView 手势缩放）
- 相关影片推荐
- 长按收藏 / 取消收藏电影、女优

### 搜索
- 顶部搜索栏（SimpleSearchView），关键词搜索跳转列表页
- 收藏夹（电影 / 女优 双 Tab，底部导航）

### 磁力搜索（DownloadActivity 三 Tab）
- **BtSearch** — JSON API + MD5 签名（x-timestamp / x-nonce / x-sign）
- **无极磁链** — cili.info HTML 解析
- **btsow** — POST JSON API（OkHttp 直连）
- 文件列表展开 / 收起，点击整行复制磁力链接 / 打开外部下载器

### 视频播放
- 在线视频源搜索（PSVS API）+ MD5 签名生成播放地址
- 预览视频播放
- 全屏播放器（JZVideoPlayer + ExoPlayer 内核）

### 系统 / 兼容性
- 防截图（SecureActivity，FLAG_SECURE）
- 崩溃友好处理（CustomActivityOnCrash）
- 存储权限适配（Android M~Q 申请权限，配置文件迁移至应用外部存储目录）
- 全 ABI 兼容（无 native 代码）
- 侧边栏源码入口（原项目 / 本项目）

---

## 技术栈

| 项 | 值 |
|----|-----|
| AGP / Gradle | 8.9.0 / 8.13 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 21 |
| Java 版本 | 17 |
| 版本名 / 版本号 | 2.0.1 / 18 |
| 架构 | 传统 Activity/Fragment，无 DI、无 MVVM |
| 网络 | Retrofit 2.11 / OkHttp / Gson / Jsoup |
| 播放 | ExoPlayer 2.19 + JiaoZiVideoPlayer |
| 图片 | Glide 4.16 |

### 数据源配置

数据源定义于 `app/src/main/assets/properties.json`：

```json
{
  "data_sources": [
    {
      "name": "骑兵",
      "domain": "https://avmoo.shop",
      "apiPath": "/jav/data/api/",
      "legacies": ["javzoo.com", "avmoo.xyz", "..."]
    },
    { "name": "步兵", "domain": "https://avsox.click", "apiPath": "/javu/data/api/", "legacies": ["..."] },
    { "name": "欧美", "domain": "https://avheat.shop", "apiPath": "/wav/data/api/", "legacies": [] }
  ]
}
```

---

## 环境要求

- JDK 17
- Android SDK（compileSdk 35）
- 支持 Android 5.0 (API 21) 及以上

## 构建

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug

# Release 包
gradlew assembleRelease

# 单元测试
gradlew testDebugUnitTest

# 清理
gradlew clean
```

构建产物位于 `app/build/outputs/apk/`。

> **注意**: Release 构建暂使用 debug 签名（`app/build.gradle`），便于直接安装测试。正式发布前请替换为正式签名。

## 安装

```bash
adb install -r app/build/outputs/apk/debug/JAViewer-fix-2.0.1-debug.apk
```

---

## 未实现功能（规划 / 遗留）

> 以下为代码中已预留但**未完成接线**或**已被移除 / 废弃**的功能，供后续开发参考。

### 已预留但未实现
| 功能 | 现状 |
|------|------|
| 广告展示 | `Configurations.show_ads` 字段存在，`DownloadFragment` 中广告代码被注释，未启用 |
| 下载计数 | `Configurations.download_counter` 字段存在，未在任何 UI 中展示 |
| 应用更新检查 | `properties.json` 含 `latest_version` / `latest_version_code` / `changelog`，但未实现更新提示逻辑 |
| 搜索建议 | `SearchAdapter` / `SimpleSearchView.setSuggestions()` 已实现，但主界面未接线 |
| 语音搜索 | `SimpleSearchView` 支持语音按钮，但 `allowVoiceSearch` 默认关闭 |

### 已废弃 / 死代码（不建议使用）
| 功能 | 现状 |
|------|------|
| Avgle API | `Avgle.java` 仅被注释代码引用，已被 PSVS 替代 |
| WebViewActivity | 验证码 / 嵌入式播放页，仅剩注释引用，未接入任何流程 |
| BTSO / TorrentKitty | 旧下载链接 API，已移除（v2.0.1） |
| MagnetSearchActivity | 已被 `MagnetSearchFragment` 替代，已移除 |

### 已知技术债务
- 所有 UI 字符串硬编码中文，未使用 `strings.xml`
- `MagnetLink.create()` 用 `indexOf("&")` 截断可能抛异常
- `MovieActivity.getScreenBitmap()` 可能 OOM
- `FavouriteActivity.mAdapter` / `FavouriteTabsFragment.mAdapter` 静态引用可能泄漏

---

## 项目结构

```
app/src/main/java/io/github/javiewer/
├── activity/          # Activities（启动、主页、详情、画廊、下载、WebView、收藏夹）
├── fragment/          # Fragments（首页、热门、已发布、女优、类别、磁力搜索等）
├── adapter/           # RecyclerView 适配器 + 数据模型（adapter/item/）
├── network/           # Retrofit 接口（BasicService、BtSearch、CiliInfo、PSVS）与 JSON 提供者
├── view/              # 自定义视图（SimpleSearchView、PinchImageView 等）与滚动监听器
├── util/              # IO 与视频播放器工具
├── JAViewer.java      # Application 单例，持有全局状态
├── Configurations.java # 持久化用户配置（收藏、数据源）
└── Properties.java    # 数据源配置解析
```

## 版本历史

- **v2.0.1** — 新增 btsow 磁力搜索 Tab；番号点击统一跳转下载页；兼容性优化（全 ABI、存储权限适配）；修复已知问题
- **v2.1.1（历史版本号）** — 内部迭代期间使用，现已统一为 2.0.1

---

## 免责声明

本项目仅供技术学习与交流使用。应用内所有内容均来自互联网公开数据源，与项目本身无关。请遵守当地法律法规，勿用于商业用途或传播不当内容。

## License

本项目为 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 的二次开发，请遵循原项目开源协议。
