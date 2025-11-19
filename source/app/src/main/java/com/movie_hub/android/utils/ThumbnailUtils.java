package com.movie_hub.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ThumbnailUtils {

    public static class ThumbnailInfo {
        public long startMs; // Dùng ms cho chính xác
        public int x, y, w, h;

        public ThumbnailInfo(long startMs, int x, int y, int w, int h) {
            this.startMs = startMs;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public static Bitmap loadSpriteImage(String spriteUrl) {
        try (InputStream input = new URL(spriteUrl).openStream()) {
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<ThumbnailInfo> loadVttFile(String vttUrl) {
        List<ThumbnailInfo> thumbnails = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(vttUrl).openStream()))) {
            String line;
            // Hỗ trợ cả mm:ss.xxx và hh:mm:ss.xxx
            Pattern timePattern = Pattern.compile("(\\d{2,}:)?(\\d{2}):(\\d{2})\\.\\d+\\s-->\\s(\\d{2,}:)?(\\d{2}):(\\d{2})\\.\\d+");
            Pattern xywhPattern = Pattern.compile("#xywh=(\\d+),(\\d+),(\\d+),(\\d+)");

            long startMs = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("WEBVTT")) continue;

                Matcher timeMatcher = timePattern.matcher(line);
                if (timeMatcher.find()) {
                    startMs = parseTimeToMs(timeMatcher.group(1), timeMatcher.group(2), timeMatcher.group(3));
                    // Đọc dòng #xywh=
                    String xywhLine = reader.readLine();
                    if (xywhLine != null) {
                        Matcher xywhMatcher = xywhPattern.matcher(xywhLine);
                        if (xywhMatcher.find()) {
                            int x = Integer.parseInt(xywhMatcher.group(1));
                            int y = Integer.parseInt(xywhMatcher.group(2));
                            int w = Integer.parseInt(xywhMatcher.group(3));
                            int h = Integer.parseInt(xywhMatcher.group(4));
                            thumbnails.add(new ThumbnailInfo(startMs, x, y, w, h));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return thumbnails;
    }

    private static long parseTimeToMs(String hours, String minutes, String secondsWithMs) {
        long h = hours != null ? Long.parseLong(hours.replace(":", "")) : 0;
        long m = Long.parseLong(minutes);
        String[] secParts = secondsWithMs.split("\\.");
        long s = Long.parseLong(secParts[0]);
        long ms = secParts.length > 1 ? Long.parseLong(String.format("%-3s", secParts[1]).replace(" ", "0")) : 0;

        return (h * 3600 + m * 60 + s) * 1000 + ms;
    }


    public static ThumbnailInfo getClosestThumbnail(List<ThumbnailInfo> thumbnails, long positionMs) {
        ThumbnailInfo closest = null;
        for (ThumbnailInfo t : thumbnails) {
            if (t.startMs <= positionMs) {
                closest = t;
            } else {
                break;
            }
        }
        return closest;
    }
}