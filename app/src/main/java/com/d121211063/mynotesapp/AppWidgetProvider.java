//package com.d121211063.mynotesapp;
//
//import android.appwidget.AppWidgetProvider;
//import android.appwidget.AppWidgetManager;
//import android.content.Context;
//import android.widget.RemoteViews;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//public class TimeWidgetProvider extends AppWidgetProvider {
//
//    @Override
//    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        // Mendapatkan waktu saat ini
//        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
//
//        // Untuk setiap widget yang terpasang
//        for (int appWidgetId : appWidgetIds) {
//            // Mendapatkan layout widget
//            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
//
//            // Menampilkan waktu pada widget
//            views.setTextViewText(R.id.timeText, currentTime);
//
//            // Memperbarui widget
//            appWidgetManager.updateAppWidget(appWidgetId, views);
//        }
//    }
//
//    @Override
//    public void onEnabled(Context context) {
//        super.onEnabled(context);
//        // Jika widget pertama kali ditambahkan, Anda bisa menambahkan logika inisialisasi di sini
//    }
//
//    @Override
//    public void onDisabled(Context context) {
//        super.onDisabled(context);
//        // Jika widget dihapus, Anda bisa menambahkan logika pembersihan di sini
//    }
//}
