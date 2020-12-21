package br.edu.ifrs.classplanner.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.helper.Helper;
import br.edu.ifrs.classplanner.model.Class;
import br.edu.ifrs.classplanner.model.Group;

public class NewGroupFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_group, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Nova turma");

        FloatingActionButton fabSaveGroup = view.findViewById(R.id.fabSaveGroup);
        fabSaveGroup.setOnClickListener(v -> {
            String groupName = ((TextInputEditText) view.findViewById(R.id.editTextGroupName)).getText().toString();
            String groupTime = ((TextInputEditText) view.findViewById(R.id.editTextGroupTime)).getText().toString();
            String groupStartDate = ((TextInputEditText) view.findViewById(R.id.editTextGroupStartDate)).getText().toString();
            String groupClassCount = ((TextInputEditText) view.findViewById(R.id.editTextGroupClassCount)).getText().toString();

            if (!Helper.isValidDateTime(groupStartDate, Helper.DATE)) {
                Snackbar.make(view, "Preencha corretamente a data", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (!Helper.isValidDateTime(groupTime, Helper.TIME)) {
                Snackbar.make(view, "Preencha corretamente o horário", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Group group = new Group(groupName, groupTime, groupStartDate, Integer.parseInt(groupClassCount));

            List<Class> classList = new ArrayList<>();
            LocalDate startDate = LocalDate.parse(groupStartDate,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            .withLocale(new Locale("pt", "BR")));

            int numberOfWeeks = 0;
            for (int classNumber = 0; classNumber < group.getClassCount(); classNumber++) {
                String startDateFormatted;
                do {
                    startDateFormatted = startDate.plusWeeks(numberOfWeeks++).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } while (Helper.isHoliday(startDateFormatted));
                classList.add(new Class(classNumber + 1, startDateFormatted));
            }

            group.setClasses(classList);

            FirebaseDatabase firebase = FirebaseDatabase.getInstance();
            DatabaseReference groupReference = firebase.getReference("group");
            groupReference.push().setValue(group)
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(getContext(), "Turma criada com sucesso", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Erro ao criar turma", Toast.LENGTH_SHORT).show());

            NavHostFragment.findNavController(NewGroupFragment.this)
                    .navigate(R.id.action_NewGroupFragment_to_GroupListFragment);
        });
    }
}