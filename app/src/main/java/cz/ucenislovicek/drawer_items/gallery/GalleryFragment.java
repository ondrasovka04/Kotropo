package cz.ucenislovicek.drawer_items.gallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import cz.ucenislovicek.databinding.FragmentGalleryBinding;

public class GalleryFragment extends Fragment {


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentGalleryBinding binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textGallery;
        textView.setText("jdshgfjdhskh");


        final Button but = binding.but;
        but.setOnClickListener(view -> {
            //new UpdateExample().execute();
            new SelectExample().execute();
        });


        return root;
    }

    private static class SelectExample extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Session tunel = new JSch().getSession("dk-313", "db.gyarab.cz", 22);
                tunel.setPassword("GyArab14");
                tunel.setConfig("StrictHostKeyChecking", "no");
                tunel.connect();
                tunel.setPortForwardingL(3306, "localhost", 3306);

                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313?serverTimezone=Europe/Prague", "dk-313", "GyArab14"); Statement st = con.createStatement(); ResultSet rs = st.executeQuery("select * from adresar")) {

                    while (rs.next()) {
                        System.out.println(rs.getString("jmeno") + " " + rs.getString("prijmeni"));
                    }
                }
                tunel.disconnect();
            } catch (SQLException | JSchException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class UpdateExample extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Session tunel = new JSch().getSession("dk-313", "db.gyarab.cz", 22);
                tunel.setPassword("GyArab14");
                tunel.setConfig("StrictHostKeyChecking", "no");
                tunel.connect();
                tunel.setPortForwardingL(3306, "localhost", 3306);

                try (Connection con = DriverManager.getConnection("jdbc:mysql://localhost/dk-313?serverTimezone=Europe/Prague", "dk-313", "GyArab14"); Statement st = con.createStatement()) {
                    st.executeUpdate("update adresar set jmeno = 'Ondřej' where jmeno = 'Libor'");
                }

                tunel.disconnect();
            } catch (SQLException | JSchException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}