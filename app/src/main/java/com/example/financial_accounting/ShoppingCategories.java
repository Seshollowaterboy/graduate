package com.example.financial_accounting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financial_accounting.databinding.ActivityShoppingCategoriesBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCategories extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ActivityShoppingCategoriesBinding binding;
    private CategoryAdapter categoryAdapter;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shopping_categories);

        binding = ActivityShoppingCategoriesBinding.inflate(getLayoutInflater());
        binding.categoriesList.setLayoutManager(new LinearLayoutManager(this));

        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchFamilyMembers(currentUserUid);

        binding.shoppingFamilyMembersSpinner.setOnItemSelectedListener(this);

        setContentView(binding.getRoot());

        binding.navView1.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    public void fillSpendList() {
        db.collection("categories").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<QueryDocumentSnapshot> documents = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.getString("user").equals(currentUserUid) || document.getString("user").equals(""))
                        documents.add(document);
                }
                // получаем данные
                categoryAdapter.documents = documents;
                // Для того чтобы не было ошибки, если список заблокирован
                synchronized (categoryAdapter) {
                    // обновление адаптера
                    categoryAdapter.notifyDataSetChanged();
                }
            } else {
                Log.w("TEST", "Error getting documents.", task.getException());
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
                        binding.shoppingFamilyMembersSpinner.setAdapter(spinnerAdapter);
                    } else {
                        Log.w("Error get family members", task.getException());
                    }
                });
    }

    public void topAppBar(View view) {
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout1);

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
        categoryAdapter = new CategoryAdapter(selectedFamilyMember, currentUserUid);

        // Устанавливаем адаптер в RecyclerView
        binding.categoriesList.setAdapter(categoryAdapter);
        fillSpendList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void addShoppingCategory(View view) {
        AddCategoryFragment fragmentAddCategory = new AddCategoryFragment();
        fragmentAddCategory.isEarnCategory = false;
        fragmentAddCategory.show(getSupportFragmentManager(), "addCategory");
    }
}