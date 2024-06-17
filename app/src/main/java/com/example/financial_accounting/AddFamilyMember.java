package com.example.financial_accounting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.financial_accounting.databinding.ActivityAddFamilyMemberBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AddFamilyMember extends AppCompatActivity {

    private ActivityAddFamilyMemberBinding binding;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private List<FamilyMember> familyMemberList = new ArrayList<>();
    private final FamilyMemberAdapter familyMemberAdapter = new FamilyMemberAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_family_member);

        familyMemberList.add(new FamilyMember("", ""));

        binding = ActivityAddFamilyMemberBinding.inflate(getLayoutInflater());
        binding.MembersList.setLayoutManager(new LinearLayoutManager(this));
        binding.MembersList.setAdapter(familyMemberAdapter);
        familyMemberAdapter.documents = familyMemberList;
        familyMemberAdapter.notifyDataSetChanged();

        setContentView(binding.getRoot());
    }

    public void AddMember(View view) {
        FamilyMember lastElement = familyMemberAdapter.documents.get(familyMemberAdapter.getItemCount() - 1);

        if(lastElement.getName().isEmpty() || lastElement.getStatus().isEmpty()){
            Snackbar.make(view.getContext(), view, "Не все поля заполнены", Snackbar.LENGTH_SHORT)
                    .show();
            return;
        }

        familyMemberAdapter.documents.add(new FamilyMember("", ""));
        int position = familyMemberAdapter.getItemCount() - 1;
        familyMemberAdapter.notifyItemInserted(position); // Сообщаем адаптеру, что элемент добавлен
        binding.MembersList.smoothScrollToPosition(position); // Прокручиваем список к новому элементу
    }

    public void RemoveMember(View view) {
        int position = familyMemberAdapter.getItemCount() - 1;

        if(position < 1)
        {
            Snackbar.make(view.getContext(), view, "Нужен хотя бы один член семьи", Snackbar.LENGTH_SHORT).show();
            return;
        }

        familyMemberAdapter.documents.remove(position);
        familyMemberAdapter.notifyItemRemoved(position); // Сообщаем адаптеру, что элемент удален
    }

    public void AddAllMembers(View view) {
        int position = familyMemberAdapter.getItemCount() - 1;
        FamilyMember fm = familyMemberAdapter.documents.get(position);

        if(fm.getName().isEmpty() || fm.getStatus().isEmpty()){
            Snackbar.make(view.getContext(), view, "Не все поля заполнены", Snackbar.LENGTH_SHORT).show();
            return;
        }

        String currentUserUid = auth.getCurrentUser().getUid();

        for (FamilyMember member : familyMemberAdapter.documents) {
            saveFamilyMemberToFireStore(currentUserUid, member);
        }
    }

    private void saveFamilyMemberToFireStore(String currentUserUid, FamilyMember member) {
        // Создаем документ в коллекции пользователей с UID текущего пользователя
        db.collection("families").document(currentUserUid)
                .collection("members")
                .add(member)
                .addOnSuccessListener(documentReference -> {
                    // Успешно сохранено
                    Log.d("AddFamilyMember", "DocumentSnapshot added with ID: " + documentReference.getId());
                    Intent intent = new Intent(AddFamilyMember.this, FamilyBudget.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Ошибка сохранения
                    Log.w("AddFamilyMember", "Error adding document", e);
                });
    }
}