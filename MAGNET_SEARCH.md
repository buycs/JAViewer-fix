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
| 1 | `GET https://www.btsearch.love/api/search?keyword=X&limit=10&offset=N&...` | `BtSearch.search()` | JSON API，带签名验证 |
| 2 | 直接从返回的 hash 拼接 | `magnet:?xt=urn:btih:{hash}` | 无需二次请求 |

- **Provider**: `BtSearchLinkProvider`
- **网络接口**: `BtSearch.java`（独立 OkHttpClient + MD5 签名）

### Tab 2: 无极磁链

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `GET https://cili.info/search?q=X` | `CiliInfo.search()` | 返回 HTML |
| 2 | 解析 `table.file-list tbody tr` | Jsoup | 提取标题、大小、详情链接（如 `/!lBfm`） |
| 3 | `GET https://cili.info/!lBfm` | `CiliInfo.get()` | 点击时请求 |
| 4 | 解析 `#input-magnet` input value | Jsoup | 提取完整磁力链接 |

- **Provider**: `CiliInfoLinkProvider`
- **网络接口**: `CiliInfo.java`

---

## 三、MagnetSearchActivity

| 步骤 | 接口 | 方法 | 说明 |
|------|------|------|------|
| 1 | `POST https://btsow.pics/bts/data/api/search` | `okhttp3.Request` | Body: `[{search:"code"},30,1]` |
| 2 | `POST https://btsow.pics/bts/data/api/magnet` | `okhttp3.Request` | Body: `["hash"]`，获取文件列表 |
| 3 | 构建 `TorrentGroup` | 内存处理 | 直接显示 |

- **无 Provider**，直接在 Activity 中用 okhttp3 同步请求
- **网络请求**: `JAViewer.HTTP_CLIENT`

---

## 四、涉及文件清单

| 文件 | 状态 | 职责 |
|------|------|------|
| `network/BtSearch.java` | 原有 | BtSearch API + 签名 |
| `network/provider/BtSearchLinkProvider.java` | 原有 | BtSearch 解析 |
| `network/CiliInfo.java` | **新建** | cili.info API |
| `network/provider/CiliInfoLinkProvider.java` | **新建** | cili.info HTML 解析 |
| `network/provider/DownloadLinkProvider.java` | 修改 | 注册 ciliinfo |
| `activity/DownloadActivity.java` | 修改 | 双 Tab |
| `activity/MagnetSearchActivity.java` | 原有 | btsow.pics 直接请求 |
| `fragment/DownloadFragment.java` | 修改 | 首次加载后 setEnd |
| `view/listener/BasicOnScrollListener.java` | 修改 | canLoadMore 检查 isEnd |

---

## 五、流程图

```
MovieActivity
    ├── FAB 点击 → DownloadActivity
    │   ├── Tab: BtSearch → btsearch.love API（直接返回 hash）
    │   └── Tab: 无极磁链 → cili.info HTML（搜索→详情→提取磁力链接）
    │
    └── 点击番号 → MagnetSearchActivity
                    └── btsow.pics API（search→magnet→文件列表）
```
