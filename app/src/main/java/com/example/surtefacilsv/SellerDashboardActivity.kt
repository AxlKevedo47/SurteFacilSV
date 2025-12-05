package com.example.surtefacilsv

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.card.MaterialCardView

class SellerDashboardActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var tvBusinessNameTitle: TextView
    private lateinit var tvProductCount: TextView
    private lateinit var tvOrderCount: TextView
    private lateinit var btnReports: Button
    private lateinit var btnAddProductQuick: Button
    private lateinit var cardProducts: MaterialCardView
    private lateinit var cardOrders: MaterialCardView
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "El permiso de ubicación es necesario para mostrar el mapa.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller_dashboard)

        initViews()
        setupListeners()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val businessName = intent.getStringExtra("business_name")
        tvBusinessNameTitle.text = if (!businessName.isNullOrEmpty()) businessName else "Dashboard del Vendedor"

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun initViews() {
        tvBusinessNameTitle = findViewById(R.id.tvBusinessNameTitle)
        tvProductCount = findViewById(R.id.tvProductCount)
        tvOrderCount = findViewById(R.id.tvOrderCount)
        btnReports = findViewById(R.id.btnReports)
        btnAddProductQuick = findViewById(R.id.btnAddProductQuick)
        cardProducts = findViewById(R.id.cardProducts)
        cardOrders = findViewById(R.id.cardOrders)
    }

    private fun setupListeners() {
        cardProducts.setOnClickListener {
            startActivity(Intent(this, SellerProductListActivity::class.java))
        }

        cardOrders.setOnClickListener {
            startActivity(Intent(this, SellerOrdersActivity::class.java))
        }

        btnReports.setOnClickListener {
            Toast.makeText(this, "Mostrando reportes...", Toast.LENGTH_SHORT).show()
        }

        btnAddProductQuick.setOnClickListener {
            startActivity(Intent(this, AddEditProductActivity::class.java))
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true // Habilita el botón de 'Mi Ubicación'
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    lastKnownLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(currentLatLng).title("Ubicación del Negocio"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                } else {
                    Toast.makeText(this, "No se pudo obtener la ubicación actual. Asegúrate de tener el GPS activado.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
