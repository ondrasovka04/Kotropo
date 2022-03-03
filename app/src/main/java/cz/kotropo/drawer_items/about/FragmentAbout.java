package cz.kotropo.drawer_items.about;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import cz.kotropo.databinding.FragmentAboutBinding;

public class FragmentAbout extends Fragment {

    @SuppressLint("SetTextI18n")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentAboutBinding binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView textView = binding.textHome;
        textView.setText("Aplikace KoTroPo má za úkol pomáhat studentům při učení slovíček cizích jazyků. Student přidává slovíčka, která rozděluje do stovek a batchů. Dále se z nich může nechat vyzkoušet a postupným zkoušením se je tak naučit.");
        return root;
    }
}