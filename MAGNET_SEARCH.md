# 磁力搜索功能汇总

> 版本: v2.1.0 (versionCode 17)

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

---

## 三、MagnetSearchActivity

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `POST https://btsow.pics/bts/data/api/search` | `okhttp3.Request` | Body: `[{search:"code"},30,1]` |
| 2 | `POST https://btsow.pics/bts/data/api/magnet` | `okhttp3.Request` | Body: `["hash"]`，获取文件列表 |
| 3 | 构建 `TorrentGroup` | 内存处理 | 直接显示 |

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

- **无 Provider**，直接在 Activity 中用 okhttp3 同步请求
- **网络请求**: `JAViewer.HTTP_CLIENT`
- **时间显示**: `lastUpdateTime`(Unix时间戳) → `yyyy-MM-dd`

---

## 四、UI 交互

| 操作 | DownloadActivity | MagnetSearchActivity |
|------|------------------|----------------------|
| 点击整行 | 弹出磁力链接对话框 | 打开磁力链接 |
| 点击三角 ▶ | 请求详情 + 展开文件列表 | 展开/收起文件列表 |
| 长按 | - | 复制磁力链接 |

**DownloadActivity 布局:**
```
┌─────────────────────────────────────┐
│ 影片编号(加粗)              ▶/▼    │
│ 时间  大小                          │
├─────────────────────────────────────┤
│ 文件1                    1.91 MB   │
│ 文件2                    5.66 GB   │
└─────────────────────────────────────┘
```

**MagnetSearchActivity 布局:**
```
┌─────────────────────────────────────┐
│ 影片编号(加粗)              ▶/▼    │
│ 时间  大小                          │
├─────────────────────────────────────┤
│ 文件1                    1.91 MB   │
│ 文件2                    5.66 GB   │
└─────────────────────────────────────┘
```

---

## 五、涉及文件清单

### 网络层
| 文件 | 状态 | 职责 |
|------|------|------|
| `network/BtSearch.java` | 修改 | BtSearch API + 签名 + 详情接口 |
| `network/provider/BtSearchLinkProvider.java` | 修改 | BtSearch 解析（搜索+详情HTML） |
| `network/CiliInfo.java` | 新建 | cili.info API（search + get） |
| `network/provider/CiliInfoLinkProvider.java` | 新建 | cili.info HTML 解析 |
| `network/provider/DownloadLinkProvider.java` | 修改 | 注册 ciliinfo provider |

### Activity / Fragment
| 文件 | 状态 | 职责 |
|------|------|------|
| `activity/DownloadActivity.java` | 修改 | 双 Tab 布局 + 空对话框修复 |
| `activity/MagnetSearchActivity.java` | 修改 | 添加时间显示（lastUpdateTime） |
| `fragment/DownloadFragment.java` | 修改 | 首次加载后 setEnd |
| `fragment/BtSearchFragment.java` | 修改 | 传递 keyword 到 adapter |

### Adapter
| 文件 | 状态 | 职责 |
|------|------|------|
| `adapter/DownloadLinkAdapter.java` | 修改 | 文件列表展开/收起 + 按provider区分请求 |
| `adapter/MagnetFileAdapter.java` | 修改 | 布局匹配 + 时间显示 |

### 数据模型
| 文件 | 状态 | 职责 |
|------|------|------|
| `adapter/item/DownloadLink.java` | 修改 | 新增 files/date/filesExpanded 字段 |
| `adapter/item/TorrentGroup.java` | 修改 | 新增 date 字段 |

### 布局
| 文件 | 状态 | 职责 |
|------|------|------|
| `res/layout/layout_download.xml` | 修改 | 两行布局 + 三角符号 + 标题加粗 |
| `res/layout/card_magnet_file.xml` | 修改 | 匹配 DownloadActivity 样式 |

### 工具
| 文件 | 状态 | 职责 |
|------|------|------|
| `view/listener/BasicOnScrollListener.java` | 修改 | canLoadMore 检查 isEnd |

---

## 六、流程图

```
MovieActivity
    ├── FAB 点击 → DownloadActivity (v2.1.0)
    │   ├── Tab: BtSearch
    │   │   ├── 搜索: GET /api/search (JSON, 签名验证)
    │   │   │   └── 返回: id, name, size, hash, created_at
    │   │   └── 文件列表: GET /api/torrent/{id} (JSON, 点击三角)
    │   │       └── 返回: torrentfile[] (name, size)
    │   │
    │   └── Tab: 无极磁链
    │       ├── 搜索: GET /search?q= (HTML)
    │       │   └── 解析: table.file-list → title, size, href
    │       └── 文件列表: GET /!id (HTML, 点击三角)
    │           └── 解析: #input-magnet + table.file-list + 发布日期
    │
    └── 点击番号 → MagnetSearchActivity
                    ├── 搜索: POST btsow.pics/api/search
                    │   └── 返回: hash, name, size, lastUpdateTime
                    └── 文件列表: POST btsow.pics/api/magnet
                        └── 返回: files[] (filename, size)
```

---

## 七、版本历史

| 版本 | 变更 |
|------|------|
| v2.1.0 | 新增 cili.info 源、文件列表展开/收起、BtSearch详情API修复、MagnetSearch时间显示 |
| v2.0.3 | BtSearch 磁力搜索、UI优化 |
