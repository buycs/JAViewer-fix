# JAViewer-fix

基于 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer) 二次开发的 Android 影片元数据浏览器（质感设计），修复原项目问题并适配新版 Android。

只浏览公开数据源的元数据（封面、详情、演员、分类、磁力链接），**不提供应用内播放**。功能：多数据源浏览与搜索（含历史记录）、影片 / 演员收藏与导入导出、截图画廊、多磁力源复制链接、主题与最近任务隐藏设置。

## 构建

JDK 17 + Android SDK（compileSdk 35），最低运行 Android 5.0 (API 21)。

```bash
./gradlew assembleRelease        # Windows 用 gradlew.bat
```

产物在 `app/build/outputs/apk/`。Release 暂用 debug 签名便于直接安装；数据源配置在 `app/src/main/assets/properties.json`。

## 免责声明

仅供技术学习与交流，内容均来自互联网公开数据源，请遵守当地法律法规。配置与收藏仅存于应用专属目录，不申请存储权限。

License 遵循原项目 [SplashCodes/JAViewer](https://github.com/SplashCodes/JAViewer)。
