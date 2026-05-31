package me.karthikeyang.handcricket;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiClient {
    public interface JsonCallback {
        void onSuccess(String body);
        void onError(Exception error);
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void get(String path, JsonCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(baseUrl + path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    postSuccess(callback, sb.toString());
                } else {
                    throw new Exception("HTTP " + code + ": " + sb);
                }
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    public void post(String path, JSONObject payload, JsonCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(baseUrl + path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                OutputStream os = conn.getOutputStream();
                os.write(payload.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
                br.close();

                if (code >= 200 && code < 300) {
                    postSuccess(callback, sb.toString());
                } else {
                    throw new Exception("HTTP " + code + ": " + sb);
                }
            } catch (Exception e) {
                postError(callback, e);
            }
        });
    }

    private void postSuccess(JsonCallback callback, String body) {
        mainHandler.post(() -> callback.onSuccess(body));
    }

    private void postError(JsonCallback callback, Exception error) {
        mainHandler.post(() -> callback.onError(error));
    }

    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }
}
