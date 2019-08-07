package com.tuomomees.myweatherapplication

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import kotlin.system.exitProcess

class SettingsActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("Settings", "Key: $key")

        if(key == "outfit_theme"){
            val alertDialog = AlertDialog.Builder(this@SettingsActivity).create()
            alertDialog.setTitle("Info")
            alertDialog.setMessage("Changing theme requires application restart.")
            alertDialog.setButton(
                AlertDialog.BUTTON_POSITIVE, "Restart app"
            ) { dialog, _ ->
                dialog.dismiss()
                restartApp()
            }
            alertDialog.setButton(
                AlertDialog.BUTTON_NEGATIVE, "Ignore"
            ) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
        val theme = sharedPref.getString("outfit_theme", "1")

        //TODO: autogenerate this
        if(theme == "0"){
            setTheme(R.style.Theme_App_Light)

        }
        if(theme == "1"){
            setTheme(R.style.Theme_App_Dark)

        }

        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    private fun restartApp(){
        val intent = Intent(this, MainActivity::class.java)
        this.startActivity(intent)
        this.finishAffinity()
    }
}