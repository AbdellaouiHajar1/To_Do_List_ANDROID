package com.example.test;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText editTextTask;
    private Button buttonAdd, buttonEdit, buttonDelete;
    private ListView listViewTasks;
    private ArrayList<String> tasksList;
    private ArrayAdapter<String> adapter;
    private int selectedTaskIndex = -1; // Pour stocker l'index de la tâche sélectionnée

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        editTextTask = findViewById(R.id.editTextTask);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        listViewTasks = findViewById(R.id.listViewTasks);
        tasksList = new ArrayList<>();

        loadTasks();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTask();
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTaskIndex != -1) {
                    String updatedTask = editTextTask.getText().toString();
                    if (!updatedTask.isEmpty()) {
                        confirmUpdateTask(tasksList.get(selectedTaskIndex), updatedTask);
                    } else {
                        Toast.makeText(MainActivity.this, "Veuillez entrer une nouvelle tâche.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showInfoDialog("Veuillez sélectionner une tâche à modifier en cliquant dessus.");
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedTaskIndex != -1) {
                    confirmDeleteTask(tasksList.get(selectedTaskIndex));
                } else {
                    showInfoDialog("Veuillez sélectionner une tâche à supprimer en cliquant dessus.");
                }
            }
        });

        listViewTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedTask = tasksList.get(position);
                editTextTask.setText(selectedTask);
                selectedTaskIndex = position; // Mettez à jour l'index de la tâche sélectionnée
            }
        });
    }

    private void addTask() {
        String task = editTextTask.getText().toString();
        if (!task.isEmpty()) {
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            db.execSQL("INSERT INTO " + DatabaseHelper.TABLE_NAME + " (" + DatabaseHelper.COLUMN_TASK + ") VALUES ('" + task + "')");
            editTextTask.setText("");
            loadTasks();
        } else {
            Toast.makeText(this, "Veuillez entrer une tâche.", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmUpdateTask(final String oldTask, final String newTask) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de modification")
                .setMessage("Êtes-vous sûr de vouloir modifier cette tâche ?")
                .setPositiveButton("Oui", (dialog, which) -> updateTask(oldTask, newTask))
                .setNegativeButton("Non", null)
                .show();
    }

    private void updateTask(String oldTask, String newTask) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("UPDATE " + DatabaseHelper.TABLE_NAME + " SET " + DatabaseHelper.COLUMN_TASK + "='" + newTask + "' WHERE " + DatabaseHelper.COLUMN_TASK + "='" + oldTask + "'");
        Toast.makeText(this, "Tâche mise à jour", Toast.LENGTH_SHORT).show();
        loadTasks();
    }

    private void confirmDeleteTask(final String task) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation de suppression")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette tâche ?")
                .setPositiveButton("Oui", (dialog, which) -> deleteTask(task))
                .setNegativeButton("Non", null)
                .show();
    }

    private void deleteTask(String task) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_NAME + " WHERE " + DatabaseHelper.COLUMN_TASK + "='" + task + "'");
        Toast.makeText(this, "Tâche supprimée", Toast.LENGTH_SHORT).show();
        loadTasks();
    }

    private void loadTasks() {
        tasksList.clear();
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseHelper.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String task = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_TASK));
            tasksList.add(task);
        }
        cursor.close();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tasksList);
        listViewTasks.setAdapter(adapter);
    }

    private void showInfoDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Information")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}
