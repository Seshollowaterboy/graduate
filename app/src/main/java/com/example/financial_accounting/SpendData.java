package com.example.financial_accounting;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

// вспомогательный класс, предствляющий собой элемент затрат(помогает для фильтрации)
public class SpendData {
    String memberName;
    Date date;

    Double sum;

    public SpendData(String memberName, Date date, Double sum){
        this.memberName = memberName;
        this.date = date;
        this.sum = sum;
    }
}

