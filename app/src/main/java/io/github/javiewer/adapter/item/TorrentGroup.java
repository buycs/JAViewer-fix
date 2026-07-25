package io.github.javiewer.adapter.item;

import java.util.ArrayList;
import java.util.List;

public class TorrentGroup {
    public String hash;
    public String torrentName;
    public long totalSize;
    public boolean expanded;
    public List<MagnetFile> files = new ArrayList<>();

    public String getMagnetLink() {
        try {
            String dn = java.net.URLEncoder.encode(torrentName, "UTF-8").replace("+", "%20");
            return "magnet:?xt=urn:btih:" + hash + "&dn=" + dn;
        } catch (Exception e) {
            return "magnet:?xt=urn:btih:" + hash;
        }
    }
}
