package cz.ucenislovicek.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.ucenislovicek.MainActivity;
import cz.ucenislovicek.SharedPrefs;

/**
 * @author Ondra
 */
public class BakaAPI {

    public static String URLEncode(String text) {
        if (text.contains(".")) {
            text = text.substring(0, text.indexOf("."));
        }
        return text.replace("ě", "%C4%9B").replace("š", "%C5%A1").replace("č", "%C4%8D").replace("ř", "%C5%99").replace("ž", "%C5%BE").replace("ý", "%C3%BD").replace("á", "%C3%A1").replace("í", "%C3%AD").replace("é", "%C3%A9").replace("ď", "%C4%8F").replace("ť", "%C5%A5").replace("ň", "%C5%88").replace("ú", "%C3%BA").replace("ů", "%C5%AF").replace("Š", "%C5%A0").replace("Č", "%C4%8C").replace("Ř", "%C5%98").replace("Ž", "%C5%BD").replace("Á", "%C3%81").replace("Ú", "%C3%9A").replace(" ", "%20").replace("ü", "%C3%BC");
    }

    public static class getSkupiny extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;
        String error = "";

        public getSkupiny(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL(SharedPrefs.getString(context, SharedPrefs.URL) + "api/3/timetable/permanent");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setRequestProperty("Authorization", "Bearer " + SharedPrefs.getString(context, SharedPrefs.TOKEN));

                if (con.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONArray myArray = new JSONObject(content.toString()).getJSONArray("Groups");
                    String[] skupiny = new String[2];
                    for (int i = 0; i < myArray.length(); i++) {
                        String name = myArray.getJSONObject(i).getString("Name");
                        if (name.contains("Anglický jazyk") && skupiny[0] == null) {
                            String s = myArray.getJSONObject(i).getString("Abbrev");
                            skupiny[0] = s.substring(s.length() - 2);
                        }
                        if (name.contains("Německý jazyk") && skupiny[1] == null) {
                            String s = myArray.getJSONObject(i).getString("Abbrev");
                            skupiny[1] = s.substring(s.length() - 2);
                        }
                        if (skupiny[0] != null && skupiny[1] != null) {
                            break;
                        }
                    }
                    SharedPrefs.setString(context, SharedPrefs.SKUPINA_AJ, skupiny[0]);
                    SharedPrefs.setString(context, SharedPrefs.SKUPINA_NJ, skupiny[1]);
                } else {
                    error = "Něco se pokazilo";
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (error.equals("")) {
                Intent i = new Intent(context, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            } else {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public static class getToken extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;
        String error = "";

        public getToken(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL(SharedPrefs.getString(context, SharedPrefs.URL) + "api/login");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setDoOutput(true);
                con.getOutputStream().write(("client_id=ANDR&grant_type=password&username=" + SharedPrefs.getString(context, SharedPrefs.USERNAME) + "&password=" + SharedPrefs.getString(context, SharedPrefs.PASSWORD)).getBytes("UTF-8"));

                if (con.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    JSONObject myJson = new JSONObject(content.toString());

                    SharedPrefs.setString(context, SharedPrefs.TOKEN, (String) myJson.get("access_token"));
                } else {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    JSONObject myJson = new JSONObject(content.toString());

                    if (((String) myJson.get("error_description")).equals("Špatný login nebo heslo")) {
                        error = "Špatný login nebo heslo";
                    } else {
                        error = "Něco se pokazilo";
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (error.equals("")) {
                new getSkupiny(context).execute();
            } else {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
            }
        }
    }

}

