package com.example.financial_accounting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.financial_accounting.databinding.ActivityEarnStatisticsBinding;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class EarnStatisticsActivity extends AppCompatActivity {

    private ActivityEarnStatisticsBinding binding;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private String userId = FirebaseAuth.getInstance().getUid();

    private List<EarnData> earns = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_earn_statistics);

        binding = ActivityEarnStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db.collection("earns").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (Objects.equals(document.getString("user_id"), userId)) {
                        earns.add(new EarnData(document.getString("member"), document.getDate("date"), document.getDouble("sum")));
                        Log.i("EarnStatisticsActivity", "earn: " + document.getDouble("sum"));
                    }
                }
                Log.i("EarnStatisticsActivity", "earns: " + earns);
            }
        });

        binding.earnStatisticsNavView.setNavigationItemSelectedListener(this::onNavigationItemSelected);

        // настройка toggle btn(можем выбрать только 1 элемент)
        binding.earnToggleButton.check(binding.earnBtnDay.getId());
        binding.earnToggleButton.setSingleSelection(true);
        binding.earnToggleButton.setSelectionRequired(true);

        // настройка графика
        binding.earnChart.setFitBars(true);

        // что будет при выбранном значении
        binding.earnToggleButton.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            // Содержимое графика
            List<BarEntry> entries = new ArrayList<>();
            List<String> categoryNames = new ArrayList<>();

            // Группировка по категориям
            HashMap<String, Double> categoryToSum = new HashMap<>();

            int index = 0;

            binding.earnBtnPeriod.setText("Период");

            // День
            if (checkedId == binding.earnBtnDay.getId()) {
                for (EarnData data : earns) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(data.date);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(new Date());
                    // траты за сегодня
                    if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                            cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)) {
                        categoryToSum.merge(data.memberName, data.sum, Double::sum);
                    }
                }
            }
            // Месяц
            else if (checkedId == binding.earnBtnMonth.getId()) {
                for (EarnData data : earns) {
                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(data.date);
                    Calendar cal2 = Calendar.getInstance();
                    cal2.setTime(new Date());
                    if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                            cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)) {
                        categoryToSum.merge(data.memberName, data.sum, Double::sum);
                    }
                }
            }

            for (String categoryName : categoryToSum.keySet()) {
                categoryNames.add(categoryName);
                // построение графиков
                entries.add(new BarEntry(index++, categoryToSum.get(categoryName).floatValue()));
            }

            // подписи
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    if ((int) value >= categoryNames.size()) return "none";
                    return categoryNames.get((int) value);
                }
            };
            // настройка графика
            setEntries(entries, formatter);
        });
    }

    public void showDatePicker(View view) {
        // Создаем экземпляр MaterialDatePicker для выбора даты
        MaterialDatePicker<Pair<Long, Long>> datePicker =
                MaterialDatePicker.Builder.dateRangePicker()
                        .setTitleText("Выберите дату")
                        .build();

        // Устанавливаем слушатель для обработки события выбора даты
        datePicker.addOnPositiveButtonClickListener(selection -> {
            List<BarEntry> entries = new ArrayList<>();
            List<String> categoryNames = new ArrayList<>();
            int index = 0;

            Date date1 = new Date(selection.first);
            // убираем один день для того чтобы этот день был включительно в периоде
            Date date2 = new Date(selection.second + 24 * 60 * 60 * 1000);
            HashMap<String, Double> categoryToSum = new HashMap<>();
            for (EarnData data : earns) {
                if (data.date.after(date1) && data.date.before(date2)) {
                    categoryToSum.merge(data.memberName, data.sum, Double::sum);
                }
            }
            binding.earnBtnPeriod.setText(datePicker.getHeaderText());

            for (String categoryName : categoryToSum.keySet()) {
                categoryNames.add(categoryName);
                entries.add(new BarEntry(index++, categoryToSum.get(categoryName).floatValue()));
            }

            // подписи
            ValueFormatter formatter = new ValueFormatter() {
                @Override
                public String getAxisLabel(float value, AxisBase axis) {
                    if ((int) value >= categoryNames.size()) return "none";
                    return categoryNames.get((int) value);
                }
            };
            setEntries(entries, formatter);
        });

        // Показываем DatePicker
        datePicker.show(getSupportFragmentManager(), "datePicker");
    }

    // вспомогательная функция для графики
    private void setEntries(List<BarEntry> entries, ValueFormatter formatter) {
        // Установка значений
        BarDataSet set = new BarDataSet(entries, "BarDataSet");

        // Украшения графика
        set.setColors(0xFF124E78, 0xFFF0F0C9, 0xFFF2BB05, 0xFFD74E09, 0xFF6E0E0A);
        BarData barData = new BarData(set);
        barData.setBarWidth(0.9f);
        binding.earnChart.getXAxis().setGranularity(1f);
        binding.earnChart.getXAxis().setValueFormatter(formatter);
        binding.earnChart.setData(barData);
        binding.earnChart.invalidate();
    }

    public void topAppBar(View view) {
        DrawerLayout drawerLayout = findViewById(binding.earnStatisticsDrawerLayout.getId());

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