package co.edu.unal.miapenahu.chartsapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Pair;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class GetHttpData extends AsyncTask {
    private List<Object> httpList;
    private PieChart pieChart;
    private String centerText;
    private String dataSetText;
    private String acum_value;
    private String value;
    private int limit;

    private Context httpContext;
    ProgressDialog progressDialog;

    public GetHttpData(List<Object> httpList, Context httpContext, PieChart pieChart, int limit, String centerText, String dataSetText, String acum_value, String value) {
        this.httpList = httpList;
        this.httpContext = httpContext;
        this.pieChart = pieChart;
        this.limit = limit;
        this.centerText = centerText;
        this.dataSetText = dataSetText;
        this.acum_value = acum_value;
        this.value = value;
    }

    @Override
    protected void onPreExecute(){
        super.onPreExecute();
        progressDialog = ProgressDialog.show(httpContext,"Descargando", "Por favor espere");
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        System.out.println("objects: "+objects[0].toString());
        Object result = null;
        try {
            String wsURL;
            if(objects.length > 0) {
                wsURL = "https://www.datos.gov.co/resource/gaic-b8aw.json?$" + objects[0].toString();
            } else{
                wsURL = "https://www.datos.gov.co/resource/gaic-b8aw.json";
            }
            URL url = new URL(wsURL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            result = inputStreamToString(in);
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(Object obj){
        super.onPostExecute(obj);
        progressDialog.dismiss();
        try{
            System.out.println(obj.toString());
            //JSONObject jsonObject = new JSONObject(URLDecoder.decode(obj.toString(),"UTF-8"));
            String test = "[{\"fecha\":\"2021-12-03T00:00:00.000\",\"fecha_corte\":\"2018-01-31T00:00:00.000\",\"a_o\":\"2018\",\"mes\":\"Enero\",\"departamento\":\"Antioquia\",\"cod_depto\":\"5\",\"producto\":\"Caballos\",\"descripcion_partida4_dig\":\"0101-Caballos, asnos, mulos y burdéganos, vivos.\",\"descripcion_partida4_dig_1\":\"Caballos reproductores de raza pura, vivos\",\"partida\":\"101210000\",\"tradici_n_producto\":\"No tradicional\",\"exportaciones_en_valor_usd\":\"9\",\"exportaciones_en_volumen\":\"2\"}]";
            //JSONObject jsonObject = new JSONObject(test);
            //JSONArray jsonArray = jsonObject.toJSONArray(jsonObject.names());
            JSONArray jsonArray = new JSONArray(obj.toString());
            if(jsonArray.getJSONObject(0).has("fecha")) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    String fecha = jsonArray.getJSONObject(i).getString("fecha");
                    //System.out.println(fecha);
                    String fecha_corte = jsonArray.getJSONObject(i).getString("fecha_corte");
                    //System.out.println(fecha_corte);
                    String a_o = jsonArray.getJSONObject(i).getString("a_o");
                    //System.out.println(a_o);
                    String mes = jsonArray.getJSONObject(i).getString("mes");
                    //System.out.println(mes);
                    String departamento = jsonArray.getJSONObject(i).getString("departamento");
                    //System.out.println(departamento);
                    this.httpList.add((Object) fecha);
/*

                String cod_depto;
                String producto;
                String descripcion_partida4_dig;
                String descripcion_partida4_dig_1;
                String partida;
                String tradici_n_producto;
                String exportaciones_en_valor_usd;
                String exportaciones_en_volumen;*/
                }
            } else if(jsonArray.getJSONObject(0).has(acum_value)){
                long total = 0;
                ArrayList<Pair<Long, String>> values = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String tradici_n_producto = jsonArray.getJSONObject(i).getString(value);
                    long count_tradici_n_producto = Integer.parseInt(jsonArray.getJSONObject(i).getString(acum_value));
                    total += count_tradici_n_producto;
                    Pair<Long, String> ans = new Pair<>(count_tradici_n_producto, tradici_n_producto);
                    values.add(ans);
                }
                loadPieChartData(values,total, limit);
            } /*else if(jsonArray.getJSONObject(0).has("sum_exportaciones_en_volumen")){
                long total = 0;
                ArrayList<Pair<Long, String>> values = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String departamento = jsonArray.getJSONObject(i).getString("departamento");
                    long sum_exportaciones_en_volumen = Integer.parseInt(jsonArray.getJSONObject(i).getString("sum_exportaciones_en_volumen"));
                    total += sum_exportaciones_en_volumen;
                    Pair<Long, String> ans = new Pair<>(sum_exportaciones_en_volumen, departamento);
                    values.add(ans);
                }
                loadPieChartData(values,total, limit);
            } else if(jsonArray.getJSONObject(0).has("count_producto")){
                long total = 0;
                ArrayList<Pair<Long, String>> values = new ArrayList<>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    String producto = jsonArray.getJSONObject(i).getString("producto");
                    long count_producto = Integer.parseInt(jsonArray.getJSONObject(i).getString("count_producto"));
                    total += count_producto;
                    Pair<Long, String> ans = new Pair<>(count_producto, producto);
                    values.add(ans);
                }
                loadPieChartData(values,total, limit);
            }*/
            System.out.println("httplist size: "+httpList.size());
        } catch (JSONException /*| UnsupportedEncodingException*/ e){
            e.printStackTrace();
        }
    }

    private String inputStreamToString(InputStream is){
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader rd = new BufferedReader(isr);
        try {
            while ((rLine = rd.readLine()) != null){
                answer.append(rLine);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return  answer.toString();
    }

    private void loadPieChartData(ArrayList<Pair<Long,String>> values, long total, int limit){
        pieChart.setCenterText(centerText);
        ArrayList<PieEntry> entries = new ArrayList<>();

        long acum = 0;

        for(int i = 0; i < (limit != 0 ? limit : values.size()); i++){
        //for(int i = 0; i < values.size(); i++){
            Pair<Long, String> res = values.get(i);
            entries.add(new PieEntry((float) res.first / total, res.second));
            acum += res.first;
        }

        if(limit != 0){
            entries.add(new PieEntry((float) (total - acum) / total, "Otros"));
        }

        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }

        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }

        PieDataSet dataSet = new PieDataSet(entries, dataSetText);
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
}
