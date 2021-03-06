package co.edu.unal.miapenahu.chartsapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private PieChart pieChart;
    private PieChart pieChart2;
    private PieChart pieChart3;
    private List<Object> httpList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pieChart = findViewById(R.id.activity_main_piechart);
        pieChart2 = findViewById(R.id.activity_main_piechart2);
        pieChart3 = findViewById(R.id.activity_main_piechart3);

        setupPieChart(pieChart);
        setupPieChart(pieChart2);
        setupPieChart(pieChart3);

        httpList = new ArrayList<>();

        getData();
    }

    private void getData(){
        GetHttpData wsData1 = new GetHttpData(httpList, MainActivity.this, pieChart,0, "Distribución Productos", "Tradición Producto","count_tradici_n_producto","tradici_n_producto");
        Object query1 = (Object) "select=tradici_n_producto,count(tradici_n_producto)&$group=tradici_n_producto";
        wsData1.execute(query1);
        GetHttpData wsData2 = new GetHttpData(httpList, MainActivity.this, pieChart2,10, "Top 10 Exportadores por Cantidad", "Exporadores por Departamento","sum_exportaciones_en_volumen","departamento");
        Object query2 = (Object) "select=departamento,sum(exportaciones_en_volumen)&$group=departamento&$order=sum_exportaciones_en_volumen%20DESC";
        wsData2.execute(query2);
        GetHttpData wsData3 = new GetHttpData(httpList, MainActivity.this, pieChart3,10, "Top 10 Exportadores por Valor en USD", "Exporadores por Departamento","sum_exportaciones_en_valor_usd","departamento");
        Object query3 = (Object) "select=departamento,sum(exportaciones_en_valor_usd)&$group=departamento&$order=sum_exportaciones_en_valor_usd%20DESC";
        wsData3.execute(query3);
    }

    private void setupPieChart(PieChart pieChart) {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(12);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setCenterText("Spending by Category");
        pieChart.setCenterTextSize(24);
        pieChart.getDescription().setEnabled(false);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTextColor(textColorBasedOnTheme());
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void loadPieChartData(PieChart pieChart) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(0.2f, "Food & Dining"));
        entries.add(new PieEntry(0.15f, "Medical"));
        entries.add(new PieEntry(0.10f, "Entertainment"));
        entries.add(new PieEntry(0.25f, "Electricity and Gas"));
        entries.add(new PieEntry(0.3f, "Housing"));

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, "Expense Category");
        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(12f);

        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(1400, Easing.EaseInOutQuad);
    }

    int textColorBasedOnTheme(){
        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                return Color.WHITE;
            case Configuration.UI_MODE_NIGHT_NO:
                return Color.BLACK;
            default:
                return Color.BLACK;
        }
    }

}