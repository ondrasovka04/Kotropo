package cz.ucenislovicek.drawer_items;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cz.ucenislovicek.MainActivity;
import cz.ucenislovicek.databinding.ActivityLoginBinding;

public class LoginForm extends AppCompatActivity {

    public static Session tunel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        try {
            tunel = new JSch().getSession("dk-313", "db.gyarab.cz", 22);
            tunel.setPassword("GyArab14");
            tunel.setConfig("StrictHostKeyChecking", "no");
            tunel.connect();
            tunel.setPortForwardingL(3306, "localhost", 3306);
        } catch (JSchException e) {
            e.printStackTrace();
        }

        final Button but = binding.button;
        but.setOnClickListener(view -> {
            System.out.println("ahooj");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });


        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        tunel.disconnect();
    }
}
