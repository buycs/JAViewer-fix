# JAViewer
质感设计 更优雅的驾车体验

## 项目背景

本项目是基于原项目 **JAViewer**（作者：hitokunys）的二次开发（fork）。

- 原项目源码: https://github.com/SplashCodes/JAViewer
- 本仓库地址: https://github.com/buycs/JAViewer-fix

在保留原有浏览、搜索、播放等核心功能的基础上，进行了修复与增强。

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

## 已实现功能

### 核心浏览
- 多数据源（骑兵 / 步兵 / 欧美），启动时从 `assets/properties.json` 加载
- 数据源切换、侧边栏编辑数据源域名（保存后自动重建网络层并重启）
- 旧域名（legacies）自动映射到当前活跃域名
- 首页 / 热门 / 最新 电影列表，无限滚动分页 + 下拉刷新
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

### 系统/兼容性
- 防截图（SecureActivity，FLAG_SECURE）
- 崩溃友好处理（CustomActivityOnCrash）
- 存储权限适配（M~Q 权限申请，配置文件迁移至外部存储）
- 全 ABI 兼容（无 native 代码）
- 修复已弃用 API（ProgressDialog / getColor / getParcelable / FragmentPagerAdapter）
- 侧边栏源码入口（原仓库 + 本仓库）

## 未实现功能（规划/遗留）

> 以下为代码中已预留但**未完成接线**或**已被移除/废弃**的功能，供后续开发参考。

### 已预留但未实现
| 功能 | 现状 |
|------|------|
| **广告展示** | `Configurations.show_ads` 字段存在，`DownloadFragment` 中广告代码被注释，未启用 |
| **下载计数** | `Configurations.download_counter` 字段存在，未在任何 UI 中展示 |
| **应用更新检查** | `properties.json` 含 `latest_version` / `latest_version_code` / `changelog`，但未实现更新提示逻辑 |
| **搜索建议** | `SearchAdapter` / `SimpleSearchView.setSuggestions()` 已实现，但主界面未接线 |
| **语音搜索** | `SimpleSearchView` 支持语音按钮，但 `allowVoiceSearch` 默认关闭 |

### 已废弃/死代码（不建议使用）
| 功能 | 现状 |
|------|------|
| **Avgle API** | `Avgle.java` 仅被注释代码引用，已被 PSVS 替代 |
| **WebViewActivity** | 验证码/嵌入式播放页，仅剩注释引用，未接入任何流程 |
| **BTSO / TorrentKitty** | 旧下载链接 API，已移除（v2.0.1） |
| **MagnetSearchActivity** | 已被 `MagnetSearchFragment` 替代，已移除 |

### 已知技术债务
- 所有 UI 字符串硬编码中文，未使用 `strings.xml`
- `MagnetLink.create()` 用 `indexOf("&")` 截断可能抛异常
- `MovieActivity.getScreenBitmap()` 可能 OOM
- `FavouriteActivity.mAdapter` / `FavouriteTabsFragment.mAdapter` 静态引用可能泄漏

## 构建

```bash
# Windows
gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

## 官方Telegram交流群
点击加入 [JAViewer 官方步行街](https://t.me/joinchat/Bp7eL0ehwp4WI-GxWxitQg) 【禁止车辆通行】
