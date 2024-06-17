package com.example.financial_accounting;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FamilyMemberEditAdapter extends RecyclerView.Adapter<FamilyMemberEditAdapter.ViewHolder>{
    List<FamilyMember> documents = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextInputLayout familyMemberNameLayout;
        final TextInputLayout familyMemberStatusLayout;

        final TextInputEditText familyMemberNameInput;
        final TextInputEditText familyMemberStatusInput;

        final FloatingActionButton removeMemberBtn;


        public ViewHolder(@NonNull View view) {
            super(view);
            familyMemberNameLayout = view.findViewById(R.id.NameMemberCurrent);
            familyMemberStatusLayout = view.findViewById(R.id.StatusMemberCurrent);
            familyMemberNameInput = view.findViewById(R.id.NameMemberEditCurrent);
            familyMemberStatusInput = view.findViewById(R.id.StatusMemberEditCurrent);
            removeMemberBtn = view.findViewById(R.id.RemoveMemberButtonCurrent);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.family_member_edit, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FamilyMember document = documents.get(position);

        Log.i("FamilyMemberEditAdapter", holder.familyMemberNameInput.toString());

        // Присваиваем значения элементам, если они уже были введены
        holder.familyMemberNameInput.setText(document.getName());
        holder.familyMemberStatusInput.setText(document.getStatus());

        // Устанавливаем слушатели для сохранения данных при их изменении пользователем
        holder.familyMemberNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Сохраняем введенное имя в объект member
                document.setName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        holder.familyMemberStatusInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                document.setStatus(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.removeMemberBtn.setOnClickListener(v -> {
            // Удаление элемента из списка снепшотов
            documents.remove(position);
            // Уведомление адаптера об удалении элемента
            notifyItemRemoved(position);
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }
}
