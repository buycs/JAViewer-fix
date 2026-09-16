package io.github.javiewer.util;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.github.javiewer.R;
import io.github.javiewer.adapter.item.MagnetFile;

public final class MagnetFiles {

    private static final String[] MEDIA_EXTENSIONS = {
            ".mp4", ".mkv", ".avi", ".wmv", ".mov", ".flv", ".ts", ".m2ts",
            ".m4v", ".rmvb", ".iso", ".vob", ".mpg", ".mpeg", ".webm", ".asf"
    };

    private MagnetFiles() {
    }

    public static boolean isMediaFile(MagnetFile file) {
        String name = displayName(file);
        if (name.isEmpty()) {
            return false;
        }
        String lower = sanitizeFileName(name).toLowerCase(Locale.ROOT);
        for (String ext : MEDIA_EXTENSIONS) {
            if (lower.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    public static List<MagnetFile> mediaFiles(List<MagnetFile> files) {
        ArrayList<MagnetFile> media = new ArrayList<>();
        if (files == null) {
            return media;
        }
        for (MagnetFile file : files) {
            if (isMediaFile(file)) {
                media.add(file);
            }
        }
        if (media.isEmpty()) {
            media.addAll(files);
        }
        return media;
    }

    public static String displayName(MagnetFile file) {
        if (file == null) {
            return "";
        }
        if (file.filename != null && !file.filename.trim().isEmpty()) {
            return file.filename;
        }
        return file.torrentName != null ? file.torrentName : "";
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[\\u200B-\\u200D\\uFEFF\\u00A0]", "")
                .replace("．", ".")
                .trim();
    }

    public static MagnetFile mainFile(List<MagnetFile> files) {
        MagnetFile main = null;
        if (files == null) {
            return null;
        }
        for (MagnetFile file : files) {
            if (main == null || file.size > main.size) {
                main = file;
            }
        }
        return main;
    }

    public static void bindMediaFileList(LayoutInflater inflater, ViewGroup container, List<MagnetFile> files) {
        container.removeAllViews();
        List<MagnetFile> media = mediaFiles(files);
        MagnetFile main = mainFile(media);
        for (MagnetFile file : media) {
            View fileView = inflater.inflate(R.layout.item_magnet_file, container, false);
            TextView nameView = fileView.findViewById(R.id.file_name);
            TextView sizeView = fileView.findViewById(R.id.file_size);
            nameView.setText(displayName(file));
            sizeView.setText(formatSize(file.size));
            if (file == main) {
                nameView.setTypeface(nameView.getTypeface(), Typeface.BOLD);
                nameView.setTextColor(0xDE000000);
            }
            container.addView(fileView);
        }
    }

    public static String formatSize(long bytes) {
        if (bytes >= 1073741824) {
            return String.format(Locale.US, "%.1f GB", bytes / 1073741824.0);
        }
        if (bytes >= 1048576) {
            return String.format(Locale.US, "%.1f MB", bytes / 1048576.0);
        }
        if (bytes >= 1024) {
            return String.format(Locale.US, "%.1f KB", bytes / 1024.0);
        }
        return bytes + " B";
    }
}
