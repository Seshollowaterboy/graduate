package com.example.financial_accounting;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.financial_accounting.databinding.FragmentAddCategoryBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;


public class AddCategoryFragment extends DialogFragment {

    public boolean isEarnCategory = false;

    private FragmentAddCategoryBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private EarnCategoryActivity earnCategoryActivity;

    private ShoppingCategories shoppingCategories;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof EarnCategoryActivity){
            earnCategoryActivity = (EarnCategoryActivity) context;
        } else if(context instanceof ShoppingCategories){
            shoppingCategories = (ShoppingCategories) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        binding = FragmentAddCategoryBinding.inflate(inflater);

        builder.setView(binding.getRoot());
        builder.setCancelable(true);

        builder.setPositiveButton("Добавить", (dialog, which) -> {

            String name = binding.newCategoryName.getText().toString();
            String userUid = auth.getUid();

            if (!name.isEmpty()) {
                Map<String, Object> categoryData = new HashMap<>();

                if (isEarnCategory) {
                    categoryData.put("name", name);
                    categoryData.put("user", userUid);

                    db.collection("earn_categories")
                            .add(categoryData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("AddCategoryFragment", "Документ успешно добавлен с ID: " + documentReference.getId());
                                // Дополнительные действия после успешного добавления
                            })
                            .addOnFailureListener(e -> {
                                Log.w("AddCategoryFragment", "Ошибка при добавлении документа", e);
                                // Обработка ошибки добавления документа
                            });

                    if(earnCategoryActivity != null){
                        earnCategoryActivity.fillEarnList();
                    }
                } else {
                    categoryData.put("name", name);
                    categoryData.put("user", userUid);

                    db.collection("categories")
                            .add(categoryData)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("AddCategoryFragment", "Документ успешно добавлен с ID: " + documentReference.getId());
                                // Дополнительные действия после успешного добавления
                            })
                            .addOnFailureListener(e -> {
                                Log.w("AddCategoryFragment", "Ошибка при добавлении документа", e);
                                // Обработка ошибки добавления документа
                            });

                    if(shoppingCategories != null){
                        shoppingCategories.fillSpendList();
                    }
                }
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss());

        return builder.create();
    }
}