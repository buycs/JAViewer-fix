package io.github.javiewer.adapter.item;

/**
 * Project: JAViewer
 */
public class Actress extends Linkable {

    protected String name;
    protected String imageUrl;
    /** 作品数量；数据源没给（或来自旧收藏数据）时为 0。 */
    protected int movieCount;

    public static Actress create(String name, String imageUrl, String detailUrl) {
        return create(name, imageUrl, detailUrl, 0);
    }

    public static Actress create(String name, String imageUrl, String detailUrl, int movieCount) {
        Actress actress = new Actress();
        actress.name = name;
        actress.imageUrl = imageUrl;
        actress.link = detailUrl;
        actress.movieCount = movieCount;
        return actress;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    /** 作品数量；未知时为 0，调用方据此决定是否展示。 */
    public int getMovieCount() {
        return movieCount;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }

        if (obj instanceof Actress) {
            String other = ((Actress) obj).getName();
            return name == null ? other == null : name.equals(other);
        }

        return false;
    }
}
