# JAViewer-fix

> 质感设计 · 更优雅的驾车体验

基于 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 二次开发的 Android 电影元数据浏览器，修复原项目问题并适配新版 Android。

- 本项目源码: https://github.com/buycs/JAViewer-fix

## 功能

- **浏览** — 首页 / 热门 / 已发布列表，无限滚动 + 下拉刷新；多数据源切换；演员、分类浏览
- **详情** — 元数据、截图画廊（手势缩放）、相关推荐、长按收藏
- **搜索** — 关键词搜索（键盘搜索键或输入框右侧搜索按钮提交）、收藏夹（电影 / 演员）
- **磁力** — 多磁力源自动切换，一键复制链接 / 打开外部下载器
- **播放** — 入口已布局，视频源尚未接通（原 API 已失效）

## 技术栈

| 项 | 值 |
|----|-----|
| AGP / Gradle | 8.9.0 / 8.13 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 21 |
| Java | 17 |
| 版本 | 2.0.1 (18) |
| 架构 | Activity/Fragment，无 DI |
| 网络 / 播放 / 图片 | Retrofit + Jsoup / ExoPlayer + JiaoZiVideoPlayer / Glide |

数据源配置于 `app/src/main/assets/properties.json`。

## 构建

```bash
gradlew.bat assembleRelease   # Windows
./gradlew assembleRelease     # macOS / Linux
```

产物在 `app/build/outputs/apk/`。

> Release 暂用 debug 签名，便于直接安装；正式发布前请换正式签名。

## 环境

- JDK 17、Android SDK (compileSdk 35)
- Android 5.0 (API 21) 及以上

## 结构

```
app/src/main/java/io/github/javiewer/
├── activity/   fragment/   adapter/
├── network/    view/       util/
├── JAViewer.java      # Application
├── Configurations.java # 用户配置
└── Properties.java    # 数据源配置
```

## 免责声明

仅供技术学习与交流使用，内容均来自互联网公开数据源，请遵守当地法律法规。

## License

遵循原项目 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 开源协议。
