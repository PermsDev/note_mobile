package com.d121211063.mynotesapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.d121211063.mynotesapp.databinding.ActivityNoteAddUpdateBinding
import com.d121211063.mynotesapp.db.DatabaseContract
import com.d121211063.mynotesapp.db.NoteHelper
import com.d121211063.mynotesapp.entity.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAddUpdateActivity : AppCompatActivity(), View.OnClickListener {
    private var isEdit = false
    private var note: Note? = null
    private var position: Int = 0
    private lateinit var noteHelper: NoteHelper

    private lateinit var binding: ActivityNoteAddUpdateBinding

    companion object {
        const val EXTRA_NOTE = "extra_note"
        const val EXTRA_POSITION = "extra_position"
        const val RESULT_ADD = 101
        const val RESULT_UPDATE = 201
        const val RESULT_DELETE = 301
        const val ALERT_DIALOG_CLOSE = 10
        const val ALERT_DIALOG_DELETE = 20
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteAddUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        noteHelper = NoteHelper.getInstance(applicationContext)
        noteHelper.open()

        note = intent.getParcelableExtra(EXTRA_NOTE)
        if (note != null) {
            position = intent.getIntExtra(EXTRA_POSITION, 0)
            isEdit = true
        } else {
            note = Note()
        }

        val actionBarTitle: String
        val btnTitle: String

        if (isEdit) {
            actionBarTitle = "Ubah"
            btnTitle = "Update"

            note?.let {
                binding.edtTitle.setText(it.title)
                binding.edtDescription.setText(it.description)
            }

            binding.btnPin.visibility = View.VISIBLE

        } else {
            actionBarTitle = "Tambah"
            btnTitle = "Simpan"

            binding.btnPin.visibility = View.GONE
        }

        supportActionBar?.title = actionBarTitle
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSubmit.text = btnTitle

        binding.btnSubmit.setOnClickListener(this)

        binding.btnPin.setOnClickListener {
            val title = binding.edtTitle.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                pinNoteToWidget(title, description)
                Toast.makeText(this, "Note pinned to widget", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please fill in both title and description", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onClick(view: View) {
        if (view.id == R.id.btn_submit) {
            val title = binding.edtTitle.text.toString().trim()
            val description = binding.edtDescription.text.toString().trim()

            if (title.isEmpty()) {
                binding.edtTitle.error = "Field can not be blank"
                return
            }

            note?.title = title
            note?.description = description

            val intent = Intent()
            intent.putExtra(EXTRA_NOTE, note)
            intent.putExtra(EXTRA_POSITION, position)

            val values = ContentValues()
            values.put(DatabaseContract.NoteColumns.TITLE, title)
            values.put(DatabaseContract.NoteColumns.DESCRIPTION, description)

            if (isEdit) {
                val result = noteHelper.update(note?.id.toString(), values)
                if (result > 0) {
                    saveNoteToPreferences(title, description)
                    updateWidget(this)

                    setResult(RESULT_UPDATE, intent)
                    finish()
                } else {
                    Toast.makeText(this@NoteAddUpdateActivity, "Gagal mengupdate data", Toast.LENGTH_SHORT).show()
                }
            } else {
                note?.date = getCurrentDate()
                values.put(DatabaseContract.NoteColumns.DATE, getCurrentDate())
                val result = noteHelper.insert(values)

                if (result > 0) {
                    note?.id = result.toInt()
                    setResult(RESULT_ADD, intent)
                    finish()
                } else {
                    Toast.makeText(this@NoteAddUpdateActivity, "Gagal menambah data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault())
        val date = Date()

        return dateFormat.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isEdit) {
            menuInflater.inflate(R.menu.menu_form, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> showAlertDialog(ALERT_DIALOG_DELETE)
            android.R.id.home -> showAlertDialog(ALERT_DIALOG_CLOSE)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showAlertDialog(ALERT_DIALOG_CLOSE)
    }

    private fun showAlertDialog(type: Int) {
        val isDialogClose = type == ALERT_DIALOG_CLOSE
        val dialogTitle: String
        val dialogMessage: String

        if (isDialogClose) {
            dialogTitle = "Batal"
            dialogMessage = "Apakah anda ingin membatalkan perubahan pada form?"
        } else {
            dialogMessage = "Apakah anda yakin ingin menghapus item ini?"
            dialogTitle = "Hapus Note"
        }

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle(dialogTitle)
        alertDialogBuilder
            .setMessage(dialogMessage)
            .setCancelable(false)
            .setPositiveButton("Ya") { _, _ ->
                if (isDialogClose) {
                    finish()
                } else {
                    val result = noteHelper.deleteById(note?.id.toString()).toLong()
                    if (result > 0) {
                        note?.let { deletePinnedNote(it) }
                        setResult(RESULT_DELETE, intent)
                        finish()
                    } else {
                        Toast.makeText(this@NoteAddUpdateActivity, "Gagal menghapus data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Tidak") { dialog, _ -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun pinNoteToWidget(title: String, description: String) {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val remoteViews = RemoteViews(packageName, R.layout.widget_layout)
        val componentName = ComponentName(this, WidgetProvider::class.java)

        saveNoteToPreferences(title, description)

        // Update widget
        remoteViews.setTextViewText(R.id.widget_title, title)
        remoteViews.setTextViewText(R.id.widget_content, description)
        remoteViews.setViewVisibility(R.id.widget_button, View.GONE)
        appWidgetManager.updateAppWidget(componentName, remoteViews)
    }

    private fun saveNoteToPreferences(title: String, description: String) {
        val sharedPreferences = getSharedPreferences("PinnedNote", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("title", title)
            putString("description", description)
            apply()
        }
    }

    private fun updateWidget(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
        val componentName = ComponentName(context, WidgetProvider::class.java)

        // Get data from SharedPreferences
        val prefs = context.getSharedPreferences("PinnedNote", Context.MODE_PRIVATE)
        val title = prefs.getString("title", "No Title")
        val description = prefs.getString("description", "No Description")

        remoteViews.setTextViewText(R.id.widget_title, title)
        remoteViews.setTextViewText(R.id.widget_content, description)

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        remoteViews.setOnClickPendingIntent(R.id.widget_button, pendingIntent)

        appWidgetManager.updateAppWidget(componentName, remoteViews)
    }

    private fun updateWidgetToDefault(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.widget_layout)
        val componentName = ComponentName(context, WidgetProvider::class.java)

        remoteViews.setTextViewText(R.id.widget_title, "Judul")
        remoteViews.setTextViewText(R.id.widget_content, "Tidak ada note yang dipilih, pin terlebih dahulu!")
        remoteViews.setViewVisibility(R.id.widget_button, View.VISIBLE);

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        remoteViews.setOnClickPendingIntent(R.id.widget_button, pendingIntent)

        appWidgetManager.updateAppWidget(componentName, remoteViews)
    }

    private fun deletePinnedNote(note: Note) {
        val sharedPreferences = getSharedPreferences("PinnedNote", Context.MODE_PRIVATE)
        val pinnedTitle = sharedPreferences.getString("title", null)
        val pinnedDescription = sharedPreferences.getString("description", null)

        if (note.title == pinnedTitle && note.description == pinnedDescription) {
            with(sharedPreferences.edit()) {
                clear()
                apply()
            }
            updateWidgetToDefault(this)
        }
    }
}
