package cz.ucenislovicek;

import android.content.Intent;
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

import cz.ucenislovicek.BakalariAPI.Login;
import cz.ucenislovicek.BakalariAPI.rozvrh.items.Rozvrh;
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
        /*try {
            tunel = new JSch().getSession("dk-313", "db.gyarab.cz", 22);
            tunel.setPassword("GyArab14");
            tunel.setConfig("StrictHostKeyChecking", "no");
            tunel.connect();
            tunel.setPortForwardingL(3306, "localhost", 3306);
        } catch (JSchException e) {
            e.printStackTrace();
        }*/

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

            // save user input
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
                    Rozvrh rozvrh = loadBag.rozvrh;


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


    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        tunel.disconnect();
    }*/
}
