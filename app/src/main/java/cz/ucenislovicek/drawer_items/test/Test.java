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
import java.util.concurrent.atomic.AtomicInteger;

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
        AtomicInteger spravne = new AtomicInteger();
        final int pocetSlovicek = slovickaCZ.size();

        cesky.setText(slovickaCZ.get(0));

        over.setOnClickListener(view -> {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View inflatedLayoutView = layoutInflater.inflate(R.layout.popup_test, null);
            inflatedLayoutView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));

            PopupWindow popup = new PopupWindow(inflatedLayoutView);

            popup.setWidth(700);
            popup.setHeight(700);
            popup.setFocusable(true);
            popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            popup.setOutsideTouchable(false);

            if (cizi.getText().toString().equals(slovickaCizi.get(0))) {
                spravne.getAndIncrement();
                Button dalsi = inflatedLayoutView.findViewById(R.id.dalsi);
                dalsi.setOnClickListener(view12 -> {
                    slovickaCizi.remove(0);
                    slovickaCZ.remove(0);
                    if (!slovickaCZ.isEmpty()) {
                        cesky.setText(slovickaCZ.get(0));
                        cizi.setText("");
                    } else {
                        popup.dismiss();
                        showResults(view, pocetSlovicek, spravne.get());
                    }
                    popup.dismiss();
                });
            } else {
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
                        popup.dismiss();
                        showResults(view, pocetSlovicek, spravne.get());
                    }
                    popup.dismiss();
                });
            }
        });
    }

    public void showResults(View view, int pocetSlovicek, int spravne) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View inflatedLayoutViewResults = layoutInflater.inflate(R.layout.popup_test_end, null);
        inflatedLayoutViewResults.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));
        PopupWindow results = new PopupWindow(inflatedLayoutViewResults);

        results.setWidth(700);
        results.setHeight(700);
        results.setFocusable(true);
        results.showAtLocation(view, Gravity.CENTER, 0, 0);
        results.setOutsideTouchable(false);
        TextView znamka = inflatedLayoutViewResults.findViewById(R.id.znamka);
        TextView tv2 = inflatedLayoutViewResults.findViewById(R.id.textView8);
        TextView tv1 = inflatedLayoutViewResults.findViewById(R.id.textView9);
        ConstraintLayout cl = inflatedLayoutViewResults.findViewById(R.id.pozadi);
        Button but = inflatedLayoutViewResults.findViewById(R.id.button4);
        long i = Math.round(spravne / pocetSlovicek * 100);
        if (i < 33) {
            znamka.setText("5");
            cl.setBackgroundColor(Color.rgb(130, 0, 0));
            znamka.setTextColor(Color.WHITE);
            tv1.setTextColor(Color.WHITE);
            tv2.setTextColor(Color.WHITE);
        } else if (i <= 51) {
            znamka.setText("4");
            cl.setBackgroundColor(Color.rgb(186, 63, 35));
            znamka.setTextColor(Color.WHITE);
            tv1.setTextColor(Color.WHITE);
            tv2.setTextColor(Color.WHITE);
        } else if (i <= 68) {
            znamka.setText("3");
            cl.setBackgroundColor(Color.rgb(255, 131, 71));
        } else if (i <= 84) {
            znamka.setText("2");
            cl.setBackgroundColor(Color.rgb(203, 224, 111));
        } else {
            znamka.setText("1");
            cl.setBackgroundColor(Color.rgb(152, 251, 152));
        }

        but.setOnClickListener(v -> {
            results.dismiss();
            onBackPressed();
        });
    }
}