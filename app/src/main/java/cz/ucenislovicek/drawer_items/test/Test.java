package cz.ucenislovicek.drawer_items.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;

import cz.ucenislovicek.MainActivity;
import cz.ucenislovicek.R;
import cz.ucenislovicek.databinding.ActivityTestBinding;

public class Test extends AppCompatActivity {

    TextView nadpis, cesky;
    EditText cizi;
    Button over;


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTestBinding binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        nadpis = binding.textView6;
        cesky = binding.cesky;
        cizi = binding.cizi;
        over = binding.button3;


        Intent i = getIntent();
        HashMap<String, String> slovicka = (HashMap<String, String>) i.getSerializableExtra("mapa");
        testuj(slovicka);
    }

    private void testuj(HashMap<String, String> slovicka) {
        ArrayList<String> slovickaCizi = new ArrayList<>(slovicka.keySet());
        ArrayList<String> slovickaCZ = new ArrayList<>(slovicka.values());

        cesky.setText(slovickaCZ.get(0));

        over.setOnClickListener(view -> {
            System.out.println(slovickaCizi);
            System.out.println(slovickaCZ);
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View inflatedLayoutView = layoutInflater.inflate(R.layout.popup_test, null);
            inflatedLayoutView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));

            if (cizi.getText().toString().equals(slovickaCizi.get(0))) {
                PopupWindow popup = new PopupWindow(inflatedLayoutView);

                popup.setWidth(700);
                popup.setHeight(700);
                popup.setFocusable(true);

                Button dalsi = inflatedLayoutView.findViewById(R.id.dalsi);
                dalsi.setOnClickListener(view12 -> {
                    slovickaCizi.remove(0);
                    slovickaCZ.remove(0);
                    if (!slovickaCZ.isEmpty()) {
                        cesky.setText(slovickaCZ.get(0));
                        cizi.setText("");
                    } else {
                        //konec testu
                        //TODO: 19.12.2021 obrazovka výsledků a odtamtud vrácení se zpět do main activity
                        Toast.makeText(getApplicationContext(), "Konec testu!", Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }
                    popup.dismiss();
                });

                popup.showAtLocation(view, Gravity.CENTER, 0, 0);

                popup.setOutsideTouchable(false);
            } else {
                PopupWindow popup = new PopupWindow(inflatedLayoutView);

                popup.setWidth(700);
                popup.setHeight(700);
                popup.setFocusable(true);

                ConstraintLayout layout = inflatedLayoutView.findViewById(R.id.pokus);
                layout.setBackgroundColor(Color.rgb(211, 11, 11));

                TextView nadpis = inflatedLayoutView.findViewById(R.id.textView5);
                nadpis.setText("ŠPATNĚ!");

                TextView spravnaOdpoved = inflatedLayoutView.findViewById(R.id.textView7);
                spravnaOdpoved.setText("Spravná odpověď byla: " + slovickaCizi.get(0));

                Button dalsi = inflatedLayoutView.findViewById(R.id.dalsi);
                dalsi.setOnClickListener(view1 -> {
                    int a = (int) ((Math.random() * (6 - 2)) + 2); //náhodné číslo <2;5>
                    while (slovickaCZ.size() <= a) {
                        a--;
                    }
                    if (a != 0) {
                        if (a != 1) {
                            slovickaCizi.add(a, slovickaCizi.get(0));
                            slovickaCZ.add(a, slovickaCZ.get(0));
                        }
                        slovickaCizi.remove(0);
                        slovickaCZ.remove(0);

                        cesky.setText(slovickaCZ.get(0));
                        cizi.setText("");
                    } else {
                        //konec testu
                        //TODO: 19.12.2021 obrazovka výsledků a odtamtud vrácení se zpět do main activity
                        Toast.makeText(getApplicationContext(), "Konec testu!", Toast.LENGTH_SHORT).show();
                        //onBackPressed();
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }

                    popup.dismiss();
                });

                popup.showAtLocation(view, Gravity.CENTER, 0, 0);

                popup.setOutsideTouchable(false);
            }
        });
    }
}