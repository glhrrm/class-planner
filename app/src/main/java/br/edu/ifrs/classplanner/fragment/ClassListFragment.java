package br.edu.ifrs.classplanner.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.adapter.ClassAdapter;
import br.edu.ifrs.classplanner.model.Class;
import br.edu.ifrs.classplanner.model.Group;

public class ClassListFragment extends Fragment {

    private RecyclerView recyclerClasses;
    private List<Class> classList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class_list, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerClasses = getActivity().findViewById(R.id.recyclerClasses);

        Group group = (Group) getArguments().getSerializable("group");

        getActivity().setTitle(group.getName());

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference groupReference = firebase.getReference("group").child(group.getId()).child("classes");
        groupReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                classList = new ArrayList<>();

                ClassAdapter classAdapter = new ClassAdapter(classList, group, getActivity());
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
                recyclerClasses.setLayoutManager(layoutManager);
                recyclerClasses.setHasFixedSize(true);

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Class aClass = dataSnapshot.getValue(Class.class);
                    aClass.setId(dataSnapshot.getKey());
                    classList.add(aClass);
                }

                recyclerClasses.setAdapter(classAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}