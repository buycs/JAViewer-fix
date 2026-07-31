# JAViewer API 接口文档

> 基于项目源码 v2.2.1 整理

---

## 目录

1. [数据源配置](#1-数据源配置)
2. [BasicService — 主数据源 API](#2-basicservice--主数据源-api)
3. [Avgle — 视频搜索 API](#3-avgle--视频搜索-api)
4. [BTSO — BT 搜索 API](#4-btso--bt-搜索-api)
5. [BtSearch — 带签名验证的 BT 搜索 API](#5-btsearch--带签名验证的-bt-搜索-api)
6. [CiliInfo — 无极磁链 HTML 解析 API](#6-ciliinfo--无极磁链-html-解析-api)
7. [PSVS — 在线视频播放 API](#7-psvs--在线视频播放-api)
8. [btsow — 磁力搜索 API（OkHttp 直连）](#8-btsow--磁力搜索-apiokhttp-直连)
9. [Provider 层](#9-provider-层)
10. [数据模型](#10-数据模型)
11. [接口调用流程图](#11-接口调用流程图)

---

## 1. 数据源配置

**文件:** `assets/properties.json`

应用启动时从 `assets/properties.json` 加载数据源列表，运行时可在 UI 中切换。

```json
{
  "latest_version": "2.0.0 Alpha 1",
  "latest_version_code": "13",
  "changelog": "...",
  "data_sources": [
    {
      "name": "骑兵",
      "domain": "https://avmoo.shop",
      "apiPath": "/jav/data/api/",
      "legacies": ["javzoo.com", "avmoo.xyz", ...]
    },
    {
      "name": "步兵",
      "domain": "https://avsox.click",
      "apiPath": "/javu/data/api/",
      "legacies": ["avme.pw", "avsox.net", ...]
    },
    {
      "name": "欧美",
      "domain": "https://avheat.shop",
      "apiPath": "/wav/data/api/",
      "legacies": []
    }
  ]
}
```

**Base URL 构建规则:** `domain + apiPath`，例: `https://avmoo.shop/jav/data/api/`

**域名替换:** `JAViewer.hostReplacements` Map 可将旧域名（legacies）映射为当前活跃域名，所有 OkHttp 请求通过 `replaceUrl()` 拦截器自动替换。

---

## 2. BasicService — 主数据源 API

**文件:** `network/BasicService.java`
**创建:** `JAViewer.recreateService()` / `JAViewer.getService()`（懒初始化，失败返回 null）

所有 POST 请求的 **Content-Type**: `application/json; charset=utf-8`
请求体均为 `List<Object>` (JSON Array)，通过 `JAViewer.getService()`（全局 Retrofit 实例，懒初始化）调用。

### 2.1 获取首页影片

```
POST {baseUrl}getMovies
Body: ["home", 60, page]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | 固定 `"home"` |
| `[1]` | int | 每页数量（固定 60） |
| `[2]` | int | 页码（从 1 开始） |

**调用方:** `HomeFragment.java:12`

---

### 2.2 获取影片详情

```
POST {baseUrl}getMovie
Body: [movieId, "cn"]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | `Movie.link`（由 Movie.create 传入的 movieId） |
| `[1]` | string | 语言，固定 `"cn"` |

**调用方:** `MovieActivity.java:139`

---

### 2.3 获取女优列表

```
POST {baseUrl}getStars
Body: ["stars", 60, page]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | 固定 `"stars"` |
| `[1]` | int | 每页数量（固定 60） |
| `[2]` | int | 页码（从 1 开始） |

**调用方:** `ActressesFragment.java:97`

---

### 2.4 获取分类列表

```
POST {baseUrl}getGenres
Body: ["types", 60]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | 固定 `"types"` |
| `[1]` | int | 数量（固定 60） |

**调用方:** `GenreTabsFragment.java`

---

### 2.5 获取筛选影片列表

```
POST {baseUrl}getFilterMovies
Body: [action, keyword, "cn", 60, page]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | 筛选类型: `"popular"` / `"released"` / `"genre"` / `"star"` / `"studio"` / `"director"` / `"label"` / `"series"` |
| `[1]` | string | 筛选关键字（ID） |
| `[2]` | string | 语言，固定 `"cn"` |
| `[3]` | int | 每页数量（固定 60） |
| `[4]` | int | 页码（从 1 开始） |

**调用方:**
- `PopularFragment.java:12` — `["popular", "", "cn", 60, page]`
- `ReleasedFragment.java:12` — `["released", "", "cn", 60, page]`
- `MovieListFragment.java:30` — `[action, keyword, "cn", 60, page]`

---

### 2.6 搜索影片

```
POST {baseUrl}search
Body: [keyword, 60, page]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | 搜索关键词 |
| `[1]` | int | 每页数量（固定 60） |
| `[2]` | int | 页码（从 1 开始） |

**调用方:** `MovieListFragment.java:32`

---

### 2.7 获取相关影片

```
POST {baseUrl}getRelatedMovies
Body: [movieId, "cn", 12]
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `[0]` | string | 影片 ID（`Movie.link`） |
| `[1]` | string | 语言，固定 `"cn"` |
| `[2]` | int | 返回数量（固定 12） |

**调用方:** `MovieActivity.java:275`

---

## 3. Avgle — 视频搜索 API

**文件:** `network/Avgle.java`

**Base URL:** `https://api.avgle.com`

```java
Avgle INSTANCE = new Retrofit.Builder()
    .baseUrl("https://api.avgle.com")
    .client(JAViewer.HTTP_CLIENT)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(Avgle.class);
```

### 3.1 搜索视频

```
GET /v1/search/{keyword}/0?limit=1
Headers:
  Accept-Language: zh-CN,zh;q=0.8,en;q=0.6
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| `keyword` | Path | string | 搜索关键词（影片番号） |
| `limit` | Query | int | 固定 1 |

**响应 (Gson 反序列化为 `AvgleSearchResult`):**

```json
{
  "success": true,
  "response": {
    "has_more": false,
    "total_videos": 1,
    "current_offset": 0,
    "limit": 1,
    "videos": [
      {
        "title": "...",
        "keyword": "...",
        "channel": "...",
        "duration": 1234.5,
        "framerate": 29.97,
        "hd": true,
        "addtime": 1234567890,
        "viewnumber": 1000,
        "likes": 10,
        "dislikes": 0,
        "video_url": "https://...",
        "embedded_url": "https://...",
        "preview_url": "https://...",
        "preview_video_url": "https://...",
        "public": true,
        "vid": "...",
        "uid": "..."
      }
    ]
  }
}
```

**调用方:** 已废弃（注释掉的 `onPlay()`），被 PSVS 替代。

### 3.2 通用 GET

```
GET /{path}
```

通用请求，用于任意路径。

---

## 4. BTSO — BT 搜索 API

**文件:** `network/BTSO.java`

**Base URL:** `https://api.rekonquer.com`

```java
BTSO INSTANCE = new Retrofit.Builder()
    .baseUrl("https://api.rekonquer.com")
    .client(JAViewer.HTTP_CLIENT)
    .build()
    .create(BTSO.class);
```

### 4.1 搜索

```
GET /btso.php?kw={keyword}&page={page}
Headers:
  Accept-Language: zh-CN,zh;q=0.8,en;q=0.6
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `kw` | string | 搜索关键词 |
| `page` | int | 页码 |

**响应:** HTML，由 `BTSOLinkProvider.parseDownloadLinks()` 通过 Jsoup 解析。

### 4.2 通用 GET

```
GET {url}
```

获取详情页 HTML。

---

## 5. BtSearch — 带签名验证的 BT 搜索 API

**文件:** `network/BtSearch.java`

**Base URL:** `https://www.btsearch.love`

使用**独立 OkHttpClient**（`BTSEARCH_CLIENT`），通过拦截器自动注入签名头。每次请求都动态计算签名。

### 5.0 鉴权机制

#### 5.0.1 签名流程

```
客户端请求
    │
    ├─ 收集所有 Query 参数（来自 Retrofit @Query 注解）
    │   └─ 例如: keyword, limit, offset, mode, time, sort, sort_type, size
    │
    ├─ 生成 timestamp = currentTimeMillis() / 1000（秒级 Unix 时间戳）
    │
    ├─ 生成 nonce    = UUID.randomUUID() → 去连字符 → 取前 8 位
    │   └─ 示例: "a1b2c3d4"
    │
    ├─ 合并参数 Map: query参数 + timestamp + nonce
    │
    ├─ 调用 generateSign(params) 计算签名
    │   │
    │   ├─ 遍历 params，构建 "key=value" 字符串列表
    │   ├─ 按字典序（字母顺序）排序
    │   ├─ 用 & 拼接所有 key=value
    │   └─ 末尾追加 "&key=long2ice"
    │       └─ 结果示例: keyword=MADB-004&limit=10&mode=&nonce=a1b2c3d4&offset=0&size=&sort=&sort_type=asc&time=&timestamp=1722000000&key=long2ice
    │
    ├─ MD5 加密（32 位小写 hex）→ 转大写
    │   └─ 示例: "A1B2C3D4E5F6789012345678ABCDEF90"
    │
    └─ 设置请求头:
        ├─ x-timestamp: "1722000000"
        ├─ x-nonce:     "a1b2c3d4"
        ├─ x-sign:      "A1B2C3D4E5F6789012345678ABCDEF90"
        ├─ Accept:      "application/json"
        ├─ User-Agent:  "Mozilla/5.0 ... Chrome/91 ..."
        └─ Referer:     "https://www.btsearch.love/search"
```

#### 5.0.2 关键参数

| 参数 | 生成方式 | 示例值 | 代码位置 |
|------|----------|--------|----------|
| `timestamp` | `System.currentTimeMillis() / 1000`（秒级） | `1722000000` | `BtSearch.java:35` |
| `nonce` | `UUID.randomUUID().toString().replace("-","").substring(0,8)` | `a1b2c3d4` | `BtSearch.java:36` |
| `sign` | MD5(排序后 query string + `&key=long2ice`).toUpperCase() | `A1B2C3D4...` | `BtSearch.java:45` |
| `Secret Key` | 固定值 `long2ice` | — | `BtSearch.java:28` |

#### 5.0.3 签名算法伪代码

```
function generateSign(params: Map<String, String>) -> String:
    pairs = []
    for (key, value) in params:
        pairs.add(key + "=" + value)
    pairs.sort()  // 字典序升序
    raw = pairs.join("&") + "&key=long2ice"
    return MD5(raw).toUpperCase()
```

#### 5.0.4 Java 实现（完整）

```java
// BtSearch.java
static String generateSign(Map<String, String> params) {
    List<String> sorted = new ArrayList<>();
    for (Map.Entry<String, String> entry : params.entrySet()) {
        sorted.add(entry.getKey() + "=" + entry.getValue());
    }
    Collections.sort(sorted);                            // 字典序排序

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sorted.size(); i++) {
        if (i > 0) sb.append("&");
        sb.append(sorted.get(i));
    }
    sb.append("&key=").append("long2ice");               // 追加 Secret Key

    return md5(sb.toString()).toUpperCase();             // MD5 → 大写
}

static String md5(String input) {
    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] digest = md.digest(input.getBytes("UTF-8"));
    StringBuilder sb = new StringBuilder();
    for (byte b : digest) {
        sb.append(String.format("%02x", b & 0xff));      // 小写 hex
    }
    return sb.toString();
}
```

#### 5.0.5 拦截器注入

签名和头部注入在 Retrofit 底层的 OkHttp 拦截器中完成，对上层 `Call` 调用透明：

```java
// BTSEARCH_CLIENT 拦截器（BtSearch.java:30-61）
OkHttpClient BTSEARCH_CLIENT = new OkHttpClient.Builder()
    .addInterceptor(chain -> {
        Request original = chain.request();
        HttpUrl url = original.url();

        // 1. 生成时间戳和随机数
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String nonce = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 2. 收集 Query 参数并合并
        Map<String, String> params = new HashMap<>();
        for (int i = 0; i < url.querySize(); i++) {
            params.put(url.queryParameterName(i), url.queryParameterValue(i));
        }
        params.put("timestamp", timestamp);
        params.put("nonce", nonce);

        // 3. 计算签名
        String sign = generateSign(params);

        // 4. 注入请求头
        Request finalRequest = original.newBuilder()
            .header("x-timestamp", timestamp)
            .header("x-nonce", nonce)
            .header("x-sign", sign)
            .header("Accept", "application/json")
            .header("User-Agent", JAViewer.USER_AGENT)   // Chrome 91 Windows UA
            .header("Referer", BASE_URL + "/search")
            .build();

        return chain.proceed(finalRequest);
    })
    .build();
```

#### 5.0.6 请求头汇总

| Header | 值 | 说明 |
|--------|-----|------|
| `x-timestamp` | Unix 秒级时间戳 | 防重放，服务端校验时间窗口 |
| `x-nonce` | 8 位随机 hex 字符串 | 防重放，与 timestamp 组合唯一 |
| `x-sign` | MD5 签名（大写 32 位） | 请求完整性校验 |
| `Accept` | `application/json` | 强制 JSON 响应 |
| `User-Agent` | Chrome 91 on Windows 10 | 反爬虫绕过 |
| `Referer` | `https://www.btsearch.love/search` | 模拟浏览器来源 |

### 5.1 搜索

```
GET /api/search?keyword={keyword}&limit={limit}&offset={offset}&mode={mode}&time={time}&sort={sort}&sort_type={sort_type}&size={size}
```

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `keyword` | string | - | 搜索关键词 |
| `limit` | int | 10 | 每页数量 |
| `offset` | int | 0 | 偏移量 |
| `mode` | string | "" | 模式 |
| `time` | string | "" | 时间范围 |
| `sort` | string | "" | 排序字段 |
| `sort_type` | string | "asc" | 排序方向 |
| `size` | string | "" | 大小过滤 |

**响应 (JSON):**

```json
{
  "total": 100,
  "data": [
    {
      "id": 2419147587,
      "name": "MADB-004",
      "size": "6083314266",
      "created_at": "2026-07-25T21:05:20.240Z",
      "hash": "969af3a5efd45076cb3aadd1a83fb54fd7d3b195",
      "count": 2,
      "hot": 0
    }
  ],
  "time_ms": 123
}
```

### 5.2 获取详情（文件列表）

```
GET /api/torrent/{id}?keyword={keyword}
```

| 参数 | 位置 | 类型 | 说明 |
|------|------|------|------|
| `id` | Path | long | 种子 ID |
| `keyword` | Query | string | 搜索关键词 |

**响应 (JSON):**

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

### 5.3 搜索调用参数示例

```
GET /api/search?keyword=MADB-004&limit=10&offset=0&mode=&time=&sort=&sort_type=asc&size=
x-timestamp: 1722000000
x-nonce: a1b2c3d4
x-sign: A1B2C3D4E5F6789012345678ABCDEF90
Accept: application/json
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 ...
Referer: https://www.btsearch.love/search
```

```java
// BtSearchLinkProvider.searchApi()
BtSearch.INSTANCE.search(keyword, 10, (page-1)*10, "", "", "", "asc", "");
```

**调用方:** `BtSearchFragment.java` — 签名由 `BTSEARCH_CLIENT` 拦截器自动注入，`search()` 调用方无感知。

---

## 6. CiliInfo — 无极磁链 HTML 解析 API

**文件:** `network/CiliInfo.java`

**Base URL:** `https://cili.info`

```java
CiliInfo INSTANCE = new Retrofit.Builder()
    .baseUrl("https://cili.info")
    .client(JAViewer.HTTP_CLIENT)
    .build()
    .create(CiliInfo.class);
```

### 6.1 搜索

```
GET /search?q={keyword}
Headers:
  Accept: text/html,...
  Accept-Language: zh-CN,zh;q=0.9
  Referer: https://cili.info/
```

**响应:** HTML，由 `CiliInfoLinkProvider.parseDownloadLinks()` 解析。

**HTML 结构:**

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

### 6.2 获取详情

```
GET {url}  (e.g., /!lBfm 或 https://cili.info/!lBfm)
```

同上请求头。

**HTML 结构:**

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

---

## 7. PSVS — 在线视频播放 API

**文件:** `network/PSVS.java`

**Base URL:** `http://api.rekonquer.com`

```java
PSVS INSTANCE = new Retrofit.Builder()
    .baseUrl("http://api.rekonquer.com")
    .client(JAViewer.HTTP_CLIENT)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(PSVS.class);
```

### 7.1 搜索视频源

```
GET /psvs/search.php?kw={keyword}
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `kw` | string | 影片番号 |

**响应:** `AvgleSearchResult`（同 Avgle API 响应结构）

### 7.2 播放地址生成

获取到 `vid` 后，构造播放 URL:

```
http://api.rekonquer.com/psvs/mp4.php?vid={vid}&ts={timestamp}&sign={sign}
```

- `timestamp` = `System.currentTimeMillis() / 1000`（秒级 Unix 时间戳）
- `sign` = MD5(`vid` + `timestamp` + "Brynhildr") 的 hex 字符串

```java
// JAViewer.b(vid, ts) 实现:
MessageDigest md = MessageDigest.getInstance("MD5");
byte[] bytes = md.digest(String.format("%s%sBrynhildr", vid, ts).getBytes());
return bytesToHex(bytes);
```

**调用方:** `MovieActivity.java:467-512` (`onPlay()` / `onClickPreview()`)

---

## 8. btsow — 磁力搜索 API（OkHttp 直连）

**文件:** `fragment/MagnetSearchFragment.java`（非 Retrofit，直接使用 OkHttp）

**Base URL:** `https://btsow.pics`

### 8.0 鉴权机制

btsow API **无显式鉴权**（无 API Key、无签名算法）。请求通过共享 `JAViewer.HTTP_CLIENT`（`OkHttpClient`）发出，自动应用以下拦截器链：

| 拦截器 | 行为 | 代码位置 |
|--------|------|----------|
| 域名替换 | 将请求 URL 中匹配 `hostReplacements` 的 host 替换为当前活跃域名 | `JAViewer.replaceUrl()` |
| User-Agent | 覆盖 `User-Agent` 头为 Chrome 91 Windows UA | `JAViewer.java:107` |
| X-Requested-With | 添加 `X-Requested-With: XMLHttpRequest`（对所有非 torrentkitty/btsearch 的 host） | `JAViewer.java:110-111` |
| Cookie 持久化 | 自动保存/发送 `Cookie` 头（内存级 `CookieJar`） | `JAViewer.COOKIE_JAR` |

**btsow 请求实际发出的请求头示例:**
```http
POST /bts/data/api/search HTTP/1.1
Host: btsow.pics
content-type: application/json
User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36
X-Requested-With: XMLHttpRequest
```

> 注意：如果服务器校验 `User-Agent` 或 `X-Requested-With`，则替换 header 可能被服务端视为异常。当前实现使用 Chrome 91 标准 UA。

### 8.1 搜索

```
POST https://btsow.pics/bts/data/api/search
Content-Type: application/json

[{"search": "MADB-004"}, 30, 1]
```

| Body 元素 | 类型 | 说明 |
|-----------|------|------|
| `[0].search` | string | 搜索关键词 |
| `[1]` | int | 每页数量（固定 30） |
| `[2]` | int | 页码（固定 1） |

**响应:**

```json
{
  "data": [
    {
      "hash": "6D8CD7F3E8821906D4EB3E0759A59F581FB2A8F5",
      "name": "MADB-004-C ...",
      "size": 8484889014,
      "lastUpdateTime": 1783840027
    }
  ]
}
```

### 8.2 获取文件列表

```
POST https://btsow.pics/bts/data/api/magnet
Content-Type: application/json

["6D8CD7F3E8821906D4EB3E0759A59F581FB2A8F5"]
```

**响应:**

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

### 8.3 磁力链接构造

```
magnet:?xt=urn:btih:{hash}&dn={torrentName}
```

`torrentName` 需 URL 编码。

---

## 9. Provider 层

### 9.1 AVMOProvider

**文件:** `network/provider/AVMOProvider.java`

主数据源 JSON 解析器，将 BasicService 返回的 JSON 解析为数据模型。

| 方法 | 输入 | 输出 | 说明 |
|------|------|------|------|
| `parseMovies(json)` | JSON string | `List<Movie>` | 解析影片列表 |
| `parseActresses(json)` | JSON string | `List<Actress>` | 解析女优列表 |
| `parseMoviesDetail(json)` | JSON string | `MovieDetail` | 解析影片详情 |
| `parseGenres(json)` | JSON string | `LinkedHashMap<String, List<Genre>>` | 解析分类列表 |

**JSON 字段映射:**

| 模型字段 | JSON 路径 | 说明 |
|----------|-----------|------|
| `Movie.title` | `data[].title` | 影片标题 |
| `Movie.code` | `data[].movieFanHao` | 影片番号 |
| `Movie.coverUrl` | `data[].posterSmall` | 封面小图 |
| `Movie.date` | `data[].releaseDate` | 发行日期 |
| `Movie.link` | `data[].movieId` | 详情链接 ID |
| `MovieDetail.title` | `data.title` | 详情标题 |
| `MovieDetail.code` | `data.movieFanHao` | 详情番号 |
| `MovieDetail.coverUrl` | `data.posterLarge` | 封面大图 |
| `MovieDetail.btsSearchUrl` | `data.btsSearchUrl` | BT 搜索 URL |
| `Screenshot.thumbnailUrl` | `data.sampleSmall[]` | 截图缩略图 |
| `Screenshot.link` | `data.sampleLarge[]` | 截图大图 |
| `Actress.name` | `data.star[].starName` / `data[].starName` | 女优名 |
| `Actress.imageUrl` | `data.star[].avatarUrl` / `data[].avatarUrl` | 头像 |
| `Actress.link` | `data.star[].starId` / `data[].starId` | 女优详情 ID |
| `Genre.name` | `data.genre[].genreName` | 分类名称 |
| `Genre.link` | `data.genre[].genreId` | 分类 ID |
| `Header.name/value` | `data.releaseDate/length/director/studio/label/series` | 元数据 |
| `data[].movieId`/`data[].starId` | `link` | 用于后续 API 调用 |

### 9.2 DownloadLinkProvider 体系

**文件:** `network/provider/DownloadLinkProvider.java`（抽象基类）

根据 provider 名称返回对应实现:

| 名称 | 实现类 | 网络接口 |
|------|--------|----------|
| `"btso"` | `BTSOLinkProvider` | `BTSO` |
| `"torrentkitty"` | `TorrentKittyLinkProvider` | `TorrentKitty` |
| `"btsearch"` | `BtSearchLinkProvider` | `BtSearch` |
| `"ciliinfo"` / `"cili"` | `CiliInfoLinkProvider` | `CiliInfo` |

**抽象方法:**

| 方法 | 返回 | 说明 |
|------|------|------|
| `search(keyword, page)` | `Call<ResponseBody>` | 搜索请求 |
| `parseDownloadLinks(html)` | `List<DownloadLink>` | 解析搜索结果 |
| `get(url)` | `Call<ResponseBody>` | 详情页请求 |
| `parseMagnetLink(html)` | `MagnetLink` | 解析磁力链接 |
| `parseFileList(html)` | `List<MagnetFile>` | 解析文件列表（可选） |
| `parseDate(html)` | `String` | 解析发布日期（可选） |

### 9.3 BTSOLinkProvider

**文件:** `network/provider/BTSOLinkProvider.java`

解析 BTSO HTML:

- 搜索: 按 `class="row"` 提取 `a` 标签 URL + `file` / `size` / `date` class
- 详情: 按 `class="magnet-link"` 提取磁力链接文本

### 9.4 TorrentKittyLinkProvider

**文件:** `network/provider/TorrentKittyLinkProvider.java`

解析 TorrentKitty HTML:

- 搜索: 按 `#archiveResult` 表格中的 `class="name"` / `size` / `date` 提取
- 详情: 按 `class="magnet-link"` 提取磁力链接

### 9.5 BtSearchLinkProvider

**文件:** `network/provider/BtSearchLinkProvider.java`

BtSearch JSON 解析:

- `parseSearchResult()`: 将 `BtSearch.SearchResult` 转为 `List<DownloadLink>`
- `parseFilesFromJson()`: 解析 `torrentfile` JSON 数组
- `parseFilesFromHtml()`: 后备解析方案（Jsoup 解析 HTML）

### 9.6 CiliInfoLinkProvider

**文件:** `network/provider/CiliInfoLinkProvider.java`

解析 cili.info HTML:

- 搜索: 按 `table.table-hover.file-list tbody tr` 提取
- 详情: 按 `#input-magnet` 提取磁力链接值
- 文件列表: 同上表格
- 日期: 按 `dt` 包含"发布日期"的兄弟 `dd` 提取

---

## 10. 数据模型

**包:** `adapter/item/`

| 类 | 字段 | 说明 |
|----|------|------|
| `Linkable` | `link: String` | 可链接基类（序列化） |
| `Movie` | `title, code, coverUrl, date, hot` | 影片列表项 |
| `MovieDetail` | `title, code, coverUrl, btsSearchUrl, screenshots, headers, genres, actresses` | 影片详情 |
| `MovieDetail.Header` | `name, value, link` | 元数据行（发行日期/时长/导演/制作商/发行商/系列） |
| `Actress` | `name, imageUrl` | 女优 |
| `Genre` | `name` | 分类 |
| `Screenshot` | `thumbnailUrl, link(imageUrl)` | 截图 |
| `DataSource` | `name, domain, apiPath, legacies` | 数据源配置 |
| `DownloadLink` | `title, size, date, link, magnetLink, files, filesExpanded` | 下载链接项 |
| `MagnetLink` | `magnetLink: String` | 磁力链接 |
| `MagnetFile` | `hash, torrentName, filename, size` | 磁力文件 |
| `TorrentGroup` | `hash, torrentName, totalSize, date, expanded, files` | btsow 磁力组 |
| `AvgleSearchResult` | `success, response` | Avgle/PSVS 搜索结果 |
| `Properties` | `latest_version, latest_version_code, changelog, data_sources` | properties.json 映射 |

---

## 11. 接口调用流程图

```
应用启动
  │
  └─ StartActivity
      └─ 读取 assets/properties.json → JAViewer.DATA_SOURCES
      └─ 从 /sdcard/JAViewer/configurations.json 加载配置
      └─ 启动 MainActivity

MainActivity
  ├─ recreateService() / getService() → 创建 Retrofit(BasicService)，懒初始化
  │
  ├─ 首页: HomeFragment.newCall(page) → POST getMovies ["home", 60, page]
  ├─ 热门: PopularFragment.newCall(page) → POST getFilterMovies ["popular", "", "cn", 60, page]
  ├─ 最新: ReleasedFragment.newCall(page) → POST getFilterMovies ["released", "", "cn", 60, page]
  ├─ 女优: ActressesFragment.newCall(page) → POST getStars ["stars", 60, page]
  ├─ 分类: GenreTabsFragment → POST getGenres ["types", 60]
  ├─ 搜索: MovieListFragment.newCall(page) → POST search [keyword, 60, page]
  │      筛选模式 → POST getFilterMovies [action, id, "cn", 60, page]
  │
  └─ 影片详情: MovieActivity
      ├─ POST getMovie [movieId, "cn"] → 解析 AVMOProvider.parseMoviesDetail()
      ├─ POST getRelatedMovies [movieId, "cn", 12] → 相关影片
      ├─ FAB → DownloadActivity(keyword=movie.code)
      │
      ├─ 预览视频: PSVS.INSTANCE.search(code) → GET /psvs/search.php?kw=code
      │   └─ 构造播放 URL: /psvs/mp4.php?vid={vid}&ts={ts}&sign={sign}
      │
      └─ 播放视频: PSVS.INSTANCE.search(code) → 同上

DownloadActivity (三 Tab)
  │
  ├─ Tab 1: BtSearch
  │   ├─ GET /api/search (BtSearch, MD5 签名)
  │   ├─ GET /api/torrent/{id} (文件列表)
  │   └─ Provider: BtSearchLinkProvider
  │
  ├─ Tab 2: 无极磁链 (cili.info)
  │   ├─ GET /search?q=keyword (HTML)
  │   ├─ GET /!xxxxx (详情 HTML)
  │   └─ Provider: CiliInfoLinkProvider
  │
  └─ Tab 3: btsow
      ├─ POST /bts/data/api/search (JSON)
      ├─ POST /bts/data/api/magnet (文件列表)
      └─ Fragment: MagnetSearchFragment (OkHttp 直连)

通用 OkHttp 拦截器:
  └─ User-Agent: Chrome 91
  └─ X-Requested-With: XMLHttpRequest (非 torrentkitty/btsearch)
  └─ 域名替换: hostReplacements Map → 自动替换 host
```

---

*文档生成时间: 2026-07-26*
