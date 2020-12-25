package br.edu.ifrs.classplanner.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
import br.edu.ifrs.classplanner.model.Class;
import br.edu.ifrs.classplanner.model.Group;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MINUTES;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.MyViewHolder> {

    private List<Group> groups;
    private Context context;

    public GroupAdapter(List<Group> groups, Context context) {
        this.groups = groups;
        this.context = context;
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

        LocalDate startDate = LocalDate.parse(group.getStartDate(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .withLocale(new Locale("pt", "BR")));
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
                                LocalDateTime classDateTime = LocalDateTime.parse(aClass.getDate() + " " + group.getTime(),
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                                                .withLocale(new Locale("pt", "BR")));

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

                        String nextClass = Collections.min(nextClassesList)
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        myViewHolder.groupNextClass.setText(context.getString(R.string.group_next_class, nextClass));

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
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView groupName, groupDayAndTime, groupNextClass;
        ImageView flagUpToDate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.groupName);
            groupDayAndTime = itemView.findViewById(R.id.groupDayAndTime);
            groupNextClass = itemView.findViewById(R.id.groupNextClass);
            flagUpToDate = itemView.findViewById(R.id.flagUpToDate);
        }
    }
}
