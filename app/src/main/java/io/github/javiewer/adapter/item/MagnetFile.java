package io.github.javiewer.adapter.item;

import java.io.Serializable;

public class MagnetFile implements Serializable {
    public String hash;
    public String torrentName;
    public String filename;
    public long size;

    public String getMagnetLink() {
        String dn;
        try { dn = java.net.URLEncoder.encode(torrentName, "UTF-8"); } catch (Exception e) { dn = torrentName; }
        return "magnet:?xt=urn:btih:" + hash + "&dn=" + dn;
    }
}
