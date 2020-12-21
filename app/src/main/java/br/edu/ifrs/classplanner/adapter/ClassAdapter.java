package br.edu.ifrs.classplanner.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import br.edu.ifrs.classplanner.R;
import br.edu.ifrs.classplanner.model.Class;
import br.edu.ifrs.classplanner.model.Group;
import br.edu.ifrs.classplanner.model.Resource;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.MyViewHolder> {

    private List<Class> classes;
    private Group group;
    private Context context;

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

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        myViewHolder.setIsRecyclable(false);

        Class aClass = classes.get(i);

        FirebaseDatabase firebase = FirebaseDatabase.getInstance();
        DatabaseReference groupReference = firebase.getReference("group").child(group.getId());
        DatabaseReference classReference = groupReference.child("classes").child(aClass.getId());

        myViewHolder.classNumber.setText(String.valueOf(aClass.getNumber()));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dtf = dtf.withLocale(new Locale("pt", "BR"));
        LocalDate date = LocalDate.parse(aClass.getDate(), dtf);
        int day = date.getDayOfMonth();
        String month = date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR"));
        myViewHolder.classDate.setText(day + " " + month);

        myViewHolder.flagClassPlanned.setBackgroundResource(getFlagShape(aClass.isClassPlanned()));
        myViewHolder.flagClassPlanned.setOnClickListener(view -> {
            aClass.setClassPlanned(!aClass.isClassPlanned());
            Task<Void> result = classReference.setValue(aClass);
            showSnackbarAndChangeShape(result, view, myViewHolder.flagClassPlanned, aClass.isClassPlanned());
        });

        myViewHolder.flagMaterialSent.setBackgroundResource(getFlagShape(aClass.isMaterialSent()));
        myViewHolder.flagMaterialSent.setOnClickListener(view -> {
            aClass.setMaterialSent(!aClass.isMaterialSent());
            Task<Void> result = classReference.setValue(aClass);
            showSnackbarAndChangeShape(result, view, myViewHolder.flagMaterialSent, aClass.isMaterialSent());
        });

        myViewHolder.flagAttendanceTaken.setBackgroundResource(getFlagShape(aClass.isAttendanceTaken()));
        myViewHolder.flagAttendanceTaken.setOnClickListener(view -> {
            aClass.setAttendanceTaken(!aClass.isAttendanceTaken());
            Task<Void> result = classReference.setValue(aClass);
            showSnackbarAndChangeShape(result, view, myViewHolder.flagAttendanceTaken, aClass.isAttendanceTaken());
        });

        myViewHolder.layoutClassClickable.setOnLongClickListener(view -> {
            String classInfo = group.getName() + "\n" +
                    aClass.getDate() + ", " + group.getTime() + "\n";
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("classInfo", classInfo);
            clipboard.setPrimaryClip(clip);
            return true;
        });

        myViewHolder.layoutClassClickable.setOnClickListener(view -> {
//          TransitionManager.beginDelayedTransition(myViewHolder.cardClass, new AutoTransition());
            if (myViewHolder.layoutClassSecondary.getVisibility() == View.VISIBLE) {
                myViewHolder.layoutClassSecondary.setVisibility(View.GONE);
            } else {
                myViewHolder.layoutClassSecondary.setVisibility(View.VISIBLE);
            }
        });

        myViewHolder.buttonAddResource.setOnClickListener(view -> {
            LayoutInflater factory = LayoutInflater.from(context);
            View dialogView = factory.inflate(R.layout.dialog_new_resource, null);

            TextInputEditText inputResourceName = dialogView.findViewById(R.id.inputResourceName);
            TextInputEditText inputResourceUrl = dialogView.findViewById(R.id.inputResourceUrl);
            CheckBox checkBoxResource = dialogView.findViewById(R.id.checkBoxResource);

            new AlertDialog.Builder(context)
                    .setTitle("Novo recurso")
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        if (inputResourceName.getText() != null && inputResourceUrl.getText() != null) {
                            String resourceName = inputResourceName.getText().toString();
                            String resourceUrl = inputResourceUrl.getText().toString();
                            if (checkBoxResource.isChecked()) {
                                for (Class c : group.getClasses()) {
//                                    Resource resource = new Resource(resourceName, resourceUrl);
//                                    DatabaseReference cReference = groupReference.child("classes").child(c.getId());
//                                    cReference.child("resources").push().setValue(resource);
                                }
                            } else {
                                Resource resource = new Resource(resourceName, resourceUrl);
                                classReference.child("resources").push().setValue(resource);
                            }
                        }
                    })
                    .setNegativeButton(android.R.string.no, (dialog, which) -> {
                        dialog.cancel();
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardClass;
        LinearLayout layoutClassClickable, layoutClassSecondary;
        TextView classNumber, classDate;
        ImageButton flagClassPlanned, flagMaterialSent, flagAttendanceTaken, buttonAddResource;

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
        }
    }

    private int getFlagShape(boolean status) {
        return status
                ? R.drawable.roundcorner_green
                : R.drawable.roundcorner_red;
    }

    private void showSnackbarAndChangeShape(Task<Void> result, View view, View itemView, boolean status) {
        result.addOnSuccessListener(aVoid -> {
            itemView.setBackgroundResource(getFlagShape(status));
            Snackbar.make(view, "Status alterado", Snackbar.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Snackbar.make(view, "Erro ao alterar status", Snackbar.LENGTH_SHORT).show();
        });
    }
}
