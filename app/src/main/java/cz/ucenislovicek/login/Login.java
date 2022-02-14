package cz.ucenislovicek.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import cz.ucenislovicek.MainActivity;
import cz.ucenislovicek.R;
import cz.ucenislovicek.SharedPrefs;
import cz.ucenislovicek.databinding.ActivityLoginBinding;

public class Login extends AppCompatActivity {

    private Button login, findSchool;
    private EditText username, password, serverAddress;
    private ProgressBar progressBar;
    private ImageView imageView;
    private TextView bg1, bg2;

    @Override
    protected void onResume() {
        super.onResume();
        ((EditText) findViewById(R.id.username)).setText(SharedPrefs.getString(this, SharedPrefs.USERNAME));
        ((EditText) findViewById(R.id.password)).setText(SharedPrefs.getString(this, SharedPrefs.PASSWORD));
        ((EditText) findViewById(R.id.address)).setText(SharedPrefs.getString(this, SharedPrefs.URL));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageView = binding.bakalariLogo;
        login = binding.login;
        findSchool = binding.findSchool;

        progressBar = binding.loading;

        password = binding.password;
        username = binding.username;
        serverAddress = binding.address;

        bg1 = binding.bg1;
        bg2 = binding.bg2;

        username.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                password.setFocusable(true);
            }
            return false;
        });

        password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login.performClick();
            }
            return false;
        });

        findSchool.setOnClickListener(v -> {
            startActivity(new Intent(Login.this, ListSkol.class));
        });

        login.setOnClickListener(v -> {
            changeVisibility(false);

            final String passwordText = password.getText().toString();
            final String usernameText = username.getText().toString();
            final String urlText = serverAddress.getText().toString();
            final String urlTextValidated = urlText.endsWith("/") ? urlText : urlText + "/";
            SharedPrefs.setString(this, SharedPrefs.USERNAME, usernameText);
            SharedPrefs.setString(this, SharedPrefs.PASSWORD, passwordText);
            SharedPrefs.setString(this, SharedPrefs.URL, urlTextValidated);

            new getToken(this).execute();
        });


    }

    public void changeVisibility(boolean on) {
        if (on) {
            login.setVisibility(View.VISIBLE);
            findSchool.setVisibility(View.VISIBLE);
            username.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            serverAddress.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            bg1.setVisibility(View.VISIBLE);
            bg2.setVisibility(View.VISIBLE);

            progressBar.setVisibility(View.INVISIBLE);
        } else {
            login.setVisibility(View.INVISIBLE);
            findSchool.setVisibility(View.INVISIBLE);
            username.setVisibility(View.INVISIBLE);
            password.setVisibility(View.INVISIBLE);
            serverAddress.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            bg1.setVisibility(View.INVISIBLE);
            bg2.setVisibility(View.INVISIBLE);

            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public class getGroups extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;
        String error = "";

        public getGroups(Context context) {
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
                    String[] groups = new String[2];
                    for (int i = 0; i < myArray.length(); i++) {
                        String name = myArray.getJSONObject(i).getString("Name");
                        if (name.contains("Anglický jazyk") && groups[0] == null) {
                            String s = myArray.getJSONObject(i).getString("Abbrev");
                            groups[0] = s.substring(s.length() - 2);
                        }
                        if (name.contains("Německý jazyk") && groups[1] == null) {
                            String s = myArray.getJSONObject(i).getString("Abbrev");
                            groups[1] = s.substring(s.length() - 2);
                        }
                        if (groups[0] != null && groups[1] != null) {
                            break;
                        }
                    }
                    SharedPrefs.setString(context, SharedPrefs.SKUPINA_AJ, groups[0]);
                    SharedPrefs.setString(context, SharedPrefs.SKUPINA_NJ, groups[1]);
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
                finish();
            } else {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                changeVisibility(true);
            }

        }
    }

    public class getToken extends AsyncTask<Void, Void, Void> {
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

                    if (myJson.get("error_description").equals("Špatný login nebo heslo")) {
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
                new getGroups(context).execute();
            } else {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
                changeVisibility(true);
            }
        }
    }
}
