# 磁力搜索功能汇总

> 版本: v2.2.1 (versionCode 18)

## 一、入口

| 触发方式 | 目标 | 关键参数 |
|----------|------|----------|
| 影片页 FAB 按钮 (圆形下载图标) | `DownloadActivity` | `keyword = movie.getCode()` |
| 影片页点击"影片番号" Header 行 | `DownloadActivity` | `keyword = detail.code` |

> ⚠️ 旧入口"点击番号代码 TextView → MagnetSearchActivity"已移除，番号现在通过 `MovieDetail.Header` 列表统一展示。

---

## 二、DownloadActivity（三 Tab）

### Tab 1: BtSearch

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `GET /api/search` | `BtSearch.search()` | JSON API，带签名验证 |
| 2 | `GET /api/torrent/{id}` | `BtSearch.getDetail()` | 点击三角时请求，返回torrentfile |
| 3 | 解析 JSON 中的 `torrentfile` 数组 | 内存处理 | 获取文件列表 |

**请求参数:**
- search: `keyword`, `limit`, `offset`, `mode`, `time`, `sort`, `sort_type`, `size`
- detail: `id` (path), `keyword` (query)

**请求头:**
- `x-timestamp`: Unix 时间戳
- `x-nonce`: 随机字符串
- `x-sign`: MD5 签名

**搜索结果字段:**
```json
{
  "id": 2419147587,
  "name": "MADB-004",
  "size": "6083314266",
  "hash": "969af3a5efd45076cb3aadd1a83fb54fd7d3b195",
  "created_at": "2026-07-25T21:05:20.240Z",
  "count": 2,
  "hot": 0
}
```

**详情结果字段:**
```json
{
  "id": 2419147587,
  "name": "MADB-004",
  "size": "6083314266",
  "hash": "969af3a5efd45076cb3aadd1a83fb54fd7d3b195",
  "created_at": "2026-07-25T21:05:20.240Z",
  "torrentfile": [
    {"id": 1012198768, "name": "file1.mp4", "size": "2001226"},
    {"id": 1012198770, "name": "file2.mp4", "size": "6081313040"}
  ]
}
```

- **Provider**: `BtSearchLinkProvider`
- **网络接口**: `BtSearch.java`（独立 OkHttpClient + MD5 签名）
- **搜索结果**: 标题(加粗) + 时间 + 大小 + 磁力链接（直接可用）
- **文件列表**: 需点击三角请求详情 API 获取

### Tab 2: 无极磁链

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `GET https://cili.info/search?q=X` | `CiliInfo.search()` | 返回 HTML |
| 2 | 解析 `table.table-hover.file-list tbody tr` | Jsoup | 提取标题、大小、详情链接 |
| 3 | `GET https://cili.info/!lBfm` | `CiliInfo.get()` | 点击三角时请求 |
| 4 | 解析 `#input-magnet` + 文件列表 | Jsoup | 提取磁力链接和文件 |

**搜索页 HTML 结构:**
```html
<table class="table table-hover file-list">
  <tbody>
    <tr>
      <td><a href="/!lBfm">APKH-197</a></td>
      <td class="td-size">7.02GB</td>
    </tr>
  </tbody>
</table>
```

**详情页 HTML 结构:**
```html
<input id="input-magnet" value="magnet:?xt=urn:btih:..." />
<table class="table table-hover file-list">
  <tbody>
    <tr>
      <td>filename.mp4</td>
      <td class="td-size">5.66 GB</td>
    </tr>
  </tbody>
</table>
<dt>发布日期 :</dt><dd>2026-07-26 02:01:02</dd>
```

- **Provider**: `CiliInfoLinkProvider`
- **网络接口**: `CiliInfo.java`
- **搜索结果**: 标题(加粗) + 时间(空) + 大小
- **时间回写**: 点击三角或整行请求详情页后，时间为空则回写
- **文件列表**: 需点击三角请求详情页获取

### Tab 3: btsow (新增)

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `POST https://btsow.pics/bts/data/api/search` | `MagnetSearchFragment.searchMagnets()` | 搜索 JSON |
| 2 | `POST https://btsow.pics/bts/data/api/magnet` | 同上，每个 hash 单独请求 | 文件列表 |
| 3 | 构建 `TorrentGroup` | 内存处理 | 搜索时同时加载文件列表 |

**搜索请求 Body 格式:**
```json
[{"search": "MADB-004"}, 30, 1]
```

**搜索结果字段:**
```json
{
  "hash": "6D8CD7F3E8821906D4EB3E0759A59F581FB2A8F5",
  "name": "MADB-004-C ...",
  "size": 8484889014,
  "lastUpdateTime": 1783840027
}
```

**文件列表请求 Body 格式:**
```json
["6D8CD7F3E8821906D4EB3E0759A59F581FB2A8F5"]
```

**文件列表响应字段:**
```json
{
  "data": {
    "files": [
      {"filename": "file1.mp4", "size": 2001226},
      {"filename": "file2.mp4", "size": 6081313040}
    ]
  }
}
```

- **Fragment**: `MagnetSearchFragment` (独立 Fragment，非继承 RecyclerFragment)
- **Adapter**: `MagnetFileAdapter`
- **网络请求**: `JAViewer.HTTP_CLIENT` + `okhttp3.Request`（同步，后台线程）
- **时间显示**: `lastUpdateTime`(Unix时间戳) → `yyyy-MM-dd`
- **文件列表**: 搜索时**同时获取所有文件列表**（每个 hash 单独请求，使用 AtomicInteger 计数完成）
- **空结果处理**: 文件列表为空时，创建占位 MagnetFile（filename = torrentName）

**与 MagnetSearchActivity 的区别:**

| 对比项 | MagnetSearchFragment | MagnetSearchActivity (已移除) |
|--------|---------------------|----------------------|
| 宿主 | DownloadActivity Tab 3 | 独立 Activity |
| 入口 | FAB / 番号 Header | ⚠️ 已移除 |
| 数据加载 | 搜索时同时获取文件列表 | 搜索时同时获取文件列表 |
| 下拉刷新 | 支持 (SwipeRefreshLayout) | 支持 |
| 代码逻辑 | 基本相同 | 基本相同（重复代码） |

---

## 三、UI 交互

### DownloadActivity 三 Tab

| 操作 | BtSearch Tab | 无极磁链 Tab | btsow Tab |
|------|-------------|-------------|-----------|
| 点击整行 | 弹出磁力链接对话框 | 弹出磁力链接对话框 | 打开磁力链接 (ACTION_VIEW) |
| 点击三角 ▶ | 请求详情 + 展开文件列表 | 请求详情页 + 展开文件列表 | 展开/收起已加载的文件列表 |
| 长按 | - | - | 复制磁力链接到剪贴板 |

**DownloadActivity 布局 (每个 Tab 的列表项):**
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

## 四、涉及文件清单

### 网络层
| 文件 | 状态 | 职责 |
|------|------|------|
| `network/BtSearch.java` | 已修改 | BtSearch API + 签名 + 详情接口 |
| `network/provider/BtSearchLinkProvider.java` | 已修改 | BtSearch 解析（搜索+详情HTML） |
| `network/CiliInfo.java` | 新建 | cili.info API（search + get） |
| `network/provider/CiliInfoLinkProvider.java` | 新建 | cili.info HTML 解析 |
| `network/provider/DownloadLinkProvider.java` | 已修改 | 注册 ciliinfo provider |

### Activity / Fragment
| 文件 | 状态 | 职责 |
|------|------|------|
| `activity/DownloadActivity.java` | 已修改 | 三 Tab 布局 (BtSearch + 无极磁链 + btsow) |
| `activity/MovieActivity.java` | 已修改 | 番号改为 Header 列表传递，FAB 跳转 DownloadActivity，修复 SERVICE 空指针 |
| `JAViewer.java` | 已修改 | 新增 `getService()` 懒初始化，防止 SERVICE 为空 |
| `fragment/MagnetSearchFragment.java` | 新建 | btsow 磁力搜索列表 (212行) |
| `fragment/DownloadFragment.java` | 已修改 | 首次加载后 setEnd |
| `fragment/BtSearchFragment.java` | 已修改 | 传递 keyword 到 adapter |

### Adapter
| 文件 | 状态 | 职责 |
|------|------|------|
| `adapter/DownloadLinkAdapter.java` | 已修改 | 文件列表展开/收起 + 按provider区分请求 |
| `adapter/MagnetFileAdapter.java` | 已修改 | 布局匹配 + 时间显示 |
| `adapter/MovieHeaderAdapter.java` | 已修改 | 新增"影片番号"点击跳转 DownloadActivity |

### 数据模型
| 文件 | 状态 | 职责 |
|------|------|------|
| `adapter/item/DownloadLink.java` | 已修改 | 新增 files/date/filesExpanded 字段 |
| `adapter/item/TorrentGroup.java` | 已修改 | 新增 date 字段 |

### 布局
| 文件 | 状态 | 职责 |
|------|------|------|
| `res/layout/layout_download.xml` | 已修改 | 两行布局 + 三角符号 + 标题加粗 |
| `res/layout/card_magnet_file.xml` | 已修改 | 匹配 DownloadActivity 样式 |
| `res/layout/fragment_magnet_search.xml` | 新建 | MagnetSearchFragment 布局 (SwipeRefreshLayout + RecyclerView + ProgressBar + 空状态) |
| `res/layout/content_movie_headers.xml` | 已修改 | movie_code TextView 默认 visibility="gone" |

### 工具
| 文件 | 状态 | 职责 |
|------|------|------|
| `view/listener/BasicOnScrollListener.java` | 已修改 | canLoadMore 检查 isEnd |

---

## 五、流程图

```
MovieActivity
    │
    ├── FAB 点击 (圆形下载图标)
    │   └── → DownloadActivity (keyword = movie.getCode())
    │
    └── 点击"影片番号" Header 行
        └── → DownloadActivity (keyword = detail.code)

DownloadActivity (三 Tab)
    │
    ├── Tab 1: BtSearch
    │   ├── 搜索: GET /api/search (JSON, 签名验证)
    │   │   └── 返回: id, name, size, hash, created_at
    │   ├── 文件列表: GET /api/torrent/{id} (JSON, 点击三角)
    │   │   └── 返回: torrentfile[] (name, size)
    │   └── 点击整行: 弹出磁力链接对话框
    │
    ├── Tab 2: 无极磁链
    │   ├── 搜索: GET /search?q= (HTML)
    │   │   └── 解析: table.file-list → title, size, href
    │   ├── 文件列表: GET /!id (HTML, 点击三角)
    │   │   └── 解析: #input-magnet + table.file-list + 发布日期
    │   └── 点击整行: 弹出磁力链接对话框
    │
    └── Tab 3: btsow (新增)
        ├── 搜索: POST btsow.pics/api/search
        │   └── 返回: hash, name, size, lastUpdateTime
        ├── 文件列表: POST btsow.pics/api/magnet (搜索时同时获取)
        │   └── 返回: files[] (filename, size)
        ├── 点击三角 ▶: 展开/收起已加载的文件列表
        └── 点击整行: 打开磁力链接 (ACTION_VIEW)
```

---

## 六、版本历史

| 版本 | 变更 |
|------|------|
| v2.2.1 | 修复 MovieActivity 空指针：`JAViewer.SERVICE` 改为 `getService()` 懒初始化，增加 null 安全处理 |
| v2.2.0 | DownloadActivity 新增 btsow Tab (MagnetSearchFragment)、番号点击统一跳转 DownloadActivity、移除下载计数提示弹窗、移除 MagnetSearchActivity 死代码 |
| v2.1.0 | 新增 cili.info 源、文件列表展开/收起、BtSearch 详情 API 修复、MagnetSearch 时间显示 |
| v2.0.3 | BtSearch 磁力搜索、UI 优化 |
