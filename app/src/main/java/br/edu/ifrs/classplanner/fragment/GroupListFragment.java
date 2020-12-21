package br.edu.ifrs.classplanner.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.adapter.GroupAdapter;
import br.edu.ifrs.classplanner.listener.RecyclerItemClickListener;
import br.edu.ifrs.classplanner.model.Group;

public class GroupListFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_list, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Turmas");

        FloatingActionButton fabCreateGroup = getActivity().findViewById(R.id.fabCreateGroup);
        fabCreateGroup.setOnClickListener(v -> NavHostFragment.findNavController(GroupListFragment.this)
                .navigate(R.id.action_GroupListFragment_to_NewGroupFragment));

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference groupReference = firebase.getReference("group");
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Group> groupList = new ArrayList<>();

                GroupAdapter groupAdapter = new GroupAdapter(groupList, getActivity());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                RecyclerView recyclerGroups = getActivity().findViewById(R.id.recyclerGroups);
                recyclerGroups.setLayoutManager(layoutManager);
                recyclerGroups.setHasFixedSize(true);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Group group = dataSnapshot.getValue(Group.class);
                    group.setId(dataSnapshot.getKey());
                    groupList.add(group);
                }

                recyclerGroups.setAdapter(groupAdapter);

                recyclerGroups.addOnItemTouchListener(
                        new RecyclerItemClickListener(
                                getContext(),
                                recyclerGroups,
                                new RecyclerItemClickListener.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                                    }

                                    @Override
                                    public void onItemClick(View view, int position) {
                                        Bundle groupBundle = new Bundle();
                                        groupBundle.putSerializable("group", groupList.get(position));
                                        NavHostFragment.findNavController(GroupListFragment.this)
                                                .navigate(R.id.action_GroupListFragment_to_ClassListFragment, groupBundle);
                                    }

                                    @Override
                                    public void onLongItemClick(View view, int position) {

                                    }
                                }
                        )
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}