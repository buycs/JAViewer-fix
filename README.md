# JAViewer-fix

> 质感设计 · 更优雅的驾车体验

基于 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 二次开发的 Android 电影元数据浏览器，修复原项目问题并适配新版 Android。

- 本项目源码: https://github.com/buycs/JAViewer-fix

## 简介

本应用用于浏览公开数据源中的影片元数据（封面、详情、演员、分类、磁力链接等）。配置保存在应用专属目录。当前版本 **2.5.0 (21)**。

**本应用不提供应用内播放。**

## 功能

- **浏览** — 首页 / 热门 / 已发布列表，无限滚动 + 下拉刷新；多数据源切换；演员、分类浏览
- **详情** — 元数据、截图画廊（手势缩放）、相关推荐、长按收藏
- **搜索** — 关键词搜索（键盘搜索键或输入框右侧搜索按钮提交），支持搜索历史记录、复用与清空
- **收藏** — 电影 / 演员收藏；支持导入导出备份
- **磁力** — 多磁力源（BtSearch / 无极磁链 / btsow）；复制链接或用外部下载器打开
- **设置** — 数据源、检查更新、清除缓存、清空搜索历史、收藏导入导出、最近任务隐藏内容

明确不提供应用内播放；原播放入口已移除。

## 技术栈

| 项 | 值 |
|----|-----|
| AGP / Gradle | 8.9.0 / 8.13 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 21 |
| Java | 17 |
| 版本 | 2.5.0 (21) |
| 架构 | Activity/Fragment，无 DI |
| 网络 / 图片 | Retrofit + OkHttp + Jsoup / Glide |

数据源配置于 `app/src/main/assets/properties.json`。

## 构建

环境：JDK 17、Android SDK（compileSdk 35）。

```bash
gradlew.bat assembleRelease   # Windows（需 JDK 17）
./gradlew assembleRelease     # macOS / Linux
```

也可同时打 debug / release：

```bash
gradlew.bat :app:assembleDebug :app:assembleRelease :app:testDebugUnitTest
```

产物路径：`app/build/outputs/apk/`。

- Release 暂用 debug 签名，便于直接安装；正式发布请换正式 keystore。
- `minifyEnabled` 仍为关闭。`app/proguard-rules.pro` 已预留 keep 规则，供将来开启混淆。

## 环境

- JDK 17、Android SDK (compileSdk 35)
- Android 5.0 (API 21) 及以上

## 结构

```
app/src/main/java/io/github/javiewer/
├── activity/   fragment/   adapter/
├── network/    view/       util/
├── JAViewer.java       # Application
├── Configurations.java # 用户配置（搜索历史、收藏、数据源）
├── Properties.java     # 数据源与版本信息
└── util/FavouriteBackup.java  # 收藏导入导出
```

## 隐私

- 配置与收藏保存在应用专属目录（`getExternalFilesDir`），不申请存储权限。
- `android:allowBackup="false"`，系统备份不会导出收藏等本地数据。

## 免责声明

仅供技术学习与交流使用，内容均来自互联网公开数据源，请遵守当地法律法规。

## License

遵循原项目 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 开源协议。
