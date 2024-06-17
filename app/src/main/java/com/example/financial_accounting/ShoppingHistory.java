package com.example.financial_accounting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class ShoppingHistory extends AppCompatActivity {

    private com.example.financial_accounting.databinding.ActivityShoppingHistoryBinding binding;

    private final CategoryAdapter categoryAdapter = new CategoryAdapter();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_shopping_history);

        binding = com.example.financial_accounting.databinding.ActivityShoppingHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.historylist.setLayoutManager(new LinearLayoutManager(this));
        binding.historylist.setAdapter(categoryAdapter); // получаем шаблон
        // Получение всех категорий
        db.collection("spends").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<QueryDocumentSnapshot> documents = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (Objects.equals(document.get("user_id"), auth.getUid())) documents.add(document);
                    }
                    // сортировка по датам
                    documents.sort(new Comparator<QueryDocumentSnapshot>() {
                        @Override
                        public int compare(QueryDocumentSnapshot o1, QueryDocumentSnapshot o2) {
                            return -o1.getDate("date").compareTo(o2.getDate("date"));
                        }
                    });
                    categoryAdapter.isAdding = false;
                    categoryAdapter.documents = documents;
                    // Для того чтобы не было ошибки, если список заблокирован
                    synchronized (categoryAdapter) {
                        categoryAdapter.notifyDataSetChanged();
                    }
                } else {
                    Log.w("TEST", "Error getting documents.", task.getException());
                }
            }
        });

        binding.navView2.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    public void topAppBar(View view) {
        DrawerLayout drawerLayout = findViewById(R.id.drawerlayout2);

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
}