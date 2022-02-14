package cz.ucenislovicek.drawer_items.test;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cz.ucenislovicek.R;
import cz.ucenislovicek.databinding.ActivityTestBinding;

public class Test extends AppCompatActivity {

    private TextView header, czech;
    private EditText foreign;
    private Button check;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTestBinding binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        header = binding.header;
        czech = binding.czech;
        foreign = binding.foreign;
        check = binding.check;

        foreign.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                check.performClick();
                return true;
            }
            return false;
        });

        Intent i = getIntent();
        HashMap<String, String> vocabulary = (HashMap<String, String>) i.getSerializableExtra("map");
        test(vocabulary);
    }

    @SuppressLint("SetTextI18n")
    private void test(HashMap<String, String> vocabulary) {
        ArrayList<String> vocabularyForeign = new ArrayList<>(vocabulary.keySet());
        ArrayList<String> vocabularyCzech = new ArrayList<>(vocabulary.values());
        AtomicInteger correct = new AtomicInteger();
        final int vocabularyCount = vocabularyCzech.size();

        czech.setText(vocabularyCzech.get(0));

        check.setOnClickListener(view -> {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View inflatedLayoutView = layoutInflater.inflate(R.layout.popup_test, null);
            inflatedLayoutView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));

            PopupWindow popup = new PopupWindow(inflatedLayoutView);

            popup.setWidth(700);
            popup.setHeight(700);
            popup.setFocusable(false);
            popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            popup.setOutsideTouchable(false);


            if (foreign.getText().toString().equals(vocabularyForeign.get(0))) {
                correct.getAndIncrement();
                Button next = inflatedLayoutView.findViewById(R.id.dalsi);
                next.setOnClickListener(view12 -> {
                    vocabularyForeign.remove(0);
                    vocabularyCzech.remove(0);
                    if (!vocabularyCzech.isEmpty()) {
                        czech.setText(vocabularyCzech.get(0));
                        foreign.setText("");
                    } else {
                        popup.dismiss();
                        showResults(view, vocabularyCount, correct.get());
                    }
                    popup.dismiss();
                });
            } else {
                ConstraintLayout layout = inflatedLayoutView.findViewById(R.id.pokus);
                layout.setBackgroundColor(Color.rgb(211, 11, 11));

                TextView header = inflatedLayoutView.findViewById(R.id.textView5);
                header.setText("ŠPATNĚ!");

                TextView correctAnswer = inflatedLayoutView.findViewById(R.id.textView7);
                correctAnswer.setText("Spravná odpověď byla: " + vocabularyForeign.get(0));

                Button next = inflatedLayoutView.findViewById(R.id.dalsi);
                next.setOnClickListener(view1 -> {
                    int a = (int) ((Math.random() * (6 - 2)) + 2); //náhodné číslo <2;5>
                    while (vocabularyCzech.size() <= a) {
                        a--;
                    }
                    if (a != 0) {
                        if (a != 1) {
                            vocabularyForeign.add(a, vocabularyForeign.get(0));
                            vocabularyCzech.add(a, vocabularyCzech.get(0));
                        }
                        vocabularyForeign.remove(0);
                        vocabularyCzech.remove(0);

                        czech.setText(vocabularyCzech.get(0));
                        foreign.setText("");
                    } else {
                        popup.dismiss();
                        showResults(view, vocabularyCount, correct.get());
                    }
                    popup.dismiss();
                });
            }
        });
    }

    public void showResults(View view, double vocabularyCount, double correct) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View inflatedLayoutViewResults = layoutInflater.inflate(R.layout.popup_test_end, null);
        inflatedLayoutViewResults.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));
        PopupWindow results = new PopupWindow(inflatedLayoutViewResults);

        results.setWidth(700);
        results.setHeight(700);
        results.setFocusable(true);
        results.showAtLocation(view, Gravity.CENTER, 0, 0);
        results.setOutsideTouchable(false);
        TextView mark = inflatedLayoutViewResults.findViewById(R.id.znamka);
        TextView tv2 = inflatedLayoutViewResults.findViewById(R.id.textView8);
        TextView tv1 = inflatedLayoutViewResults.findViewById(R.id.textView9);
        ConstraintLayout cl = inflatedLayoutViewResults.findViewById(R.id.pozadi);
        Button but = inflatedLayoutViewResults.findViewById(R.id.button4);
        long i = Math.round(correct / vocabularyCount * 100);
        if (i < 33) {
            mark.setText("5");
            cl.setBackgroundColor(Color.rgb(130, 0, 0));
            mark.setTextColor(Color.WHITE);
            tv1.setTextColor(Color.WHITE);
            tv2.setTextColor(Color.WHITE);
        } else if (i <= 51) {
            mark.setText("4");
            cl.setBackgroundColor(Color.rgb(186, 63, 35));
            mark.setTextColor(Color.WHITE);
            tv1.setTextColor(Color.WHITE);
            tv2.setTextColor(Color.WHITE);
        } else if (i <= 68) {
            mark.setText("3");
            cl.setBackgroundColor(Color.rgb(255, 131, 71));
        } else if (i <= 84) {
            mark.setText("2");
            cl.setBackgroundColor(Color.rgb(203, 224, 111));
        } else {
            mark.setText("1");
            cl.setBackgroundColor(Color.rgb(152, 251, 152));
        }

        but.setOnClickListener(v -> {
            results.dismiss();
            onBackPressed();
        });
    }
}