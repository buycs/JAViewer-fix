# JAViewer-fix

> 质感设计 · 更优雅的驾车体验

基于 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 二次开发的 Android 电影元数据浏览器。在保留原有浏览、搜索、磁力下载等核心功能的基础上，进行了大量修复与功能增强，并完成新版本 Android 系统的兼容性适配。

- 原项目源码: https://github.com/SplashCodes/JAViewer
- 本项目源码: https://github.com/buycs/JAViewer-fix

---

## 功能特性

### 核心浏览
- 多数据源，启动时从 `assets/properties.json` 加载
- 侧边栏一键切换数据源，支持编辑数据源域名（保存后自动重建网络层并重启）
- 旧域名（legacies）自动映射到当前活跃域名
- 首页 / 热门 / 已发布 电影列表，无限滚动分页 + 下拉刷新
- 演员列表（封面 Palette 取色）
- 分类标签页（保留日文原文，加载失败自动重试）

### 影片详情
- 详情元数据（发行日期 / 时长 / 导演 / 制作商 / 发行商 / 系列）
- 点击"影片编号" Header → 跳转磁力搜索页
- 截图缩略图 → 全屏画廊（ViewPager + PinchImageView 手势缩放）
- 相关影片推荐
- 长按收藏 / 取消收藏电影、演员

### 搜索
- 顶部搜索栏（SimpleSearchView），关键词搜索跳转列表页
- 收藏夹（电影 / 演员 双 Tab，底部导航）

### 磁力搜索（下载页多 Tab）
- 多磁力搜索源，自动切换可用源
- 文件列表展开 / 收起，点击整行复制磁力链接 / 打开外部下载器

### 视频播放（未实现）
- 播放 / 预览按钮已布局，但尚未接线可用视频源
- 原有在线视频源 API 已失效，功能待后续实现

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
      "name": "数据源一",
      "domain": "https://example.com",
      "apiPath": "/api/",
      "legacies": ["legacy1.com", "legacy2.com", "..."]
    },
    { "name": "数据源二", "domain": "https://example2.com", "apiPath": "/api2/", "legacies": ["..."] },
    { "name": "数据源三", "domain": "https://example3.com", "apiPath": "/api3/", "legacies": [] }
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

## 项目结构

```
app/src/main/java/io/github/javiewer/
├── activity/          # Activities（启动、主页、详情、画廊、下载、WebView、收藏夹）
├── fragment/          # Fragments（首页、热门、已发布、演员、类别、磁力搜索等）
├── adapter/           # RecyclerView 适配器 + 数据模型（adapter/item/）
├── network/           # Retrofit 接口与数据解析提供者
├── view/              # 自定义视图（SimpleSearchView、PinchImageView 等）与滚动监听器
├── util/              # IO 与视频播放器工具
├── JAViewer.java      # Application 单例，持有全局状态
├── Configurations.java # 持久化用户配置（收藏、数据源）
└── Properties.java    # 数据源配置解析
```

## 版本历史

- **v2.0.1** — 新增磁力搜索 Tab；编号点击统一跳转下载页；兼容性优化（全 ABI、存储权限适配）；修复已知问题

---

## 免责声明

本项目仅供技术学习与交流使用。应用内所有内容均来自互联网公开数据源，与项目本身无关。请遵守当地法律法规，勿用于商业用途或传播不当内容。

## License

本项目为 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 的二次开发，请遵循原项目开源协议。
