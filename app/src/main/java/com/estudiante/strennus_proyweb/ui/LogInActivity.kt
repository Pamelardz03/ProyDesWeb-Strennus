package com.estudiante.strennus_proyweb.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.ActivityLoginBinding
import com.estudiante.strennus_proyweb.entities.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LogInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val usuario = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AppDataBase.getInstance(this)

            CoroutineScope(Dispatchers.IO).launch {
                val user = db.usuarioDao().login(usuario, password)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        // Guardar usuario logueado en SharedPreferences para usarse dentro de la app
                        val prefs = getSharedPreferences("strenuus_prefs", MODE_PRIVATE)
                        prefs.edit()
                            .putInt("usuario_id", user.id)
                            .putString("usuario_name", user.name)
                            .putString("usuario_username", user.username)
                            .putString("usuario_correo", user.correo)
                            .apply()

                        startActivity(Intent(this@LogInActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }

        binding.tvCreateAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvForgotPassword.setOnClickListener {
            val usuario = binding.etUsername.text.toString().trim()

            if (usuario.isEmpty()) {
                Toast.makeText(this, "Ingresa el usuario primero", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AppDataBase.getInstance(this)
            CoroutineScope(Dispatchers.IO).launch {
                val user = db.usuarioDao().getByName(usuario)
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        Toast.makeText(
                            this@LogInActivity,
                            "Se enviaron pasos de recuperación al correo ${user.correo}",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(this@LogInActivity, "Usuario no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Usuario de prueba
        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDataBase.getInstance(this@LogInActivity)
            val usuario = Usuario(
                name = "test",
                username = "tester",
                correo = "test@test.com",
                password = "1234",
                fechaRegistro = System.currentTimeMillis()
            )
            db.usuarioDao().insert(usuario)
        }
    }
}