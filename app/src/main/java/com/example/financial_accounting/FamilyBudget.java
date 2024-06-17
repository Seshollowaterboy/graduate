package com.example.financial_accounting;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.financial_accounting.databinding.ActivityAddFamilyMemberBinding;
import com.example.financial_accounting.databinding.ActivityFamilyBudgetBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class FamilyBudget extends AppCompatActivity {

    private ActivityFamilyBudgetBinding binding;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_family_budget);

        binding = ActivityFamilyBudgetBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());
    }

    public void ConfirmBudget(View view) {
        String budgetText = binding.BudgetEdit.getText().toString();
        if (budgetText.isEmpty()) {
            Snackbar.make(view.getContext(), view, "Поле бюджета не может быть пустым", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String currentUserUid = auth.getCurrentUser().getUid();

        Map<String, Object> budgetData = new HashMap<>();
        budgetData.put("budget", Double.parseDouble(budgetText));

        db.collection("families").document(currentUserUid)
                .set(budgetData, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    // Успешно сохранено
                    Log.d("ConfirmBudget", "Budget data added successfully");
                    Intent intent = new Intent(FamilyBudget.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Ошибка сохранения
                    Log.w("ConfirmBudget", "Error adding budget data", e);
                });
    }
}