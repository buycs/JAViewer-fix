# JAViewer Android 项目完整设计文档

> 版本: v2.1.0 (versionCode 17)

## 1. 项目概况

| 配置项 | 值 |
|--------|-----|
| AGP / Gradle | 8.9.0 / 8.13 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 21 |
| Java 版本 | 17 |
| 应用 ID | `io.github.javiewer` |
| 版本名 / 版本号 | 2.1.0 / 17 |
| ProGuard | 关闭 |

### 核心依赖

| 分类 | 依赖 |
|------|------|
| UI | Material 1.12, materialdrawer 6.1.2, ahbottomnavigation 2.2.0, constraintlayout 2.2.0 |
| 网络 | Retrofit 2.11, Gson 2.11, Jsoup 1.18.3, OkHttp |
| 播放 | ExoPlayer 2.19.1, jiaozivideoplayer 6.2.12 |
| 工具 | Glide 4.16, dexter 6.2.2, recyclerview-animators 4.0.2 |

---

## 2. 架构设计

### 2.1 架构模式

传统 Activity/Fragment 架构，**无 DI 框架、无 MVVM**。全局状态存储在 `JAViewer`（Application 类）的静态字段中。

### 2.2 全局状态 (JAViewer.java)

| 字段 | 类型 | 说明 |
|------|------|------|
| `HTTP_CLIENT` | OkHttpClient | UA 伪装 + CSRF Token 注入 |
| `SERVICE` | BasicService | Retrofit 实例，数据源切换时重建 |
| `CONFIGURATIONS` | Configurations | 用户配置（收藏、数据源） |
| `DATA_SOURCES` | List\<DataSource\> | 从 properties.json 加载 |
| `hostReplacements` | Map | 旧域名 → 新域名映射 |
| `csrfToken` | String | 异步获取，无同步机制 |
| `COOKIE_JAR` | CookieJar | 内存 Cookie 存储 |

### 2.3 Activity 继承关系

```
AppCompatActivity
├── StartActivity (启动入口)
└── SecureActivity (防截图)
    ├── MainActivity → 侧边抽屉 + 6 个 Fragment
    ├── MovieActivity → 电影详情 + 收藏 + 分享
    ├── MovieListActivity → 电影列表容器
    ├── GalleryActivity → 全屏图片画廊
    ├── DownloadActivity → 磁力搜索 (BtSearch + 无极磁链 双Tab)
    ├── FavouriteActivity → 收藏夹 (底部导航)
    ├── WebViewActivity → 内嵌 WebView
    └── MagnetSearchActivity → 磁力搜索 (btsow.pics)
```

### 2.4 Fragment 继承关系

```
RecyclerFragment<I, LM> (抽象基类: 分页 + 下拉刷新)
├── MovieFragment (无限滚动 + 动画)
│   ├── HomeFragment → 首页
│   ├── PopularFragment → 热门
│   ├── ReleasedFragment → 最新
│   └── MovieListFragment → 搜索/筛选
├── ActressesFragment → 女优列表
└── FavouriteFragment (收藏基类)
    ├── FavouriteMovieFragment → 收藏电影
    └── FavouriteActressFragment → 收藏女优

ExtendedAppBarFragment
├── GenreTabsFragment → 类型标签页
└── FavouriteTabsFragment → 收藏标签页
```

---

## 3. 网络层

### 3.1 Retrofit 接口 (BasicService)

所有端点 POST 到数据源 API，返回原始 `ResponseBody`，由 `AVMOProvider` 手动 JSON 解析。

| 端点 | 参数 | 说明 |
|------|------|------|
| `getMovies` | [type, limit, page] | 电影列表 |
| `getMovie` | [movieId, lang] | 电影详情 |
| `getStars` | [type, limit, page] | 女优列表 |
| `getGenres` | [lang] | 类型列表 |
| `getFilterMovies` | [filter, keyword, lang, limit, page] | 筛选 |
| `search` | [keyword, limit, page] | 搜索 |
| `getRelatedMovies` | [movieId, lang, limit] | 相关电影 |

### 3.2 磁力搜索 API

#### BtSearch (btsearch.love) — JSON API + 签名验证

| 端点 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/api/search` | GET | keyword, limit, offset, mode, time, sort, sort_type, size | 搜索结果 |
| `/api/torrent/{id}` | GET | id(path), keyword(query) | 详情+文件列表 |

**签名机制:**
- 独立 OkHttpClient (`BTSEARCH_CLIENT`) + 拦截器
- 请求头: `x-timestamp`, `x-nonce`, `x-sign`
- 签名算法: `MD5(排序后的参数 + &key=long2ice).toUpperCase()`

**搜索响应:**
```json
{
  "total": 10,
  "data": [
    {
      "id": 2419147587,
      "name": "MADB-004",
      "size": "6083314266",
      "hash": "969af3a5efd45076cb3aadd1a83fb54fd7d3b195",
      "created_at": "2026-07-25T21:05:20.240Z",
      "count": 2,
      "hot": 0
    }
  ]
}
```

**详情响应:**
```json
{
  "id": 2419147587,
  "name": "MADB-004",
  "size": "6083314266",
  "hash": "969af3a5efd45076cb3aadd1a83fb54fd7d3b195",
  "torrentfile": [
    {"id": 1012198768, "name": "file1.mp4", "size": "2001226"},
    {"id": 1012198770, "name": "file2.mp4", "size": "6081313040"}
  ]
}
```

#### CiliInfo (cili.info) — HTML 解析

| 端点 | 方法 | 参数 | 说明 |
|------|------|------|------|
| `/search?q=X` | GET | q | 搜索结果 HTML |
| `/{path}` (如 `/!lBfm`) | GET | path | 详情页 HTML |

- 无分页支持
- 搜索结果解析: `table.table-hover.file-list tbody tr`
- 详情页解析: `#input-magnet` (磁力链接) + `table.table-hover.file-list` (文件列表) + `dt:contains("发布日期")` (时间)

#### BTSO (btsow.pics) — POST JSON API

| 端点 | 方法 | Body | 说明 |
|------|------|------|------|
| `/bts/data/api/search` | POST | `[{"search":"code"}, 30, 1]` | 搜索 |
| `/bts/data/api/magnet` | POST | `["hash"]` | 文件列表 |

**搜索响应:**
```json
{
  "data": [
    {
      "hash": "6D8CD7F3...",
      "name": "MADB-004-C ...",
      "size": 8484889014,
      "lastUpdateTime": 1783840027
    }
  ]
}
```

### 3.3 第三方 API

| API | 用途 | 状态 |
|-----|------|------|
| Avgle | 视频搜索 | 使用中 |
| PSVS (rekonquer.com) | 预览视频 | 使用中 |
| BtSearch (btsearch.love) | 磁力搜索 (JSON API) | 使用中 |
| CiliInfo (cili.info) | 磁力搜索 (HTML) | 使用中 |
| BTSO (btsow.pics) | 磁力搜索 (MagnetSearchActivity) | 使用中 |
| TorrentKitty | 下载链接 | 已移除 |

### 3.4 OkHttp 拦截器

**JAViewer.HTTP_CLIENT (主客户端):**
1. 域名替换 (`hostReplacements`)
2. User-Agent 伪装 (Chrome 91.0)
3. `X-Requested-With: XMLHttpRequest`
4. CSRF Token 注入 (`X-CSRF-Token`)

**BtSearch.BTSEARCH_CLIENT (BtSearch 专用):**
1. 时间戳 + 随机 Nonce
2. MD5 签名生成
3. `Accept: application/json`
4. `Referer` 设置

---

## 4. 数据流

### 4.1 配置持久化

**存储位置:** `/sdcard/JAViewer/configurations.json`

```json
{
  "starred_movies": [...],
  "starred_actresses": [...],
  "data_source": { "name": "...", "domain": "...", "apiPath": "..." },
  "show_ads": false,
  "download_counter": 0
}
```

触发保存：数据源切换、域名编辑、收藏操作、下载计数。

### 4.2 网络请求流程

```
UI 触发 → newCall(page) → OkHttp 拦截器(域名替换/UA/CSRF)
→ Retrofit POST → AVMOProvider.parseXxx(json) → adapter.notifyItemRangeInserted()
```

### 4.3 磁力搜索数据流

```
DownloadActivity
├── BtSearch Tab
│   ├── 搜索: BtSearch.searchApi() → parseSearchResult() → DownloadLink 列表
│   ├── 点击三角: BtSearch.getDetail() → parseFilesFromJson() → 展开文件列表
│   └── 点击整行: 弹出磁力链接对话框
│
└── 无极磁链 Tab
    ├── 搜索: CiliInfo.search() → parseDownloadLinks() → DownloadLink 列表
    ├── 点击三角: CiliInfo.get() → parseMagnetLink() + parseFileList() + parseDate()
    └── 点击整行: 弹出磁力链接对话框

MagnetSearchActivity
├── 搜索: btsow.pics/api/search → TorrentGroup 列表
└── 文件列表: btsow.pics/api/magnet → 展开文件
```

### 4.4 数据源切换

```
RadioGroup 选择 → CONFIGURATIONS.setDataSource() → save()
→ recreateService() (重建 Retrofit) → restart() (重建 Activity)
```

### 4.5 收藏数据流

```
用户操作 → starred_movies.add/remove → CONFIGURATIONS.save()
→ FavouriteActivity.update() (通知所有收藏 Fragment 刷新)
```

---

## 5. UI 结构

### 5.1 主题继承

```
MaterialDrawerTheme.Light.DarkToolbar
└── AppTheme
    ├── AppTheme.NoActionBar
    │   └── AppTheme.NoActionBar.TransparentStatusBar
    ├── AppTheme.Start
    ├── AppTheme.AppBarOverlay
    └── AppTheme.PopupOverlay
```

### 5.2 自定义 View

| View | 说明 |
|------|------|
| `PinchImageView` | 手势图片控件（缩放、双击） |
| `SimpleSearchView` | 搜索视图（语音、建议） |
| `CompactCollapsingToolbarLayout` | 修复 insets 的折叠工具栏 |
| `SquareTopCrop` | Glide 顶部裁剪 |
| `ExoPlayerImpl` | ExoPlayer 媒体接口 |

### 5.3 Adapter 清单

| Adapter | 数据类型 | 用途 |
|---------|----------|------|
| `MovieAdapter` | Movie | 电影卡片 |
| `ActressAdapter` | Actress | 女优卡片 |
| `ActressPaletteAdapter` | Actress | 女优卡片 (Palette) |
| `GenreAdapter` | Genre | 类型标签 |
| `MovieHeaderAdapter` | Header | 元数据行 |
| `ScreenshotAdapter` | Screenshot | 截图网格 |
| `RelatedMovieAdapter` | Movie | 相关电影 |
| `DownloadLinkAdapter` | DownloadLink | 下载链接 + 文件列表展开/收起 |
| `MagnetFileAdapter` | TorrentGroup | 磁力文件 + 时间显示 |

### 5.4 DownloadActivity 布局

```
┌─────────────────────────────────────┐
│ 影片编号(加粗)              ▶/▼    │
│ 时间  大小                          │
├─────────────────────────────────────┤
│ 文件1                    1.91 MB   │
│ 文件2                    5.66 GB   │
└─────────────────────────────────────┘
```

- 标题加粗显示
- 第一行: 标题
- 第二行: 时间 + 大小
- 三角符号垂直居中右侧
- 文件列表在主内容下方展开

---

## 6. 完整文件清单

### 根包

| 文件 | 职责 |
|------|------|
| `JAViewer.java` | Application，全局单例、网络客户端、工具方法 |
| `Configurations.java` | 用户配置持久化 |
| `Properties.java` | properties.json 数据模型 |

### activity/

| 文件 | 职责 |
|------|------|
| `StartActivity.java` | 启动入口，权限检查、配置初始化 |
| `SecureActivity.java` | 防截图基类 |
| `MainActivity.java` | 主界面，侧边抽屉导航 |
| `MovieActivity.java` | 电影详情、收藏、分享 |
| `MovieListActivity.java` | 电影列表容器 |
| `GalleryActivity.java` | 全屏图片画廊 |
| `DownloadActivity.java` | 磁力搜索 (BtSearch + 无极磁链 双Tab) |
| `FavouriteActivity.java` | 收藏夹 |
| `WebViewActivity.java` | 内嵌 WebView |
| `MagnetSearchActivity.java` | 磁力搜索 (btsow.pics API) |

### fragment/

| 文件 | 职责 |
|------|------|
| `RecyclerFragment.java` | RecyclerView 抽象基类 |
| `MovieFragment.java` | 电影列表抽象基类 |
| `HomeFragment.java` | 首页 |
| `PopularFragment.java` | 热门 |
| `ReleasedFragment.java` | 最新 |
| `ActressesFragment.java` | 女优列表 |
| `MovieListFragment.java` | 搜索/筛选列表 |
| `ExtendedAppBarFragment.java` | 扩展 AppBar 基类 |
| `DownloadFragment.java` | 无极磁链下载列表 |
| `BtSearchFragment.java` | BtSearch 下载列表 |
| `genre/GenreTabsFragment.java` | 类型标签页 |
| `genre/GenreFragment.java` | 类型网格 |
| `favourite/FavouriteFragment.java` | 收藏基类 |
| `favourite/FavouriteTabsFragment.java` | 收藏标签页 |
| `favourite/FavouriteMovieFragment.java` | 收藏电影 |
| `favourite/FavouriteActressFragment.java` | 收藏女优 |

### adapter/

| 文件 | 职责 |
|------|------|
| `ItemAdapter.java` | 泛型基类 |
| `MovieAdapter.java` | 电影卡片 |
| `ActressAdapter.java` | 女优卡片 |
| `ActressPaletteAdapter.java` | 女优卡片 (Palette) |
| `GenreAdapter.java` | 类型标签 |
| `MovieHeaderAdapter.java` | 元数据行 |
| `ScreenshotAdapter.java` | 截图网格 |
| `RelatedMovieAdapter.java` | 相关电影 |
| `DownloadLinkAdapter.java` | 下载链接 + 文件列表展开/收起 (provider 感知) |
| `MagnetFileAdapter.java` | 磁力文件 + 时间显示 |
| `ViewPagerAdapter.java` | ViewPager 适配 |
| `NavigationSpinnerAdapter.java` | 下拉选择 |

### adapter/item/

| 文件 | 职责 |
|------|------|
| `Linkable.java` | 可链接实体基类 |
| `DataSource.java` | 数据源模型 |
| `Movie.java` | 电影模型 |
| `MovieDetail.java` | 电影详情模型 |
| `Actress.java` | 女优模型 |
| `Genre.java` | 类型模型 |
| `Screenshot.java` | 截图模型 |
| `DownloadLink.java` | 下载链接模型 (files, date, filesExpanded) |
| `MagnetLink.java` | 磁力链接模型 |
| `MagnetFile.java` | 磁力文件模型 |
| `TorrentGroup.java` | Torrent 分组模型 (date) |

### network/

| 文件 | 职责 |
|------|------|
| `BasicService.java` | Retrofit API 接口 |
| `Avgle.java` | Avgle API |
| `PSVS.java` | PSVS API |
| `BtSearch.java` | BtSearch API + 签名 (独立 OkHttpClient) |
| `CiliInfo.java` | cili.info API (Retrofit + ResponseBody) |
| `BTSO.java` | BTSO API (旧，保留) |
| `TorrentKitty.java` | TorrentKitty API (旧，保留) |

### network/provider/

| 文件 | 职责 |
|------|------|
| `AVMOProvider.java` | AVMO JSON 解析 |
| `DownloadLinkProvider.java` | 下载链接抽象基类 (parseFileList, parseDate) |
| `BtSearchLinkProvider.java` | BtSearch JSON 解析 + 文件列表 |
| `CiliInfoLinkProvider.java` | cili.info HTML 解析 + 文件列表 + 日期 |
| `BTSOLinkProvider.java` | BTSO HTML 解析 (旧) |
| `TorrentKittyLinkProvider.java` | TorrentKitty HTML 解析 (旧) |

### view/

| 文件 | 职责 |
|------|------|
| `PinchImageView.java` | 手势图片控件 |
| `SimpleSearchView.java` | 搜索视图 |
| `SearchAdapter.java` | 搜索建议适配 |
| `CompactCollapsingToolbarLayout.java` | 修复 insets |
| `SquareTopCrop.java` | Glide 裁剪 |
| `AnimationUtil.java` | 动画工具 |
| `ViewUtil.java` | 视图工具 |

### view/listener/

| 文件 | 职责 |
|------|------|
| `BasicOnScrollListener.java` | 分页加载基类 (canLoadMore 检查 isEnd) |
| `EndlessOnScrollListener.java` | 无限滚动 |
| `ActressClickListener.java` | 女优点击 |
| `ActressLongClickListener.java` | 女优长按 |

### view/decoration/

| 文件 | 职责 |
|------|------|
| `GridSpacingItemDecoration.java` | 网格间距 |
| `MovieItemDecoration.java` | 电影列表间距 |
| `ActressItemDecoration.java` | 女优列表间距 |
| `DownloadItemDecoration.java` | 下载列表间距 |

### util/

| 文件 | 职责 |
|------|------|
| `IOUtils.java` | IO 工具 |
| `ExoPlayerImpl.java` | ExoPlayer 媒体接口 |
| `SimpleVideoPlayer.java` | 自定义播放器 UI |

---

## 7. 技术债务

### 线程安全
- `csrfToken` 后台线程写入、主线程读取，无同步
- `SERVICE` 在 `recreateService()` 中被替换，可能与进行中的请求冲突
- `starred_movies/actresses` 列表多处修改，无同步

### 内存泄漏风险
- `FavouriteActivity.mAdapter` 静态引用
- `FavouriteTabsFragment.mAdapter` 静态引用
- `WebViewActivity.httpClient` 静态 OkHttpClient

### 已弃用 API
- `ProgressDialog`、`getColor(int)`、`getParcelable(String)`、`FragmentPagerAdapter`

### 硬编码
- **所有 UI 字符串硬编码中文**，未使用 `strings.xml`

### 静默异常处理
- `Configurations.load()`、`StartActivity` 多处、`AVMOProvider` 等空 catch 块

### 逻辑问题
- `MagnetLink.create()` 用 `indexOf("&")` 截断磁力链接可能抛异常
- `MovieActivity.getScreenBitmap()` 可能 OOM

### 仓库依赖
- `jcenter.bintray.com` 已关闭
- `maven.fabric.io` 已关闭

---

## 8. 版本历史

| 版本 | 变更 |
|------|------|
| v2.1.0 | 新增 cili.info 磁力搜索源、文件列表展开/收起、BtSearch 详情 API 修复、MagnetSearch 时间显示、UI 优化 |
| v2.0.3 | BtSearch 磁力搜索、UI 优化 |
