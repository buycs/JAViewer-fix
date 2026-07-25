package io.github.javiewer.adapter.item;

import java.util.List;

/**
 * Project: JAViewer
 */

public class DataSource extends Linkable {

    public String name;
    public String domain;
    public String apiPath;
    public List<String> legacies;

    public DataSource() {
    }

    public DataSource(String name, String domain, String apiPath) {
        this.name = name;
        this.domain = domain;
        this.apiPath = apiPath;
        this.link = domain + apiPath;
    }

    public String getLink() {
        if (domain != null && apiPath != null) {
            return domain + apiPath;
        }
        return link;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataSource) {
            return getLink() != null && getLink().equals(((DataSource) o).getLink());
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return name;
    }

}
