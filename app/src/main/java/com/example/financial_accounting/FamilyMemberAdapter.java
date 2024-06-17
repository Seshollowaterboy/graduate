package com.example.financial_accounting;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class FamilyMemberAdapter extends RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder> {

    List<FamilyMember> documents = new ArrayList<>();


    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextInputLayout familyMemberNameLayout;
        final TextInputLayout familyMemberStatusLayout;

        final TextInputEditText familyMemberNameInput;
        final TextInputEditText familyMemberStatusInput;


        public ViewHolder(@NonNull View view) {
            super(view);
            familyMemberNameLayout = view.findViewById(R.id.NameMember);
            familyMemberStatusLayout = view.findViewById(R.id.StatusMember);
            familyMemberNameInput = view.findViewById(R.id.NameMemberEdit);
            familyMemberStatusInput = view.findViewById(R.id.StatusMemberEdit);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.family_member, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        FamilyMember member = documents.get(position);

        // Присваиваем значения элементам, если они уже были введены
        holder.familyMemberNameInput.setText(member.getName());
        holder.familyMemberStatusInput.setText(member.getStatus());

        // Устанавливаем слушатели для сохранения данных при их изменении пользователем
        holder.familyMemberNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Сохраняем введенное имя в объект member
                member.setName(s.toString());
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
                // Сохраняем введенное состояние в объект member
                member.setStatus(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    public FamilyMemberAdapter(){}
}
