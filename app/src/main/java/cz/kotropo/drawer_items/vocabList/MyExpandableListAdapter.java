package cz.kotropo.drawer_items.vocabList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.kotropo.R;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final Map<String, List<String>> hundredCollection;
    private final List<String> hundredList;
    private final Group[] groupes;
    private boolean clickByUser = true;


    public MyExpandableListAdapter(Context context, List<String> hundredList, Map<String, List<String>> hundredCollection) {
        this.context = context;
        this.hundredCollection = hundredCollection;
        this.hundredList = hundredList;
        groupes = new Group[getGroupCount()];
        for (int i = 0; i < groupes.length; i++) {
            groupes[i] = new Group(null, new CheckBox[getChildrenCount(i)]);
        }

    }

    public Group[] getGroupes() {
        return groupes;
    }

    @Override
    public int getGroupCount() {
        return hundredCollection.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return Objects.requireNonNull(hundredCollection.get(hundredList.get(i))).size();
    }

    @Override
    public Object getGroup(int i) {
        return hundredList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return Objects.requireNonNull(hundredCollection.get(hundredList.get(i))).get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String hundredName = getGroup(i).toString();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.hundred_item, null);
        }
        TextView item = view.findViewById(R.id.hundredName);
        item.setTextColor(context.getResources().getColor(R.color.textColor));
        item.setTypeface(null, Typeface.BOLD);
        item.setText(hundredName);

        CheckBox hundredBox = view.findViewById(R.id.hundredBox);
        hundredBox.setTag(hundredName + "@" + i);
        groupes[i].setHundredBox(hundredBox);

        hundredBox.setOnCheckedChangeListener((compoundButton, b1) -> {
            if (clickByUser) {
                String s = (String) hundredBox.getTag();
                for (CheckBox cb : groupes[Integer.parseInt(s.split("@")[1])].getBatches()) {
                    if (cb != null) {
                        cb.setChecked(b1);
                    }
                }
            }
        });
        return view;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String model = getChild(i, i1).toString();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.batch_item, null);
        }
        TextView item = view.findViewById(R.id.batchName);
        item.setTextColor(context.getResources().getColor(R.color.textColor));
        item.setText(model);

        CheckBox batchBox = view.findViewById(R.id.batchBox);
        batchBox.setTag(model);
        groupes[i].getBatches()[i1] = batchBox;

        CheckBox hundredBox = groupes[i].getHundredBox();

        batchBox.setOnCheckedChangeListener((compoundButton, b1) -> {
            clickByUser = false;
            boolean bool = false;
            for (CheckBox cb : groupes[i].getBatches()) {
                if (cb != null) {
                    if (!cb.isChecked()) {
                        bool = true;
                    }
                }
            }
            //bool == true když nejsou všechny zaškrtlé
            groupes[i].getHundredBox().setChecked(!bool);
            clickByUser = true;
        });

        if (hundredBox.isChecked()) {
            for (CheckBox cb : groupes[i].getBatches()) {
                if (cb != null) {
                    cb.setChecked(true);
                }
            }
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }

    public static class Group {
        private final CheckBox[] batches;
        private CheckBox hundredBox;

        public Group(CheckBox hundredBox, CheckBox[] childList) {
            this.hundredBox = hundredBox;
            this.batches = childList;
        }

        public CheckBox getHundredBox() {
            return hundredBox;
        }

        public void setHundredBox(CheckBox hundredBox) {
            this.hundredBox = hundredBox;
        }

        public CheckBox[] getBatches() {
            return batches;
        }
    }
}
