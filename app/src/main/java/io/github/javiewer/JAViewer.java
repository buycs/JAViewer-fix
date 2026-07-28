package io.github.javiewer;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jzvd.JZVideoPlayer;
import io.github.javiewer.adapter.item.DataSource;
import io.github.javiewer.fragment.ActressesFragment;
import io.github.javiewer.fragment.HomeFragment;
import io.github.javiewer.fragment.PopularFragment;
import io.github.javiewer.fragment.ReleasedFragment;
import io.github.javiewer.fragment.favourite.FavouriteTabsFragment;
import io.github.javiewer.fragment.genre.GenreTabsFragment;
import io.github.javiewer.network.BasicService;
import io.github.javiewer.util.ExoPlayerImpl;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class JAViewer extends Application {

    private static Context appContext;

    public static Context getAppContext() {
        return appContext;
    }

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    public static final List<DataSource> DATA_SOURCES = new ArrayList<>();
    public static final Map<Integer, Class<? extends Fragment>> FRAGMENTS = new HashMap<Integer, Class<? extends Fragment>>() {{
        put(R.id.nav_home, HomeFragment.class);
        put(R.id.nav_popular, PopularFragment.class);
        put(R.id.nav_released, ReleasedFragment.class);
        put(R.id.nav_actresses, ActressesFragment.class);
        put(R.id.nav_genre, GenreTabsFragment.class);
        put(R.id.nav_favourite, FavouriteTabsFragment.class);
    }};
    public static Configurations CONFIGURATIONS;
    public static BasicService SERVICE;
    public static Map<String, String> hostReplacements = new HashMap<>();

    public static final CookieJar COOKIE_JAR = new CookieJar() {
        private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
            String host = url.host();
            List<Cookie> existing = cookieStore.get(host);
            if (existing == null) {
                existing = new ArrayList<>();
                cookieStore.put(host, existing);
            }
            for (Cookie newCookie : cookies) {
                boolean replaced = false;
                for (int i = 0; i < existing.size(); i++) {
                    if (existing.get(i).name().equals(newCookie.name())) {
                        existing.set(i, newCookie);
                        replaced = true;
                        break;
                    }
                }
                if (!replaced) {
                    existing.add(newCookie);
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<Cookie>();
        }
    };

    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            Request original = chain.request();

            Request.Builder builder = original.newBuilder()
                    .url(replaceUrl(original.url()))
                    .header("User-Agent", USER_AGENT);

            String host = original.url().host();
            if (!host.contains("torrentkitty") && !host.contains("btsearch")) {
                builder.header("X-Requested-With", "XMLHttpRequest");
            }

            Request request = builder.build();

            android.util.Log.d("JAViewer", "Request URL: " + original.url());
            android.util.Log.d("JAViewer", "Final URL: " + request.url());
            return chain.proceed(request);
        }
    })
            .cookieJar(COOKIE_JAR)
            .build();

    static {
        JZVideoPlayer.setMediaInterface(new ExoPlayerImpl());
    }

    public static DataSource getDataSource() {
        return JAViewer.CONFIGURATIONS.getDataSource();
    }

    public static void recreateService() {
        try {
            android.util.Log.d("JAViewer", "recreateService: " + JAViewer.getDataSource().getLink());
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(JAViewer.getDataSource().getLink())
                    .client(JAViewer.HTTP_CLIENT)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            SERVICE = retrofit.create(BasicService.class);
            android.util.Log.d("JAViewer", "recreateService: SUCCESS");
        } catch (Exception e) {
            android.util.Log.e("JAViewer", "recreateService: FAILED", e);
        }
    }

    public static BasicService getService() {
        if (SERVICE == null) {
            recreateService();
        }
        return SERVICE;
    }

    public static File getStorageDir() {
        File dir = new File(appContext.getExternalFilesDir(null), "JAViewer/");
        dir.mkdirs();
        return dir;
    }

    public static HttpUrl replaceUrl(HttpUrl url) {
        HttpUrl.Builder builder = url.newBuilder();
        String host = url.url().getHost();
        if (hostReplacements.containsKey(host)) {
            builder.host(hostReplacements.get(host));
            return builder.build();
        }
        return url;
    }

    public static <T> T parseJson(Class<T> beanClass, JsonReader reader) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(reader, beanClass);
    }

    public static <T> T parseJson(Class<T> beanClass, String json) throws JsonParseException {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        return gson.fromJson(json, beanClass);
    }

    public static String b(String s1, String s2) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(String.format("%s%sBrynhildr", s1, s2).getBytes());
            return bytesToHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static boolean Objects_equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }
}
