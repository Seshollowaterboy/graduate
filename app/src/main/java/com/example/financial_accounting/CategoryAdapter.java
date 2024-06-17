package com.example.financial_accounting;

import android.annotation.SuppressLint;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

// RecycleView всех категорий
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    // Список всех категорий в виде документа
    List<QueryDocumentSnapshot> documents = new ArrayList<>();
    // Словарь (название категории к сумме) для сохранения при скроллинге
    HashMap<String, Double> nameToSum = new HashMap<>();

    // Режим добавления
    Boolean isAdding = true;

    private String selectedFamilyMember;
    private String currentUserUid;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextInputEditText sumTextInput;
        final TextInputLayout sumTextInputLayout;
        final Button addButton;

        final TextView sumText;

        final TextView categoryName;

        final TextView dateView;

        final TextView categoryFamilyMember;

        final TextView categoryNameAdding;

        // получение элементов
        public ViewHolder(View view) {
            super(view);
            sumTextInput = view.findViewById(R.id.categorySumEdit);
            imageView = view.findViewById(R.id.categoryImage);
            addButton = view.findViewById(R.id.Btn1);
            sumText = view.findViewById(R.id.sumText);
            categoryName = view.findViewById(R.id.categoryName);
            dateView = view.findViewById(R.id.categoryDate);
            categoryFamilyMember = view.findViewById(R.id.categoryFamilyMember);
            categoryNameAdding = view.findViewById(R.id.categoryNameAdding);
            sumTextInputLayout = view.findViewById(R.id.categorySum);
        }
    }

    public CategoryAdapter() {
    }

    public CategoryAdapter(Boolean isAdding) {
        this.isAdding = isAdding;
    }

    public CategoryAdapter(String selectedFamilyMember, String currentUserUid) {
        this.selectedFamilyMember = selectedFamilyMember;
        this.currentUserUid = currentUserUid;
    }

    // создание отображения элемента списка(шаблон для разметки)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_category, parent, false);

        return new ViewHolder(view);
    }

    // изменение отображения под каждый элемент списка
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.addButton.setEnabled(false);

        // история трат
        if (!isAdding) {

            // изменение отображения
            holder.sumTextInputLayout.setVisibility(View.GONE);
            holder.sumText.setText(documents.get(position).getLong("sum").toString() + " р.");
            holder.categoryFamilyMember.setText(documents.get(position).getString("member"));
            holder.dateView.setText(new SimpleDateFormat("dd.MM.yyyy").format(documents.get(position).getDate("date")));
            holder.sumText.setVisibility(View.VISIBLE);
            holder.categoryNameAdding.setVisibility(View.GONE);
            holder.dateView.setVisibility(View.VISIBLE);
            holder.categoryFamilyMember.setVisibility(View.VISIBLE);
            holder.sumTextInput.setVisibility(View.GONE);

            holder.addButton.setVisibility(View.GONE);

            // получение названия категории
            documents.get(position).getDocumentReference("category").get().addOnSuccessListener(documentSnapshot -> {
                String name = documentSnapshot.getString("name");
                holder.categoryName.setText(name);
                holder.categoryName.setVisibility(View.VISIBLE);

                // Проверяем, существует ли файл изображения в Firebase Storage
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference imageRef = storage.getReference().child(name.toLowerCase(Locale.ROOT) + ".png");

                imageRef.getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Файл существует, загружаем изображение
                        Uri uri = task.getResult();
                        Picasso.get().load(uri).into(holder.imageView); // Отображаем полученное изображение
                    } else {
                        // Обработка ошибки загрузки изображения по умолчанию
                        holder.imageView.setImageResource(R.drawable.person); // Загрузить изображение по умолчанию
                    }
                });
            });

            return;
        }

        // режим добавления трат
        String name = documents.get(position).getString("name");
        holder.categoryNameAdding.setText(name);

        // Загрузка изображения из Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageRef = storage.getReference().child(name.toLowerCase(Locale.ROOT) + ".png");

        imageRef.getDownloadUrl().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Файл существует, загружаем изображение
                Uri uri = task.getResult();
                Picasso.get().load(uri).into(holder.imageView); // Отображаем полученное изображение
            } else {
                // Обработка ошибки загрузки изображения по умолчанию
                holder.imageView.setImageResource(R.drawable.person); // Загрузить изображение по умолчанию
            }
        });


        // Отправка данных при нажатии на кнопку
        holder.addButton.setOnClickListener(v -> {
            double spendSum = nameToSum.get(name);

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("families").document(currentUserUid).get().addOnSuccessListener(documentSnapshot -> {
                double currentBudget = documentSnapshot.getDouble("budget");

                // Проверяем, хватает ли бюджета на покупку
                if (currentBudget >= spendSum) {
                    HashMap<String, Object> spendData = new HashMap<>();
                    spendData.put("category", documents.get(position).getReference());
                    spendData.put("date", new Timestamp(new Date()));
                    spendData.put("sum", nameToSum.get(name));
                    spendData.put("user_id", currentUserUid);
                    spendData.put("member", selectedFamilyMember);

                    db.collection("spends").add(spendData).addOnSuccessListener(command -> {
                        // Обновляем бюджет, вычитая сумму трата
                        double newBudget = currentBudget - spendSum;
                        // Обновляем значение бюджета в базе данных
                        db.collection("families").document(currentUserUid).update("budget", newBudget)
                                .addOnSuccessListener(aVoid -> {
                                    // Очищаем поле ввода
                                    holder.sumTextInput.setText("");
                                    // Оповещаем пользователя об успешном добавлении трата
                                    Snackbar.make(v.getContext(), v, "Добавлен трата в категорию " + name, Snackbar.LENGTH_SHORT)
                                            .show();
                                })
                                .addOnFailureListener(e -> Log.e("Error", "Error updating budget", e));
                    });
                } else {
                    // Сообщаем пользователю, что не хватает денег на покупку
                    Snackbar.make(v.getContext(), v, "Не хватает денег на покупку", Snackbar.LENGTH_SHORT)
                            .show();
                }
            });
        });

        // при изменении текста в поле
        holder.sumTextInput.addTextChangedListener(new TextWatcher() {

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
                    holder.addButton.setEnabled(true);
                } catch (NumberFormatException e) {
                    holder.addButton.setEnabled(false);
                }
            }
        });

        if (nameToSum.get(name) != null) {
            System.out.println(name);
            holder.sumTextInput.setText(nameToSum.get(name).toString());
        }else{
            holder.sumTextInput.setText("");
        }
    }

    // получение количества элементов
    @Override
    public int getItemCount() {
        return documents.size();
    }
}