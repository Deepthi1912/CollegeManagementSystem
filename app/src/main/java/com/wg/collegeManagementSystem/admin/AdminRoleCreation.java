package com.wg.collegeManagementSystem.admin;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.wg.collegeManagementSystem.R;
import com.wg.collegeManagementSystem.data.model.Role;
import com.wg.collegeManagementSystem.data.repo.RoleRepo;
import com.wg.collegeManagementSystem.model.RoleList;

import java.util.List;

public class AdminRoleCreation extends Fragment {

    private static final String TAG = AdminRoleCreation.class.getSimpleName();
    private ListView listView;
    private EditText inputRole;
    private AppCompatButton fragmentAdminRoleBtnInsert;
    private CustomAdapter customAdapter;
    private MaterialDialog.Builder builder;
    private String oldRoleType, newRoleType;
    private boolean wrapInScrollView = true;
    private EditText fragmentAdminRoleUpdateInputRoleType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_admin_role_creation, container, false);

        builder = new MaterialDialog.Builder(getActivity());
        inputRole = (EditText) view.findViewById(R.id.fragment_admin_role_input_role);
        listView = (ListView) view.findViewById(R.id.fragment_admin_role_list);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View mView = inflater.inflate(R.layout.fragment_admin_role_update, null);

                oldRoleType = ((AppCompatTextView) view.findViewById(R.id.fragment_admin_role_list_role_type)).getText().toString();
                fragmentAdminRoleUpdateInputRoleType = (EditText) mView.findViewById(R.id.fragment_admin_role_update_input_role);
                fragmentAdminRoleUpdateInputRoleType.setText(oldRoleType);

                builder.title("Action");
                builder.positiveText("Update");
                builder.negativeText("Delete");
                builder.neutralText("Cancel");
                builder.customView(mView, wrapInScrollView);
                builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        newRoleType = fragmentAdminRoleUpdateInputRoleType.getText().toString().trim();
                        update(oldRoleType, newRoleType);
                    }
                });
                builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        delete();
                    }
                });
                builder.onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        return;
                    }
                });

                MaterialDialog dialog = builder.build();
                dialog.show();

                return true;
            }
        });

        fragmentAdminRoleBtnInsert = (AppCompatButton) view.findViewById(R.id.fragment_admin_role_btn_insert);
        fragmentAdminRoleBtnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insert();
            }
        });
        show_data();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle("User Role Creation");
    }

    /**
     * For inserting in database
     */
    public void insert() {
        String roleType = inputRole.getText().toString().trim();

        if (roleType.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill the input field", Toast.LENGTH_SHORT).show();
        } else {
            if (roleType.equals(oldRoleType)) {
                Toast.makeText(getActivity(), "You already made an entry for this", Toast.LENGTH_SHORT).show();
            } else {
                RoleRepo roleRepo = new RoleRepo();
                Role role = new Role();

                role.setRoleType(roleType);
                int status = roleRepo.insert(role);

                inputRole.setText("");

                Toast.makeText(getActivity(), "Added Successfully", Toast.LENGTH_SHORT).show();
                show_data();
            }
        }
    }

    /**
     * For updating in database
     */

    public void update(String oldRoleType, String newRoleType) {
        if (newRoleType.isEmpty()) {
            Toast.makeText(getActivity(), "Please fill the input field", Toast.LENGTH_SHORT).show();
        } else {
            RoleRepo roleRepo = new RoleRepo();
            Role role = new Role();

            role.setOldRoleType(oldRoleType);
            role.setNewRoleType(newRoleType);
            roleRepo.update(role);

            Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_SHORT).show();
            show_data();
        }
    }

    /**
     * For deleting roles in database
     */
    public void delete() {
        RoleRepo roleRepo = new RoleRepo();
        Role role = new Role();

        role.setRoleType(oldRoleType);
        roleRepo.delete(role);

        Toast.makeText(getActivity(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
        show_data();
    }

    /**
     * For showing added roles from database
     */
    public void show_data() {
        RoleRepo roleRepo = new RoleRepo();
        List<RoleList> list = roleRepo.getRole();

        String[] roleType;

        roleType = new String[list.size()];

        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                roleType[i] = list.get(i).getRoleType();
            }
            customAdapter = new CustomAdapter(getActivity(), roleType);
            listView.setAdapter(customAdapter);
        } else {
            Toast.makeText(getActivity(), "No data in database", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ViewHolder to hold elements from custom row layout
     */
    public class ViewHolder {
        AppCompatTextView lblSlNo, lblRoleType;

        public ViewHolder(View v) {
            lblSlNo = (AppCompatTextView) v.findViewById(R.id.fragment_admin_role_list_sl_no);
            lblRoleType = (AppCompatTextView) v.findViewById(R.id.fragment_admin_role_list_role_type);
        }
    }

    /**
     * Custom adapter to fill list view
     */
    public class CustomAdapter extends ArrayAdapter<String> {
        String[] mRoleType;
        Context mContext;

        public CustomAdapter(Context context, String[] roleType) {
            super(context, R.layout.fragment_admin_role_row, R.id.fragment_admin_role_list_role_type, roleType);
            mRoleType = roleType;
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder viewHolder;

            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.fragment_admin_role_row, parent, false);
                viewHolder = new ViewHolder(row);
                row.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) row.getTag();
            }

            viewHolder.lblSlNo.setText(String.valueOf(position + 1));
            viewHolder.lblRoleType.setText(mRoleType[position]);

            return row;
        }
    }
}
