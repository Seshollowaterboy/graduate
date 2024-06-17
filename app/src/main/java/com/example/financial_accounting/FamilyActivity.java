package com.example.financial_accounting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financial_accounting.databinding.ActivityAddFamilyMemberBinding;
import com.example.financial_accounting.databinding.ActivityFamilyBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FamilyActivity extends AppCompatActivity {

    private ActivityFamilyBinding binding;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private final FamilyMemberEditAdapter familyMemberEditAdapter = new FamilyMemberEditAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_family);

        binding = ActivityFamilyBinding.inflate(getLayoutInflater());
        binding.MembersListEdit.setLayoutManager(new LinearLayoutManager(this));
        binding.MembersListEdit.setAdapter(familyMemberEditAdapter);

        String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fetchFamilyMembers(currentUserUid);

        setContentView(binding.getRoot());

        binding.familyNavView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void fetchFamilyMembers(String currentUserUid) {
        // Здесь вы можете выполнить запрос к коллекции с членами семьи
        // Например, если ваши члены семьи хранятся в коллекции "familyMembers"
        // и каждый документ представляет одного члена семьи, вы можете выполнить запрос так:
        db.collection("families")
                .document(currentUserUid)
                .collection("members")
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {
                        List<FamilyMember> documents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            documents.add(new FamilyMember(document.getString("name"), document.getString("status")));
                        }
                        familyMemberEditAdapter.documents = documents;

                        synchronized (familyMemberEditAdapter) {
                            familyMemberEditAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.w("Error get family members", task.getException());
                    }
                });
    }

    public void AddMember(View view) {
        FamilyMember lastElement = familyMemberEditAdapter.documents.get(familyMemberEditAdapter.getItemCount() - 1);

        if(lastElement.getName().isEmpty() || lastElement.getStatus().isEmpty()){
            Snackbar.make(view.getContext(), view, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        familyMemberEditAdapter.documents.add(new FamilyMember("", ""));
        int position = familyMemberEditAdapter.getItemCount() - 1;
        familyMemberEditAdapter.notifyItemInserted(position); // Сообщаем адаптеру, что элемент добавлен
        binding.MembersListEdit.smoothScrollToPosition(position); // Прокручиваем список к новому элементу
    }

    public void SaveMembers(View view) {
        FamilyMember lastElement = familyMemberEditAdapter.documents.get(familyMemberEditAdapter.getItemCount() - 1);

        if(lastElement.getName().isEmpty() || lastElement.getStatus().isEmpty()){
            Snackbar.make(view.getContext(), view, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        String currentUserUid = auth.getCurrentUser().getUid();

        // Удаляем текущие записи о членах семьи из базы данных
        db.collection("families").document(currentUserUid)
                .collection("members")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Успешно получены текущие записи о членах семьи
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Удаляем каждую запись
                        db.collection("families").document(currentUserUid)
                                .collection("members")
                                .document(document.getId())
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Запись успешно удалена
                                    Log.d("SaveMembers", "DocumentSnapshot successfully deleted!");
                                })
                                .addOnFailureListener(e -> {
                                    // Ошибка удаления записи
                                    Log.w("SaveMembers", "Error deleting document", e);
                                });
                    }

                    // Теперь добавляем всех членов семьи заново
                    for (FamilyMember member : familyMemberEditAdapter.documents) {
                        saveFamilyMemberToFireStore(currentUserUid, member, view);
                    }
                })
                .addOnFailureListener(e -> {
                    // Ошибка получения текущих записей о членах семьи
                    Log.w("SaveMembers", "Error getting documents", e);
                });
    }

    private void saveFamilyMemberToFireStore(String currentUserUid, FamilyMember member, View view) {
        // Создаем документ в коллекции пользователей с UID текущего пользователя
        db.collection("families").document(currentUserUid)
                .collection("members")
                .add(member)
                .addOnSuccessListener(documentReference -> {
                    // Успешно сохранено
                    Log.d("AddFamilyMember", "DocumentSnapshot saved");
                    Snackbar.make(view.getContext(), view, "Данные сохранены", Snackbar.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Ошибка сохранения
                    Log.w("AddFamilyMember", "Error adding document", e);
                    Snackbar.make(view.getContext(), view, "Не удалось сохранить данные", Snackbar.LENGTH_SHORT).show();
                });
    }

    public void topAppBar(View view) {
        DrawerLayout drawerLayout = binding.familyDrawerLayout;

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