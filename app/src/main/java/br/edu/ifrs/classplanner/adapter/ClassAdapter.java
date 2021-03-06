package br.edu.ifrs.classplanner.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.helper.Helper;
import br.edu.ifrs.classplanner.model.Class;
import br.edu.ifrs.classplanner.model.Group;
import br.edu.ifrs.classplanner.model.Resource;
import br.edu.ifrs.classplanner.service.ReminderService;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.MyViewHolder> {

    private final List<Class> classes;
    private final Group group;
    private final Context context;

    public ClassAdapter(List<Class> classes, Group group, Context context) {
        this.classes = classes;
        this.group = group;
        this.context = context;
    }

    @NonNull
    @Override
    public ClassAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemList = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.adapter_classes, viewGroup, false);
        return new MyViewHolder(itemList);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.setIsRecyclable(false);

        List<Resource> resources = new ArrayList<>();

        Class aClass = classes.get(i);

        DatabaseReference classReference = FirebaseDatabase.getInstance()
                .getReference("classes").child(aClass.getId());

        myViewHolder.classNumber.setText(String.valueOf(aClass.getNumber()));

        LocalDate date = Helper.parseDate(aClass.getDate());
        int day = date.getDayOfMonth();
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
        myViewHolder.classDate.setText(context.getString(R.string.class_day_and_month, day, month));

        myViewHolder.flagClassPlanned.setBackgroundResource(getFlagShape(aClass.isClassPlanned()));
        myViewHolder.flagClassPlanned.setOnClickListener(view -> {
            aClass.setClassPlanned(!aClass.isClassPlanned());
            Task<Void> result = classReference.setValue(aClass);
            showResultForFlags(result, view, myViewHolder.flagClassPlanned, aClass.isClassPlanned());

            if (aClass.isClassPlanned()) {
                ReminderService reminderService = new ReminderService();
                int jobId = reminderService.generateJobId(aClass.getDate(), group.getTime());
                reminderService.cancelJob(context, jobId);
            }
        });

        myViewHolder.flagMaterialSent.setBackgroundResource(getFlagShape(aClass.isMaterialSent()));
        myViewHolder.flagMaterialSent.setOnClickListener(view -> {
            aClass.setMaterialSent(!aClass.isMaterialSent());
            Task<Void> result = classReference.setValue(aClass);
            showResultForFlags(result, view, myViewHolder.flagMaterialSent, aClass.isMaterialSent());
        });

        myViewHolder.flagAttendanceTaken.setBackgroundResource(getFlagShape(aClass.isAttendanceTaken()));
        myViewHolder.flagAttendanceTaken.setOnClickListener(view -> {
            aClass.setAttendanceTaken(!aClass.isAttendanceTaken());
            Task<Void> result = classReference.setValue(aClass);
            showResultForFlags(result, view, myViewHolder.flagAttendanceTaken, aClass.isAttendanceTaken());
        });

        myViewHolder.layoutClassClickable.setOnClickListener(view -> {
            if (myViewHolder.layoutClassSecondary.getVisibility() == View.VISIBLE) {
                myViewHolder.layoutClassSecondary.setVisibility(View.GONE);
            } else {
                myViewHolder.layoutClassSecondary.setVisibility(View.VISIBLE);
            }
        });

        myViewHolder.buttonAddResource.setOnClickListener(view -> {
            View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_new_resource, null);

            AlertDialog alertDialog = new AlertDialog.Builder(context)
                    .setTitle("Novo recurso")
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> dialog.cancel())
                    .show();

            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                String resourceName = ((TextInputEditText) dialogView.findViewById(R.id.inputResourceName)).getText().toString();
                String resourceUrl = ((TextInputEditText) dialogView.findViewById(R.id.inputResourceUrl)).getText().toString();
                CheckBox checkBoxResource = dialogView.findViewById(R.id.checkBoxResource);

                if (resourceName.isEmpty() || resourceUrl.isEmpty()) {
                    Snackbar.make(v, R.string.fill_all_fields, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                if (checkBoxResource.isChecked()) {
                    FirebaseDatabase.getInstance().getReference("classes")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @RequiresApi(api = Build.VERSION_CODES.N)
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    DatabaseReference resourcesReference = FirebaseDatabase.getInstance()
                                            .getReference("resources");

                                    List<Task<Void>> allTasks = new ArrayList<>();

                                    snapshot.getChildren().forEach(dataSnapshot -> {
                                        Class c = dataSnapshot.getValue(Class.class);
                                        if (c.getGroupId().equals(aClass.getGroupId())) {
                                            Resource resource = new Resource(resourceName, resourceUrl, dataSnapshot.getKey());
                                            allTasks.add(resourcesReference.push().setValue(resource));
                                        }
                                    });

                                    Task<Void> firebaseResult = Tasks.whenAll(allTasks);
                                    Task<Void> snackbarResult = showResultForResources(firebaseResult, view);
                                    snackbarResult.addOnSuccessListener(aVoid -> notifyDataSetChanged());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                } else {
                    Resource resource = new Resource(resourceName, resourceUrl, aClass.getId());
                    Task<Void> firebaseResult = FirebaseDatabase.getInstance()
                            .getReference("resources").push().setValue(resource);
                    Task<Void> snackbarResult = showResultForResources(firebaseResult, view);
                    snackbarResult.addOnSuccessListener(aVoid -> notifyDataSetChanged());
                }

                alertDialog.dismiss();
            });
        });

        FirebaseDatabase.getInstance().getReference("resources")
                .addValueEventListener(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        snapshot.getChildren().forEach(dataSnapshot -> {
                            Resource resource = dataSnapshot.getValue(Resource.class);

                            if (resource.getClassId().equals(aClass.getId())) {
                                resources.add(resource);

                                Chip chipResource = (Chip) LayoutInflater.from(context)
                                        .inflate(R.layout.chip_resource, null);
                                chipResource.setText(resource.getName());
                                chipResource.setCheckable(false);
                                chipResource.setCloseIconVisible(false);

                                chipResource.setOnClickListener(view -> {
                                    chipResource.setCloseIconVisible(!chipResource.isCloseIconVisible());
                                });

                                chipResource.setOnLongClickListener(view -> {
                                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("classInfo", resource.getUrl());
                                    clipboard.setPrimaryClip(clip);
                                    return true;
                                });

                                chipResource.setOnCloseIconClickListener(view -> {
                                    String resourceId = dataSnapshot.getKey();
                                    FirebaseDatabase.getInstance().getReference("resources").child(resourceId).removeValue();

                                    Snackbar.make(view, "Recurso excluído", Snackbar.LENGTH_SHORT).show();

                                    notifyDataSetChanged();
                                });

                                myViewHolder.chipGroupResources.addView(chipResource);
                            }
                        });

                        myViewHolder.layoutClassClickable.setOnLongClickListener(view -> {
                            StringBuilder classInfo = new StringBuilder();
                            classInfo.append(group.getName()).append("\n")
                                    .append(aClass.getDate()).append(", ").append(group.getTime()).append("\n");
                            resources.forEach(resource -> classInfo.append(resource.getName()).append(": ").append(resource.getUrl()).append("\n"));

                            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clip = ClipData.newPlainText("classInfo", classInfo);
                            clipboard.setPrimaryClip(clip);

                            return true;
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    private void showResultForFlags(Task<Void> result, View view, View itemView, boolean status) {
        result.addOnSuccessListener(aVoid -> {
            itemView.setBackgroundResource(getFlagShape(status));
            Snackbar.make(view, "Status alterado", Snackbar.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Snackbar.make(view, "Erro ao alterar status", Snackbar.LENGTH_SHORT).show();
        });
    }

    private int getFlagShape(boolean status) {
        return status
                ? R.drawable.roundcorner_accent
                : R.drawable.roundcorner_grey;
    }

    private Task<Void> showResultForResources(Task<Void> result, View view) {
        return result.addOnSuccessListener(aVoid -> {
            Snackbar.make(view, "Recurso criado com sucesso", Snackbar.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Snackbar.make(view, "Erro ao criar recurso", Snackbar.LENGTH_SHORT).show();
        });

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardClass;
        LinearLayout layoutClassClickable, layoutClassSecondary;
        TextView classNumber, classDate;
        ImageButton flagClassPlanned, flagMaterialSent, flagAttendanceTaken, buttonAddResource;
        ChipGroup chipGroupResources;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            cardClass = itemView.findViewById(R.id.cardClass);
            layoutClassClickable = itemView.findViewById(R.id.layoutClassClickable);
            layoutClassSecondary = itemView.findViewById(R.id.layoutClassSecondary);
            classNumber = itemView.findViewById(R.id.classNumber);
            classDate = itemView.findViewById(R.id.classDate);
            flagClassPlanned = itemView.findViewById(R.id.flagClassPlanned);
            flagMaterialSent = itemView.findViewById(R.id.flagMaterialSent);
            flagAttendanceTaken = itemView.findViewById(R.id.flagAttendanceTaken);
            buttonAddResource = itemView.findViewById(R.id.buttonAddResource);
            chipGroupResources = itemView.findViewById(R.id.chipGroupResources);
        }

    }
}
