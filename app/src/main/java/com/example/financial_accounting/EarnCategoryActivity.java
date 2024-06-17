package com.example.financial_accounting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financial_accounting.databinding.ActivityEarnCategoryBinding;
import com.example.financial_accounting.databinding.FragmentAddCategoryBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EarnCategoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityEarnCategoryBinding binding;
    private EarnCategoryAdapter earnCategoryAdapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_earn_category);

        binding = ActivityEarnCategoryBinding.inflate(getLayoutInflater());
        binding.earnCategoriesList.setLayoutManager(new LinearLayoutManager(this));

        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchFamilyMembers(currentUserUid);

        binding.familyMembersSpinner.setOnItemSelectedListener(this);

        setContentView(binding.getRoot());

        binding.earnNavView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    public void fillEarnList() {
        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("earn_categories").orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                List<QueryDocumentSnapshot> documents = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getString("user").equals(currentUserUid) || document.getString("user").equals(""))
                        documents.add(document);
                }
                earnCategoryAdapter.documents = documents;
                synchronized (earnCategoryAdapter){
                    earnCategoryAdapter.notifyDataSetChanged();
                }
            } else {
                Snackbar.make(binding.getRoot().getContext(), binding.getRoot(), "Не удалось получить категории.", Snackbar.LENGTH_LONG).show();
                Log.w("EarnCategoryActivity", "Error getting documents.", task.getException());
            }
        });
    }

    private void fetchFamilyMembers(String currentUserUid) {
        db.collection("families")
                .document(currentUserUid)
                .collection("members")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        List<String> familyMemberNames = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            familyMemberNames.add(document.getString("name"));
                        }

                        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, familyMemberNames);
                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.familyMembersSpinner.setAdapter(spinnerAdapter);
                    } else {
                        Log.w("Error get family members", task.getException());
                    }
                });
    }

    public void topAppBar(View view) {
        DrawerLayout drawerLayout = binding.earnDrawerLayout;

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.currencyExchange) {
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.shoppingBag) {
            Intent intent = new Intent(this, ShoppingCategories.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.shoppingCart) {
            Intent intent = new Intent(this, ShoppingHistory.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.family) {
            Intent intent = new Intent(this, FamilyActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.monetization) {
            Intent intent = new Intent(this, EarnCategoryActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.budget) {
            Intent intent = new Intent(this, FamilyBudgetCurrentActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.earnHistory) {
            Intent intent = new Intent(this, EarnCategoryHistoryActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.statsEarning) {
            Intent intent = new Intent(this, EarnStatisticsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedFamilyMember = (String) parent.getItemAtPosition(position);
        // Создаем адаптер и передаем выбранное имя в конструктор
        earnCategoryAdapter = new EarnCategoryAdapter(selectedFamilyMember, currentUserUid);

        // Устанавливаем адаптер в RecyclerView
        binding.earnCategoriesList.setAdapter(earnCategoryAdapter);
        fillEarnList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void addEarnCategory(View view) {
        AddCategoryFragment fragmentAddCategory = new AddCategoryFragment();
        fragmentAddCategory.isEarnCategory = true;
        fragmentAddCategory.show(getSupportFragmentManager(), "addCategory");
    }
}