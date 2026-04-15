package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.ui.HistorialFragment
import com.example.myapplication.ui.InicioFragment
import com.example.myapplication.ui.PerfilFragment
import com.example.myapplication.ui.ProductsFragment
import com.example.myapplication.workers.RecordatorioWorker
import com.google.android.material.navigation.NavigationView
import java.util.concurrent.TimeUnit

class InicioActivity : AppCompatActivity() {
    private lateinit var dlayMenu: DrawerLayout
    private lateinit var nvMenu: NavigationView
    private lateinit var ivMenu: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)

        dlayMenu = findViewById(R.id.dlayMenu)
        nvMenu = findViewById(R.id.nvMenu)
        ivMenu = findViewById(R.id.ivMenu)

        solicitarPermisoNotificaciones()
        programarRecordatorioDiario()

        ivMenu.setOnClickListener {
            dlayMenu.open()
        }

        nvMenu.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            dlayMenu.closeDrawers()

            when (menuItem.itemId) {
                R.id.itInicio -> replaceFragment(ProductsFragment())
                R.id.itHistorial -> replaceFragment(HistorialFragment())
                R.id.itPerfil -> replaceFragment(PerfilFragment())
                R.id.itProductos -> replaceFragment(ProductsFragment())
                R.id.itCarrito -> startActivity(Intent(this, CarritoActivity::class.java))
            }
            true
        }

        val fragmentoInicial = intent.getStringExtra("mostrarFragmento")
        when (fragmentoInicial) {
            "historial" -> replaceFragment(HistorialFragment())
            "productos" -> replaceFragment(ProductsFragment())
            else -> replaceFragment(ProductsFragment())
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.flayContenedor, fragment)
            .commit()
    }

    private fun solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1000)
            }
        }
    }

    private fun programarRecordatorioDiario() {
        val workRequest = PeriodicWorkRequestBuilder<RecordatorioWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("RecordatorioDiario", ExistingPeriodicWorkPolicy.KEEP, workRequest)
    }
}
