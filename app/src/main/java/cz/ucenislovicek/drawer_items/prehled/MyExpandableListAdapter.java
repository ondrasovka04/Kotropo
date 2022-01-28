package cz.ucenislovicek.drawer_items.prehled;

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

import cz.ucenislovicek.R;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final Map<String, List<String>> mobileCollection;
    private final List<String> groupList;
    private final Group[] groupes;
    private boolean clickByUser = true;


    public MyExpandableListAdapter(Context context, List<String> groupList, Map<String, List<String>> mobileCollection) {
        this.context = context;
        this.mobileCollection = mobileCollection;
        this.groupList = groupList;
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
        return mobileCollection.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return mobileCollection.get(groupList.get(i)).size();
    }

    @Override
    public Object getGroup(int i) {
        return groupList.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return mobileCollection.get(groupList.get(i)).get(i1);
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

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        String mobileName = getGroup(i).toString();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.group_item, null);
        }
        TextView item = view.findViewById(R.id.mobile);
        item.setTypeface(null, Typeface.BOLD);
        item.setText(mobileName);

        CheckBox groupBox = view.findViewById(R.id.groupBox);
        groupBox.setTag(mobileName + "@" + i);
        groupes[i].setGroupBox(groupBox);

        groupBox.setOnCheckedChangeListener((compoundButton, b1) -> {
            if (clickByUser) {
                String s = (String) groupBox.getTag();
                for (CheckBox cb : groupes[Integer.parseInt(s.split("@")[1])].getChilds()) {
                    if (cb != null) {
                        cb.setChecked(b1);
                    }
                }
            }
        });
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        String model = getChild(i, i1).toString();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.child_item, null);
        }
        TextView item = view.findViewById(R.id.model);
        item.setText(model);

        CheckBox childBox = view.findViewById(R.id.childBox);
        childBox.setTag(model);
        groupes[i].getChilds()[i1] = childBox;

        CheckBox groupBox = groupes[i].getGroupBox();

        childBox.setOnCheckedChangeListener((compoundButton, b1) -> {
            clickByUser = false;
            boolean pomocny = false;
            for (CheckBox cb : groupes[i].getChilds()) {
                if (cb != null) {
                    if (!cb.isChecked()) {
                        pomocny = true;
                    }
                }
            }
            //pomocny == true když nejsou všechny zaškrtlé
            groupes[i].getGroupBox().setChecked(!pomocny);
            clickByUser = true;
        });

        if (groupBox.isChecked()) {
            for (CheckBox cb : groupes[i].getChilds()) {
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
        private CheckBox groupBox;
        private final CheckBox[] childs;

        public Group(CheckBox groupBox, CheckBox[] childList) {
            this.groupBox = groupBox;
            this.childs = childList;
        }

        public CheckBox getGroupBox() {
            return groupBox;
        }

        public void setGroupBox(CheckBox groupBox) {
            this.groupBox = groupBox;
        }

        public CheckBox[] getChilds() {
            return childs;
        }
    }
}
