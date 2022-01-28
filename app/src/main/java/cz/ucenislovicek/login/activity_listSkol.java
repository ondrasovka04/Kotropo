package cz.ucenislovicek.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cz.ucenislovicek.R;
import cz.ucenislovicek.SharedPrefs;

public class activity_listSkol extends AppCompatActivity {
    private final List<ExampleItem> mesta = new ArrayList<>(), skoly = new ArrayList<>();
    private final Context activityContext = this;
    boolean zobrazenySkoly = false;
    private ExampleAdapter adapter;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_skol);
        new getMesta(this).execute();
    }

    private void setUpRecyclerView(List<ExampleItem> list) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new ExampleAdapter(list);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (zobrazenySkoly) {
            setUpRecyclerView(mesta);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.skola_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }

    static class ExampleItem {
        private final String text1;
        private final String text2;

        public ExampleItem(String text1, String text2) {
            this.text1 = text1;
            this.text2 = text2;
        }

        public String getText1() {
            return text1;
        }

        public String getText2() {
            return text2;
        }
    }

    class ExampleAdapter extends RecyclerView.Adapter<ExampleAdapter.ExampleViewHolder> implements Filterable {
        private final List<ExampleItem> exampleList;
        private final List<ExampleItem> exampleListFull;
        private final Filter exampleFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ExampleItem> filteredList = new ArrayList<>();

                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(exampleListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (ExampleItem item : exampleListFull) {
                        String text1 = bezDiakritiky(item.getText1()).toLowerCase();
                        String text2 = bezDiakritiky(item.getText2()).toLowerCase();
                        if (text1.contains(filterPattern) || text2.contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }

                FilterResults results = new FilterResults();
                results.values = filteredList;

                return results;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                exampleList.clear();
                exampleList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };

        public String bezDiakritiky(String vstup){
            String abecedasdiakritikou = "áÁčČďĎéÉěĚíÍňŇóÓřŘšŠťŤúÚůŮýÝžŽ";
            String abecedebezdiakritiky = "aAcCdDeEeEiInNoOrRsStTuUuUyYzZ";
            char[] pole1 = vstup.toCharArray();
            char[] pole2 = abecedasdiakritikou.toCharArray();
            char[] pole3 = abecedebezdiakritiky.toCharArray();
            for(int a = 0; a < pole1.length; a++){
                for(int b = 0; b < pole2.length; b++){
                    if(pole1[a] == pole2[b]){
                        pole1[a] = pole3[b];
                    }
                }
            }
            return String.valueOf(pole1);
        }

        ExampleAdapter(List<ExampleItem> exampleList) {
            this.exampleList = exampleList;
            exampleListFull = new ArrayList<>(exampleList);
        }

        @NonNull
        @Override
        public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.skola_item, parent, false);
            return new ExampleViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ExampleViewHolder holder, int position) {
            ExampleItem currentItem = exampleList.get(position);

            holder.textView1.setText(currentItem.getText1());
            if (currentItem.getText2().equals("")) {
                holder.textView2.setVisibility(View.GONE);
            }
            holder.textView2.setText(currentItem.getText2());
        }

        @Override
        public int getItemCount() {
            return exampleList.size();
        }

        @Override
        public Filter getFilter() {
            return exampleFilter;
        }

        class ExampleViewHolder extends RecyclerView.ViewHolder {
            TextView textView1;
            TextView textView2;

            ExampleViewHolder(View itemView) {
                super(itemView);
                textView1 = itemView.findViewById(R.id.text_view1);
                textView2 = itemView.findViewById(R.id.text_view2);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (zobrazenySkoly) {
                            SharedPrefs.setString(activityContext, SharedPrefs.URL, textView2.getText().toString());
                            startActivity(new Intent(activityContext, LoginForm.class));
                        } else {
                            if (skoly.isEmpty()) {
                                new getSkoly(itemView.getContext(), textView1.getText().toString()).execute();
                            } else {
                                setUpRecyclerView(skoly);
                            }
                        }
                    }
                });
            }
        }
    }

    public class getMesta extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;

        public getMesta(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("https://sluzby.bakalari.cz/api/v1/municipality");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                JSONArray myArray = new JSONArray(content.toString());
                mesta.clear();
                for (int i = 1; i < myArray.length(); i++) {
                    mesta.add(new ExampleItem(myArray.getJSONObject(i).getString("name"), ""));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            zobrazenySkoly = false;
            setUpRecyclerView(mesta);
        }
    }

    public class getSkoly extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        public Context context;
        public String city;

        public getSkoly(Context context, String city) {
            this.context = context;
            this.city = city;
        }

        public String URLEncode(String text) {
            if (text.contains(".")) {
                text = text.substring(0, text.indexOf("."));
            }
            return text.replace("ě", "%C4%9B").replace("š", "%C5%A1").replace("č", "%C4%8D").replace("ř", "%C5%99").replace("ž", "%C5%BE").replace("ý", "%C3%BD").replace("á", "%C3%A1").replace("í", "%C3%AD").replace("é", "%C3%A9").replace("ď", "%C4%8F").replace("ť", "%C5%A5").replace("ň", "%C5%88").replace("ú", "%C3%BA").replace("ů", "%C5%AF").replace("Š", "%C5%A0").replace("Č", "%C4%8C").replace("Ř", "%C5%98").replace("Ž", "%C5%BD").replace("Á", "%C3%81").replace("Ú", "%C3%9A").replace(" ", "%20").replace("ü", "%C3%BC");
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("https://sluzby.bakalari.cz/api/v1/municipality/" + URLEncode(city));
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Accept", "application/json");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                JSONArray myArray = new JSONObject(content.toString()).getJSONArray("schools");
                for (int i = 0; i < myArray.length(); i++) {
                    skoly.add(new ExampleItem((String) myArray.getJSONObject(i).get("name"), (String) myArray.getJSONObject(i).get("schoolUrl")));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            zobrazenySkoly = true;
            setUpRecyclerView(skoly);
            searchView.setQuery("", true);
        }
    }
}