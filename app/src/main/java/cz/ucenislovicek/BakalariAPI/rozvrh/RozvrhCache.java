// taken from https://github.com/vitSkalicky/lepsi-rozvrh/
package cz.ucenislovicek.BakalariAPI.rozvrh;

import static cz.ucenislovicek.BakalariAPI.rozvrh.ResponseCode.*;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.nio.channels.FileLock;

import cz.ucenislovicek.BakalariAPI.rozvrh.items.RozvrhRoot;
import cz.ucenislovicek.BakalariAPI.rozvrh.rozvrh3.Rozvrh3;
import cz.ucenislovicek.BakalariAPI.rozvrh.rozvrh3.RozvrhConverter;


public class RozvrhCache {
    public static final String TAG = RozvrhCache.class.getSimpleName();

    /**
     * Saved rozvrh for later faster loading. Saving is performed on background thread and file
     * writing is thread-safe.
     *
     * @param monday monday for week identification, leave null for permanent timetable
     * @param rozvrh string containing timetable xml
     */
    public static void saveRawRozvrh(LocalDate monday, String rozvrh, Context context) {
        AsyncTask.execute(() -> {
            LocalDate sureMonday = null;
            if (monday != null)
                sureMonday = Utils.getWeekMonday(monday); //just to be extra sure

            String filename;
            if (sureMonday == null) {
                filename = "rozvrh-perm.xml";
            } else {
                filename = "rozvrh-" + Utils.dateToString(sureMonday) + ".xml";
            }


            try (FileOutputStream outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                 FileLock lock = outputStream.getChannel().lock()) {

                outputStream.write(rozvrh.getBytes());

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    public static void loadRozvrh(LocalDate monday, RozvrhListener listener, Context context) {
        new Thread(() -> {
            LocalDate sureMonday = null;
            if (monday != null)
                sureMonday = Utils.getWeekMonday(monday); //just to be extra sure

            String filename;
            if (sureMonday == null) {
                filename = "rozvrh-perm.xml";
            } else {
                filename = "rozvrh-" + Utils.dateToString(sureMonday) + ".xml";
            }

            RozvrhRoot root;
            try (FileInputStream inputStream = context.openFileInput(filename)) {

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

                root = RozvrhConverter.convert(objectMapper.readValue(inputStream, Rozvrh3.class), monday == null,context);
                root.checkDemoMode(context);

            } catch (FileNotFoundException e) {
                if (sureMonday != null)
                    System.out.println("Timetable for week " + Utils.dateToString(sureMonday) + " not found.");
                else
                    System.out.println("Timetable for week " + "perm" + " not found.");

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.method(new RozvrhWrapper(null, NO_CACHE, RozvrhWrapper.SOURCE_CACHE)));
                return;
            } catch (Exception e) {
                e.printStackTrace();

                new Handler(Looper.getMainLooper()).post(() ->
                        listener.method(new RozvrhWrapper(null, NO_CACHE, RozvrhWrapper.SOURCE_CACHE)));
                return;
            }
            new Handler(Looper.getMainLooper()).post(() ->
                    listener.method(new RozvrhWrapper(root.getRozvrh(), SUCCESS, RozvrhWrapper.SOURCE_CACHE)));
        }).start();

    }

    /**
     * Deletes Rozvrhs saved in 'cache' which are older than month. Operations are run on background.
     * Should be called on every app exit (or just time by time).
     */
    public static void clearOldCache(Context context) {
        AsyncTask.execute(() -> {

            File dir = context.getFilesDir();

            LocalDate deleteBefore = LocalDate.now().minusMonths(1);

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File fileDir, String name) {
                    if (fileDir == dir && name.length() > 11) {
                        String date = name.substring(7, name.length() - 4);

                        if (date.equals("perm")) return false;

                        LocalDate fileDate;
                        try {
                            fileDate = Utils.parseDate(date);
                        } catch (IllegalArgumentException e) {
                            return false;
                        }

                        if (fileDate.isBefore(deleteBefore)) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    return false;
                }
            };

            String[] fileNames = dir.list(filter);

            for (String item : fileNames) {
                context.deleteFile(item);
            }
        });
    }

    /**
     * Deletes all rovrhs save din cache. Operations are run on background.
     */
    public static void clearCache(Context context) {
        AsyncTask.execute(() -> {

            File dir = context.getFilesDir();

            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File fileDir, String name) {
                    if (fileDir == dir) {
                        if (name.equals("rozvrh-perm.xml")) return true;
                        else return name.matches("rozvrh-[0-9]{8}\\.xml");
                    }
                    return false;
                }
            };

            String[] fileNames = dir.list(filter);

            for (String item : fileNames) {
                context.deleteFile(item);
            }
        });
    }
}
