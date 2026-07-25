package io.github.javiewer.adapter.item;

import java.io.Serializable;

/**
 * Project: JAViewer
 */
public class MagnetLink implements Serializable {

    protected String magnetLink;

    public static MagnetLink create(String magnetLink) {
        MagnetLink magnet = new MagnetLink();
        if (magnetLink != null && !magnetLink.isEmpty()) {
            magnet.magnetLink = magnetLink;
        }
        return magnet;
    }

    public String getMagnetLink() {
        return magnetLink;
    }
}
