// taken from https://github.com/vitSkalicky/lepsi-rozvrh/
package cz.ucenislovicek.BakalariAPI;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.joda.time.LocalDateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Objects;

import cz.ucenislovicek.BakalariAPI.rozvrh.RozvrhCache;
import cz.ucenislovicek.SharedPrefs;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class Login {
        private static String TAG = Login.class.getSimpleName();

        private Context context;

        /**
         * Use
         * @param context
         */
        public Login(Context context) {
            this.context = context;
        }

        /**
         * ResponseListener for returning login data.
         */
        public static interface Listener{
            public void onResponse(int code);
        }

        public static final int SUCCESS = 0; // data: token
        public static final int WRONG_LOGIN = 1; // data: response
        public static final int UNEXPECTER_RESPONSE = 3; // data: response
        public static final int SERVER_UNREACHABLE = 4; // data: message
        public static final int ROZVRH_DISABLED = 5; // data: response

        /**
         * Logs in user and returns status through listener. Credentials are saved (if login successful).
         * when finished {@link Listener#onResponse(int)} is called with {@code code} being one of constants above
         * (in this class).
         * @param url Bakaláři base url (eg. https://bakalari.gpisnicka.cz/bakaweb/)
         * @param username user's username
         * @param password user's password
         * @param listener listener for returning status
         */

        public void login(String url, String username, String password, Listener listener){
            SharedPrefs.setString(context, SharedPrefs.URL, unifyUrl(url));
            Retrofit retrofit = getRetrofit();

            if (retrofit == null){
                Log.d("AAA","aa");
                listener.onResponse(SERVER_UNREACHABLE);
                return;
            }

            LoginBakalariAPI apiInterface = retrofit.create(LoginBakalariAPI.class);

            apiInterface.firstLogin("ANDR", "password", username, password).enqueue(new Callback<LoginResponse>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                    if (response.isSuccessful()){
                        String refreshToken = null;
                        if (response.body() != null) {
                            refreshToken = response.body().refresh_token;
                        }
                        String accessToken = Objects.requireNonNull(response.body()).access_token;

                        SharedPrefs.setString(context, SharedPrefs.REFRESH_TOKEN, refreshToken);
                        SharedPrefs.setString(context, SharedPrefs.ACCEESS_TOKEN, accessToken);
                        SharedPrefs.setString(context, SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.body().expires_in).toString(ISODateTimeFormat.dateTime()));

                        apiInterface.getUser().enqueue(new Callback<UserResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                                if(response.isSuccessful()){
                                    SharedPrefs.setString(context, SharedPrefs.NAME, Objects.requireNonNull(response.body()).fullName);
                                    SharedPrefs.setString(context, SharedPrefs.TYPE, response.body().UserType);
                                    listener.onResponse(SUCCESS);
                                }else {
                                    listener.onResponse(UNEXPECTER_RESPONSE);
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                                Log.e(TAG, t.toString());
                                t.printStackTrace();
                                listener.onResponse(SERVER_UNREACHABLE);
                            }
                        });
                    }else {
                        listener.onResponse(WRONG_LOGIN);

                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, t.toString());
                    t.printStackTrace();
                    listener.onResponse(SERVER_UNREACHABLE);
                }
            });
        }

        /**
         * listeners waiting for refresh response
         */
        private final ArrayList<Listener> refreshQueue = new ArrayList<>();

        /**
         * refreshes login token
         * */
        public void refreshToken(Listener listener){
            Retrofit retrofit = getRetrofit();
            if (retrofit == null){

                listener.onResponse(SERVER_UNREACHABLE);
                return;
            }

            LoginBakalariAPI apiInterface = retrofit.create(LoginBakalariAPI.class);

            refreshQueue.add(listener);

            if (refreshQueue.size() == 1){
                apiInterface.refreshLogin("ANDR", "refresh_token", SharedPrefs.getString(context, SharedPrefs.REFRESH_TOKEN)).enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                        if (response.isSuccessful()){
                            String refreshToken = Objects.requireNonNull(response.body()).refresh_token;
                            String accessToken = response.body().access_token;


                            SharedPrefs.setString(context, SharedPrefs.REFRESH_TOKEN, refreshToken);
                            SharedPrefs.setString(context, SharedPrefs.ACCEESS_TOKEN, accessToken);
                            SharedPrefs.setString(context, SharedPrefs.ACCESS_EXPIRES, LocalDateTime.now().plusSeconds(response.body().expires_in).toString(ISODateTimeFormat.dateTime()));

                            notifyRefreshQueue(SUCCESS);
                        }else {
                            notifyRefreshQueue(WRONG_LOGIN);

                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, t.toString());
                        t.printStackTrace();
                        notifyRefreshQueue(SERVER_UNREACHABLE);
                    }
                });
            }
        }

        private void notifyRefreshQueue(int code){
            ArrayList<Listener> copy = new ArrayList<>(refreshQueue);
            refreshQueue.clear();

            for (Listener item :copy) {
                item.onResponse(code);
            }
        }

        public void refreshTokenIfNeeded(Listener listener){
            if (getAccessToken(context).isEmpty()){
                refreshToken(listener);
            }
        }

        /**
         * Returns a valid access token or an empty string.
         */
        public String getAccessToken(Context context){
            String expiresStr = SharedPrefs.getString(context, SharedPrefs.ACCESS_EXPIRES);
            if (expiresStr.isEmpty())
                return "";
            LocalDateTime expires = LocalDateTime.parse(expiresStr, ISODateTimeFormat.dateTimeParser());
            if (expires.isBefore(LocalDateTime.now()))
                return "";
            return SharedPrefs.getString(context, SharedPrefs.ACCEESS_TOKEN);
        }

        /**
         * Logs out user (deletes credentials)
         */
        public void logout() {
            SharedPrefs.remove(context, SharedPrefs.USERNAME);
            SharedPrefs.remove(context, SharedPrefs.REFRESH_TOKEN);
            SharedPrefs.remove(context, SharedPrefs.ACCEESS_TOKEN);
            SharedPrefs.remove(context, SharedPrefs.ACCESS_EXPIRES);
            SharedPrefs.remove(context, SharedPrefs.URL);
            SharedPrefs.remove(context, SharedPrefs.NAME);
            SharedPrefs.remove(context, SharedPrefs.PASSWORD);
            RozvrhCache.clearCache(context);
        }

        /**
         * Whether to show teacher's or students rozvrh (each is fetched and displayed slightly differently)
         * @return {@code true} if the user logged in is a teacher or {@code false} if not (then it is a student or a parent)
         */
        public boolean isTeacher(){
            String type = SharedPrefs.getString(context, SharedPrefs.TYPE);
            return type.equals("teacher");
        }

        public boolean isLoggedIn(){
            return !SharedPrefs.getString(context, SharedPrefs.REFRESH_TOKEN).isEmpty();
        }

        /**
         * Removes /next/login.aspx
         */
        private String unifyUrl(String url){
            if (url.endsWith(".aspx"))
                url = url.substring(0, url.length() - 5);
            if (url.endsWith("login")){
                url = url.substring(0, url.length() - 5);
                if (url.endsWith("next/"))
                    url = url.substring(0, url.length() - 5);
            }
            if (!url.endsWith("/"))
                url += "/";
            if (!(url.startsWith("http://") || url.startsWith("https://"))){
                url = "https://" + url;
            }
            return url;
        }


    private Login login = null;
    public Login getLogin() {
        if (login == null){
            login = new Login(context);
        }
        return login;
    }

    public Retrofit getRetrofit() {
       Log.d("AAA",Boolean.toString(SharedPrefs.contains(context,  SharedPrefs.URL)));
        if (SharedPrefs.contains(context,  SharedPrefs.URL)) {

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            Interceptor loginInterceptor = chain -> {
                if (!getLogin().getAccessToken(context).isEmpty()) {
                    Request newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + getLogin().getAccessToken(context))
                            .build();
                    return chain.proceed(newRequest);
                }
                return chain.proceed(chain.request());
            };

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).addInterceptor(loginInterceptor).build();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

            Retrofit retrofit;
            try {
                retrofit = new Retrofit.Builder()
                        .baseUrl(SharedPrefs.getString(context, SharedPrefs.URL))
                        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                        .client(client)
                        .build();
            } catch (IllegalArgumentException e) {
                return null;
            }

            return retrofit;
        }

        return null;
    }
}


