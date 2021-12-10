package cz.ucenislovicek.BakalariAPI.schoolsDatabase;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.atomic.AtomicInteger;


public class SchoolsDatabaseAPI {
    public static final String TAG = SchoolsDatabaseAPI.class.getSimpleName();

    public static final String SCHOOLS_DATABASE_URL = "https://sluzby.bakalari.cz/api/v1/municipality/"; //includes the last /


    /**
     * Fetches list of all schools from Bakaláři api by fetching schools for each letter of Czech alphabet. Might take some time. Returns false if fetching fails.
     *
     * @param database SchoolInfo data will be saved into this database.
     * @param progressBar progress is displayed onto this progressbar unless it is {@code null}.
     * @return RequestQueue used for requests.
     */
    public static RequestQueue getAllSchools(Context context, Listener listener, SchoolsDatabase database, ProgressBar progressBar) {
        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        AsyncTask.execute(() -> {
            SchoolDAO dao = database.schoolDAO();

            int schoolsInDatabase = dao.countAllSchools();
            if (schoolsInDatabase > 0) {
                //data already queried before (is wiped on exit)
                new Handler(Looper.getMainLooper()).post(() ->
                listener.onFinished(true));
                return;
            }

            String[] CZchars = {"a", "á", "b", "c", "č", "d", "ď", "e", "é", "ě", "f", "g", "h", "ch", "i", "í", "j", "k", "l", "m", "n", "ň", "o", "ó", "p", "q", "r", "ř", "s", "š", "t", "ť", "u", "ú", "ů", "v", "w", "x", "y", "ý", "z", "ž"};

            int start = CZchars.length;
            AtomicInteger requestsCounter = new AtomicInteger(start);

            new Handler(Looper.getMainLooper()).post(() -> {
                if (progressBar != null) {
                    progressBar.setMax(start);
                    progressBar.setProgress(0);
                }
            });

            for (final String s : CZchars) {
                String url = SCHOOLS_DATABASE_URL + URLEncoder.encode(s);
                try {
                    url = SCHOOLS_DATABASE_URL + URLEncoder.encode(s, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                final String furl = url;

                SchoolRequest request = new SchoolRequest(furl, dao, response -> {

                    decrement(requestsCounter, listener, dao, start, progressBar);
                }, error -> {
                    if (error != null && (error.networkResponse == null || error.networkResponse.statusCode != 404)) {
                        error.printStackTrace();
                    }
                    decrement(requestsCounter, listener, dao, start, progressBar);

                });

                requestQueue.add(request);

            }
        });

        return requestQueue;
    }



    public interface Listener {
        void onFinished(boolean success);
    }

    private static void decrement(AtomicInteger i, Listener listener, SchoolDAO dao, int start, ProgressBar progressBar) {
        int got = i.decrementAndGet();
        if (progressBar != null) {
            new Handler(Looper.getMainLooper()).post(() ->{
                if (Build.VERSION.SDK_INT >= 24) {
                    progressBar.setProgress(start - got, true);
                }else {
                    progressBar.setProgress(start - got);
                }
            });

        }
        if (got == 0) {
            AsyncTask.execute(() -> {
                if (dao.countAllSchools() > 0){
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onFinished(true));
                }else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            listener.onFinished(false));
                }
            });
        }

    }
}
