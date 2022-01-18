package cz.ucenislovicek.drawer_items.prehled;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class Prehled_ extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView sv = new ScrollView(this);
        TableLayout tl = new TableLayout(this);
        tl.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Intent intent = getIntent();
        List<String> cesky = intent.getStringArrayListExtra("cesky");
        List<String> cizi = intent.getStringArrayListExtra("cizi");
        List<String> badge = intent.getStringArrayListExtra("badge");
        String aktualBadge = "";

        for (int i = 0; i < cesky.size(); i++) {
            if (aktualBadge.equals(badge.get(i))) {
                TableRow tableRow = new TableRow(this);

                TableLayout.LayoutParams param = new TableLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);

                TextView tv_cizi = new TextView(this);
                tv_cizi.setPadding(10, 10, 10, 10);
                tv_cizi.setGravity(Gravity.START);
                tv_cizi.setTextSize(20);
                tv_cizi.setText(cizi.get(i));
                //tv_cizi.setLayoutParams(new TableRow.LayoutParams(0));
                tv_cizi.setLayoutParams(param);

                TextView tv_cesky = new TextView(this);
                tv_cesky.setPadding(10, 10, 10, 10);
                tv_cesky.setGravity(Gravity.START);
                tv_cesky.setTextSize(20);
                tv_cesky.setText(cesky.get(i));
                tv_cesky.setLayoutParams(new TableRow.LayoutParams(2));
                //tv_cesky.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
                tableRow.addView(tv_cizi);
                tableRow.addView(tv_cesky);

                tl.addView(tableRow);
                View v = new View(this);
                v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, 1));
                v.setBackgroundColor(Color.rgb(51, 51, 51));
                tl.addView(v);
            } else {
                aktualBadge = badge.get(i);
                i--;
                TextView tv = new TextView(this);
                TableRow tr = new TableRow(this);
                tv.setPadding(10, 10, 10, 10);
                tv.setGravity(Gravity.START);
                tv.setTypeface(Typeface.DEFAULT_BOLD);
                tv.setTextSize(22);
                tv.setText(aktualBadge);
                tr.addView(tv);
                tl.addView(tr);
            }
        }
        sv.addView(tl);
        setContentView(sv);
    }
}