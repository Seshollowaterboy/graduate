package com.example.financial_accounting;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class EarnCategoryAdapter extends RecyclerView.Adapter<EarnCategoryAdapter.ViewHolder>{

    // Список всех категорий в виде документа
    List<QueryDocumentSnapshot> documents = new ArrayList<>();
    // Словарь (название категории к сумме) для сохранения при скроллинге
    HashMap<String, Double> nameToSum = new HashMap<>();

    // Режим добавления
    Boolean isAdding = true;

    private String selectedFamilyMember;
    private String currentUserUid;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView earnImageView;
        final TextInputEditText earnSumTextInput;
        final TextInputLayout earnSumTextInputLayout;
        final Button earnAddButton;

        final TextView earnSumText;

        final TextView earnCategoryName;

        final TextView earnDateView;
        final TextView earnFamilyMember;

        final TextView earnCategoryNameAdding;

        // получение элементов
        public ViewHolder(View view) {
            super(view);
            earnSumTextInput = view.findViewById(R.id.earnSumEdit);
            earnImageView = view.findViewById(R.id.earnImage);
            earnAddButton = view.findViewById(R.id.earnBtn);
            earnSumText = view.findViewById(R.id.earnSumText);
            earnCategoryName = view.findViewById(R.id.earnName);
            earnDateView = view.findViewById(R.id.earnCategoryDate);
            earnFamilyMember = view.findViewById(R.id.earnCategoryFamilyMember);
            earnCategoryNameAdding = view.findViewById(R.id.earnNameAdding);
            earnSumTextInputLayout = view.findViewById(R.id.earnSum);
        }
    }

    public EarnCategoryAdapter(){}

    public EarnCategoryAdapter(String selectedFamilyMember, String currentUserUid) {
        this.selectedFamilyMember = selectedFamilyMember;
        this.currentUserUid = currentUserUid;
    }

    public EarnCategoryAdapter(Boolean isAdding) {
        this.isAdding = isAdding;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.earn_category, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.earnAddButton.setEnabled(false);

        // история трат
        if (!isAdding) {
            // изменение отображения
            holder.earnSumTextInputLayout.setVisibility(View.GONE);
            holder.earnSumText.setText(documents.get(position).getLong("sum").toString() + " р.");
            holder.earnFamilyMember.setText(documents.get(position).getString("member"));
            holder.earnDateView.setText(new SimpleDateFormat("dd.MM.yyyy").format(documents.get(position).getDate("date")));
            holder.earnSumText.setVisibility(View.VISIBLE);
            holder.earnCategoryNameAdding.setVisibility(View.GONE);
            holder.earnDateView.setVisibility(View.VISIBLE);
            holder.earnFamilyMember.setVisibility(View.VISIBLE);

            holder.earnAddButton.setVisibility(View.GONE);

            // получение названия категории
            documents.get(position).getDocumentReference("earn_category").get().addOnSuccessListener(documentSnapshot -> {
                String name = documentSnapshot.getString("name");
                holder.earnCategoryName.setText(name);
                holder.earnCategoryName.setVisibility(View.VISIBLE);

                // Проверяем, существует ли файл изображения в Firebase Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imageRef = storage.getReference().child(name.toLowerCase(Locale.ROOT) + ".png");

                imageRef.getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Файл существует, загружаем изображение
                        Uri uri = task.getResult();
                        Picasso.get().load(uri).into(holder.earnImageView); // Отображаем полученное изображение
                    } else {
                        // Обработка ошибки загрузки изображения по умолчанию
                        holder.earnImageView.setImageResource(R.drawable.person); // Загрузить изображение по умолчанию
                    }
                });
            });
            return;
        }

        String name = documents.get(position).getString("name");
        holder.earnCategoryNameAdding.setText(name);

        // Загрузка изображения из Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child(name.toLowerCase(Locale.ROOT) + ".png");

        imageRef.getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Файл существует, загружаем изображение
                Uri uri = task.getResult();
                Picasso.get().load(uri).into(holder.earnImageView); // Отображаем полученное изображение
            } else {
                // Обработка ошибки загрузки изображения по умолчанию
                holder.earnImageView.setImageResource(R.drawable.person); // Загрузить изображение по умолчанию
            }
        });

        // Отправка данных при нажатии на кнопку
        holder.earnAddButton.setOnClickListener(v -> {
            double earnSum = nameToSum.get(name);

            HashMap<String, Object> earnData = new HashMap<>();
            earnData.put("earn_category", documents.get(position).getReference());
            earnData.put("date", new Timestamp(new Date()));
            earnData.put("sum", earnSum);
            earnData.put("user_id", currentUserUid);
            earnData.put("member", selectedFamilyMember);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("earns").add(earnData).addOnSuccessListener(command -> {
                db.collection("families").document(currentUserUid).get().addOnSuccessListener(documentSnapshot -> {
                    double currentBudget = documentSnapshot.getDouble("budget");
                    // Обновляем бюджет, добавляя к текущему значению сумму дохода
                    double newBudget = currentBudget + earnSum;
                    // Обновляем значение бюджета в базе данных
                    db.collection("families").document(currentUserUid).update("budget", newBudget)
                            .addOnSuccessListener(aVoid -> {
                                // Очищаем поле ввода
                                holder.earnSumTextInput.setText("");
                                // Оповещаем пользователя
                                Snackbar.make(v.getContext(), v, "Добавлен доход в категорию " + name, Snackbar.LENGTH_SHORT)
                                        .show();
                            })
                            .addOnFailureListener(e -> Log.e("Error", "Error updating budget", e));
                });
            });
        });


        // при изменении текста в поле
        holder.earnSumTextInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            // Сохранение значение в словарь и включение кнопки, если это double
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    nameToSum.put(name, Double.parseDouble(s.toString()));
                    holder.earnAddButton.setEnabled(true);
                } catch (NumberFormatException e) {
                    holder.earnAddButton.setEnabled(false);
                }
            }
        });

        if (nameToSum.get(name) != null) {
            holder.earnSumTextInput.setText(nameToSum.get(name).toString());
        } else {
            holder.earnSumTextInput.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

}
