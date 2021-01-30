package br.edu.ifrs.classplanner.adapter;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.helper.Helper;
import br.edu.ifrs.classplanner.model.Class;
import br.edu.ifrs.classplanner.model.Group;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    private List<Group> groups;
    private Context context;
    private NavController navController;

    public GroupAdapter(List<Group> groups, Context context, NavController navController) {
        this.groups = groups;
        this.context = context;
        this.navController = navController;
    }

    @NonNull
    @Override
    public GroupAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemList = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_groups, viewGroup, false);
        return new MyViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupAdapter.MyViewHolder myViewHolder, int i) {
        myViewHolder.setIsRecyclable(false);

        Group group = groups.get(i);

        myViewHolder.groupName.setText(group.getName());

        LocalDate startDate = Helper.parseDate(group.getStartDate());
        String dayOfWeek = startDate.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, new Locale("pt", "BR"));
        String capitalizedDayOfWeek = dayOfWeek.substring(0, 1).toUpperCase() + dayOfWeek.substring(1).toLowerCase();
        myViewHolder.groupDayAndTime.setText(context.getString(R.string.group_day_of_week_and_time, capitalizedDayOfWeek, group.getTime()));

//        Loop para definir data da próxima aula e status geral de atividades
        FirebaseDatabase.getInstance().getReference("classes")
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isUpToDate = true;

                        LocalDateTime now = LocalDateTime.now();

                        List<LocalDateTime> nextClassesList = new ArrayList<>();

                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Class aClass = dataSnapshot.getValue(Class.class);
                            if (aClass.getGroupId().equals(group.getId())) {
                                LocalDateTime classDateTime = Helper.parseDateTime(aClass.getDate() + group.getTime());

                                if (classDateTime.isAfter(now)) {
                                    nextClassesList.add(classDateTime);
                                }

//                                Está na hora da aula e alguma das atividades não foi realizada
                                if (MINUTES.between(classDateTime, now) >= 0
                                        && !(aClass.isClassPlanned() && aClass.isMaterialSent() && aClass.isAttendanceTaken())) {
                                    isUpToDate = false;
//                                Faltam 2 dias ou menos para a próxima aula e ela ainda não foi planejada
                                } else if (DAYS.between(now, classDateTime) <= 2 && !aClass.isClassPlanned()) {
                                    isUpToDate = false;
                                }
                            }
                        }

                        if (!nextClassesList.isEmpty()) {
                            String nextClass = Collections.min(nextClassesList)
                                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                            myViewHolder.groupNextClass.setText(context.getString(R.string.group_next_class, nextClass));
                        }

                        if (isUpToDate) {
                            myViewHolder.flagUpToDate.setImageResource(R.drawable.ic_uptodate);
                        } else {
                            myViewHolder.flagUpToDate.setImageResource(R.drawable.ic_attention);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        myViewHolder.layoutGroupClickable.setOnClickListener(view -> {
            Bundle groupBundle = new Bundle();
            groupBundle.putSerializable("group", group);
            navController.navigate(R.id.action_GroupListFragment_to_ClassListFragment, groupBundle);
        });

        myViewHolder.buttonDelete.setOnClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Excluir grupo")
                    .setMessage("Tem certeza que deseja excluir esse grupo?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        List<Task<Void>> allTasks = new ArrayList<>();

                        DatabaseReference classesReference = FirebaseDatabase.getInstance().getReference("classes");
                        classesReference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    Class aClass = dataSnapshot.getValue(Class.class);
                                    if (aClass.getGroupId().equals(group.getId())) {
                                        allTasks.add(classesReference.child(dataSnapshot.getKey()).removeValue());
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                        allTasks.add(FirebaseDatabase.getInstance()
                                .getReference("groups").child(group.getId()).removeValue());

                        Tasks.whenAll(allTasks).addOnSuccessListener(aVoid -> {
                            groups.remove(i);
                            notifyDataSetChanged();
                        });
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView groupName, groupDayAndTime, groupNextClass;
        ImageView flagUpToDate;
        ImageButton buttonDelete;
        LinearLayout layoutGroupClickable;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.groupName);
            groupDayAndTime = itemView.findViewById(R.id.groupDayAndTime);
            groupNextClass = itemView.findViewById(R.id.groupNextClass);
            flagUpToDate = itemView.findViewById(R.id.flagUpToDate);
            layoutGroupClickable = itemView.findViewById(R.id.layoutGroupClickable);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
