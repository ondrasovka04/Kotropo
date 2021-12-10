package cz.ucenislovicek;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cz.ucenislovicek.BakalariAPI.LoadBag;
import cz.ucenislovicek.BakalariAPI.Login;
import cz.ucenislovicek.BakalariAPI.SchoolsListActivity;
import cz.ucenislovicek.databinding.ActivityLoginBinding;

public class LoginForm extends AppCompatActivity {

    public static Session tunel;

    @Override
    protected void onResume() {
        super.onResume();
        ((EditText) findViewById(R.id.uzivjm)).setText(SharedPrefs.getString(this, SharedPrefs.USERNAME));
        ((EditText) findViewById(R.id.heslo)).setText(SharedPrefs.getString(this, SharedPrefs.PASSWORD));
        ((EditText) findViewById(R.id.address)).setText(SharedPrefs.getString(this, SharedPrefs.URL));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        new Thread(() -> {
            try {
                tunel = new JSch().getSession("dk-313", "db.gyarab.cz", 22);
                tunel.setPassword("GyArab14");
                tunel.setConfig("StrictHostKeyChecking", "no");
                tunel.connect();
                tunel.setPortForwardingL(3306, "localhost", 3306);
            } catch (JSchException e) {
                e.printStackTrace();
            }
        }).start();


        final Button login = binding.button;

        final Button findSchool = binding.findSchool;
        findSchool.setOnClickListener(view -> startActivity(new Intent(LoginForm.this, SchoolsListActivity.class)));

        final ProgressBar progressBar = binding.progressBar2;
        final EditText password = binding.heslo;
        final EditText username = binding.uzivjm;
        final EditText serverAddress = binding.address;

        login.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            login.setVisibility(View.INVISIBLE);
            login.setClickable(false);

            final String passwordText = password.getText().toString();
            final String usernameText = username.getText().toString();
            final String urlText = serverAddress.getText().toString();
            final String urlTextValidated = urlText.endsWith("/") ? urlText : urlText + "/";
            SharedPrefs.setString(this, SharedPrefs.USERNAME, usernameText);
            SharedPrefs.setString(this, SharedPrefs.PASSWORD, passwordText);
            SharedPrefs.setString(this, SharedPrefs.URL, urlText);

            Login loginLogic = new Login(this);

            loginLogic.getLogin().login(urlTextValidated, usernameText, passwordText, (code) -> {
                if (code == Login.SUCCESS) {

                    LoadBag loadBag = new LoadBag(this, this);
                    loadBag.getRozvrh(Integer.MAX_VALUE);

                    new checkFirstLogin().execute();
                    startActivity(new Intent(LoginForm.this, MainActivity.class));
                    finish();
                    return;
                }
                login.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                if (code == Login.WRONG_LOGIN) {
                    resetButton(progressBar, login);
                    username.setError("Špatně zadané uživatelské jméno nebo heslo.");
                    password.setError("Špatně zadané uživatelské jméno nebo heslo.");
                }
                if (code == Login.SERVER_UNREACHABLE) {
                    resetButton(progressBar, login);
                    serverAddress.setError("Nelze se spojit se serverem. Zkontrolujte školní adresu.");
                }
                if (code == Login.UNEXPECTER_RESPONSE) {
                    resetButton(progressBar, login);
                    serverAddress.setError("Něco se pokazilo.");
                }
                if (code == Login.ROZVRH_DISABLED) {
                    resetButton(progressBar, login);
                    serverAddress.setError("Služby, které tato aplikace potřebuje nejsou k dispozici.");

                }
            });
        });


    }

    public void resetButton(ProgressBar progressBar, Button login) {
        progressBar.setVisibility(View.INVISIBLE);
        login.setVisibility(View.VISIBLE);
        login.setClickable(true);
    }

    private class checkFirstLogin extends AsyncTask<Void, Void, Void> {

        int result = 0;

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select count(*) from uzivatele where uzivjm=?");
                st.setString(1, SharedPrefs.getString(getApplicationContext(), SharedPrefs.USERNAME));
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    result = rs.getInt(1);

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (result == 1) {
                if (SharedPrefs.getInt(getApplicationContext(), SharedPrefs.UZIVID) == -1) {
                    new getUzivId().execute();
                }
            } else {
                new registrate().execute();
            }
        }
    }

    private class registrate extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("insert into uzivatele (uzivjm, encrHeslo, plainHeslo, typ) values (?, ?, ?, ?)");
                st.setString(1, SharedPrefs.getString(getApplicationContext(), SharedPrefs.USERNAME));
                st.setString(2, encryptPassword(SharedPrefs.getString(getApplicationContext(), SharedPrefs.PASSWORD)));
                st.setString(3, SharedPrefs.getString(getApplicationContext(), SharedPrefs.PASSWORD));
                st.setString(4, SharedPrefs.getString(getApplicationContext(), SharedPrefs.TYPE));
                st.executeUpdate();
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }

        private String encryptPassword(String plainPassword) {
            try {
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(plainPassword.getBytes());
                StringBuilder s = new StringBuilder();
                for (byte a : m.digest()) {
                    s.append(Integer.toString((a & 0xff) + 0x100, 16).substring(1));
                }
                return s.toString();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    private class getUzivId extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313_uceniSlovicek?serverTimezone=Europe/Prague", "dk-313", "GyArab14");
                PreparedStatement st = con.prepareStatement("select uzivid from uzivatele where uzivjm=?");
                st.setString(1, SharedPrefs.getString(getApplicationContext(), SharedPrefs.USERNAME));
                ResultSet rs = st.executeQuery();
                if (rs.next()) {
                    SharedPrefs.setInt(getApplicationContext(), SharedPrefs.UZIVID, rs.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
