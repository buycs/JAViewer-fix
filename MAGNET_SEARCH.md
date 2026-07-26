# 磁力搜索功能汇总

## 一、入口

| 触发方式 | 目标 Activity | 关键参数 |
|----------|---------------|----------|
| 影片页 FAB 按钮 | `DownloadActivity` | `keyword = movie.getCode()` |
| 影片页点击番号 | `MagnetSearchActivity` | `code = detail.code` |

---

## 二、DownloadActivity（双 Tab）

### Tab 1: BtSearch

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `GET /api/search?keyword=X&limit=10&offset=N&...` | `BtSearch.search()` | JSON API，带签名验证 |
| 2 | `GET /api/torrent/{id}?keyword=X` | `BtSearch.getDetail()` | 点击三角时请求，返回torrentfile |
| 3 | 解析 JSON 中的 `torrentfile` 数组 | 内存处理 | 获取文件列表 |

- **Provider**: `BtSearchLinkProvider`
- **网络接口**: `BtSearch.java`（独立 OkHttpClient + MD5 签名）
- **搜索结果**: 标题 + 大小 + 时间 + 磁力链接（直接可用）
- **文件列表**: 需点击三角请求详情 API 获取

### Tab 2: 无极磁链

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `GET https://cili.info/search?q=X` | `CiliInfo.search()` | 返回 HTML |
| 2 | 解析 `table.table-hover.file-list tbody tr` | Jsoup | 提取标题、大小、详情链接 |
| 3 | `GET https://cili.info/!lBfm` | `CiliInfo.get()` | 点击三角时请求 |
| 4 | 解析 `#input-magnet` + 文件列表 | Jsoup | 提取磁力链接和文件 |

- **Provider**: `CiliInfoLinkProvider`
- **网络接口**: `CiliInfo.java`
- **搜索结果**: 标题 + 大小（时间为空，点击后回写）
- **文件列表**: 需点击三角请求详情页获取

---

## 三、MagnetSearchActivity

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `POST https://btsow.pics/bts/data/api/search` | `okhttp3.Request` | Body: `[{search:"code"},30,1]` |
| 2 | `POST https://btsow.pics/bts/data/api/magnet` | `okhttp3.Request` | Body: `["hash"]`，获取文件列表 |
| 3 | 构建 `TorrentGroup` | 内存处理 | 直接显示 |

- **无 Provider**，直接在 Activity 中用 okhttp3 同步请求
- **网络请求**: `JAViewer.HTTP_CLIENT`
- **时间显示**: 从 `lastUpdateTime`（Unix时间戳）转换为 `yyyy-MM-dd`

---

## 四、UI 交互

| 操作 | DownloadActivity | MagnetSearchActivity |
|------|------------------|----------------------|
| 点击整行 | 弹出磁力链接对话框 | 打开磁力链接 |
| 点击三角 ▶ | 请求详情 + 展开文件列表 | 展开/收起文件列表 |
| 长按 | - | 复制磁力链接 |

---

## 五、涉及文件清单

| 文件 | 状态 | 职责 |
|------|------|------|
| `network/BtSearch.java` | 修改 | BtSearch API + 签名 |
| `network/provider/BtSearchLinkProvider.java` | 修改 | BtSearch 解析（搜索+详情） |
| `network/CiliInfo.java` | 新建 | cili.info API |
| `network/provider/CiliInfoLinkProvider.java` | 修改 | cili.info HTML 解析 |
| `network/provider/DownloadLinkProvider.java` | 修改 | 注册 ciliinfo |
| `activity/DownloadActivity.java` | 修改 | 双 Tab + 空对话框修复 |
| `activity/MagnetSearchActivity.java` | 修改 | 添加时间显示 |
| `adapter/DownloadLinkAdapter.java` | 修改 | 文件列表展开/收起 |
| `adapter/MagnetFileAdapter.java` | 修改 | 布局匹配 + 时间显示 |
| `adapter/item/DownloadLink.java` | 修改 | 新增 files/date 字段 |
| `adapter/item/TorrentGroup.java` | 修改 | 新增 date 字段 |
| `fragment/DownloadFragment.java` | 修改 | 首次加载后 setEnd |
| `fragment/BtSearchFragment.java` | 修改 | 传递 keyword |
| `view/listener/BasicOnScrollListener.java` | 修改 | canLoadMore 检查 isEnd |
| `res/layout/layout_download.xml` | 修改 | 两行布局 + 三角符号 |
| `res/layout/card_magnet_file.xml` | 修改 | 匹配 DownloadActivity 样式 |

---

## 六、流程图

```
MovieActivity
    ├── FAB 点击 → DownloadActivity
    │   ├── Tab: BtSearch
    │   │   ├── 搜索: /api/search (JSON)
    │   │   └── 文件列表: /api/torrent/{id} (JSON, 点击三角)
    │   └── Tab: 无极磁链
    │       ├── 搜索: /search?q= (HTML)
    │       └── 文件列表: /!id (HTML, 点击三角)
    │
    └── 点击番号 → MagnetSearchActivity
                    ├── 搜索: btsow.pics/api/search
                    └── 文件列表: btsow.pics/api/magnet
```
