package io.github.javiewer.adapter.item;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: JAViewer
 */
public class DownloadLink extends Linkable {
    protected String title;
    protected String size;
    protected String date;
    protected MagnetLink magnetLink;
    protected List<MagnetFile> files;
    public boolean filesExpanded;

    public static DownloadLink create(String title, String size, String date, String link, String magnetLink) {
        DownloadLink download = new DownloadLink();
        download.title = title;
        download.size = size;
        download.date = date;
        download.link = link;
        download.magnetLink = MagnetLink.create(magnetLink);
        return download;
    }

    public String getTitle() {
        return title;
    }

    public String getSize() {
        return size;
    }

    public String getDate() {
        return date;
    }

    public boolean hasMagnetLink() {
        return magnetLink != null && magnetLink.getMagnetLink() != null;
    }

    public String getMagnetLink() {
        return magnetLink != null ? magnetLink.getMagnetLink() : null;
    }

    public void setMagnetLink(MagnetLink magnetLink) {
        this.magnetLink = magnetLink;
    }

    public List<MagnetFile> getFiles() {
        return files;
    }

    public void setFiles(List<MagnetFile> files) {
        this.files = files;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
