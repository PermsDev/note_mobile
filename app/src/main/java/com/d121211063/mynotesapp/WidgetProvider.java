package com.d121211063.mynotesapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences prefs = context.getSharedPreferences("PinnedNote", Context.MODE_PRIVATE);
        String title = prefs.getString("title", null);
        String description = prefs.getString("description", null);

        for (int appWidgetId : appWidgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            if (title == null || description == null) {
                views.setTextViewText(R.id.widget_title, "Judul");
                views.setTextViewText(R.id.widget_content, "Tidak ada catatan yang dipilih, pin terlebih dahulu!");
                views.setViewVisibility(R.id.widget_button, View.VISIBLE);
            } else {
                views.setTextViewText(R.id.widget_title, title);
                views.setTextViewText(R.id.widget_content, description);
                views.setViewVisibility(R.id.widget_button, View.GONE);
            }

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
