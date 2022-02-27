package cz.kotropo.drawer_items.test;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import cz.kotropo.R;
import cz.kotropo.databinding.ActivityTestBinding;


public class Test extends AppCompatActivity {

    private TextView czech;
    private EditText foreign;
    private Button check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTestBinding binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle("Ukončení").setMessage("Opravdu chcete test ukončit?").setPositiveButton("Ano", (dialog, which) -> finish()).setNegativeButton("Ne", null).show();
    }

    @SuppressLint("SetTextI18n")
    private void test(HashMap<String, String> vocabulary) {
        ArrayList<String> vocabularyForeign = new ArrayList<>(vocabulary.keySet());
        ArrayList<String> vocabularyCzech = new ArrayList<>(vocabulary.values());
        ArrayList<String> answers = new ArrayList<>();
        AtomicReference<Double> correct = new AtomicReference<>(0.0);
        AtomicBoolean secondChance = new AtomicBoolean(true);
        final int vocabularyCount = vocabularyCzech.size();

        czech.setText(vocabularyCzech.get(0));

        check.setOnClickListener(view -> {
            if ((double) levenshtein(foreign.getText().toString(), vocabularyForeign.get(0)) / vocabularyForeign.get(0).length() < 0.34 && secondChance.get() && !foreign.getText().toString().equals(vocabularyForeign.get(0))) {
                Toast.makeText(this, "Jste blízko!", Toast.LENGTH_SHORT).show();
                secondChance.set(false);
                return;
            }

            secondChance.set(true);

            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View inflatedLayoutView = layoutInflater.inflate(R.layout.popup_test, null);
            inflatedLayoutView.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));

            PopupWindow popup = new PopupWindow(inflatedLayoutView);

            popup.setWidth(800);
            popup.setHeight(1200);
            popup.setFocusable(false);
            popup.showAtLocation(view, Gravity.CENTER, 0, 0);
            popup.setOutsideTouchable(false);

            if (foreign.getText().toString().equals(vocabularyForeign.get(0))) {
                int i = 0;
                for (String s : answers) {
                    if (s.equals(vocabularyForeign.get(0))) {
                        i++;
                    }
                }
                if (i == 0) {
                    correct.getAndSet(correct.get() + 1.0);
                } else if (i == 1) {
                    correct.getAndSet(correct.get() + 0.5);
                }
                Button next = inflatedLayoutView.findViewById(R.id.next);
                ImageView icon = inflatedLayoutView.findViewById(R.id.icon);
                icon.setImageResource(R.drawable.tick);
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
                layout.setBackgroundColor(Color.rgb(255, 51, 86));

                TextView header = inflatedLayoutView.findViewById(R.id.header1);
                header.setText("ŠPATNĚ!");

                ImageView icon = inflatedLayoutView.findViewById(R.id.icon);
                icon.setImageResource(R.drawable.cross);

                TextView correctAnswer = inflatedLayoutView.findViewById(R.id.wrongAnswer);
                String s = "Správná odpověď byla:<br><br> <b>" + vocabularyForeign.get(0) + "</b>";
                correctAnswer.setText(Html.fromHtml(s));

                Button next = inflatedLayoutView.findViewById(R.id.next);
                next.setOnClickListener(view1 -> {
                    int a = (int) ((Math.random() * 5) + 1);
                    while (vocabularyCzech.size() < a) {
                        a--;
                    }
                    if (a != 0) {
                        answers.add(vocabularyForeign.get(0));
                        vocabularyForeign.add(a, vocabularyForeign.get(0));
                        vocabularyCzech.add(a, vocabularyCzech.get(0));
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

    private void showResults(View view, double vocabularyCount, double correct) {
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View inflatedLayoutViewResults = layoutInflater.inflate(R.layout.popup_test_end, null);
        inflatedLayoutViewResults.setAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.popupanim));
        PopupWindow results = new PopupWindow(inflatedLayoutViewResults);

        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View v = getCurrentFocus();
        if (v == null) {
            v = new View(this);
        }
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        results.setWidth(700);
        results.setHeight(1000);
        results.setFocusable(true);
        results.showAtLocation(view, Gravity.CENTER, 0, 0);
        results.setOutsideTouchable(false);
        TextView mark = inflatedLayoutViewResults.findViewById(R.id.mark);
        ConstraintLayout cl = inflatedLayoutViewResults.findViewById(R.id.pozadi);
        Button but = inflatedLayoutViewResults.findViewById(R.id.close);
        ImageView emoji = inflatedLayoutViewResults.findViewById(R.id.emoji);
        long i = Math.round(correct / vocabularyCount * 100);
        if (i < 50) {
            mark.setText("5");
            emoji.setImageResource(R.drawable.mark_4_5);
            cl.setBackgroundColor(Color.rgb(130, 0, 0));
        } else if (i < 64) {
            mark.setText("4");
            emoji.setImageResource(R.drawable.mark_4_5);
            cl.setBackgroundColor(Color.rgb(186, 63, 35));
        } else if (i < 77) {
            mark.setText("3");
            emoji.setImageResource(R.drawable.mark_3);
            cl.setBackgroundColor(Color.rgb(255, 131, 71));
        } else if (i < 90) {
            mark.setText("2");
            emoji.setImageResource(R.drawable.mark_2);
            cl.setBackgroundColor(Color.rgb(203, 224, 111));
        } else {
            mark.setText("1");
            emoji.setImageResource(R.drawable.mark_1);
            cl.setBackgroundColor(Color.rgb(152, 251, 152));
        }

        but.setOnClickListener(vv -> {
            results.dismiss();
            finish();
        });
    }

    private int levenshtein(String typed, String correct) {
        int[][] dp = new int[typed.length() + 1][correct.length() + 1];
        for (int i = 0; i <= typed.length(); i++) {
            for (int j = 0; j <= correct.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int a = typed.charAt(i - 1) == correct.charAt(j - 1) ? 0 : 1;
                    int[] numbers = new int[]{dp[i - 1][j - 1] + a, dp[i - 1][j] + 1, dp[i][j - 1] + 1};
                    dp[i][j] = Arrays.stream(numbers).min().orElse(Integer.MAX_VALUE);
                }
            }
        }
        return dp[typed.length()][correct.length()];
    }
}