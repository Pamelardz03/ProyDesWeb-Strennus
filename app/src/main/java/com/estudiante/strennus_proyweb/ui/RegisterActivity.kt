package com.estudiante.strennus_proyweb.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.estudiante.strennus_proyweb.data.AppDataBase
import com.estudiante.strennus_proyweb.databinding.ActivityRegisterBinding
import com.estudiante.strennus_proyweb.entities.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val nombre = binding.etName.text.toString().trim()
            val usuario = binding.etUsername.text.toString().trim()
            val correo = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (nombre.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val db = AppDataBase.getInstance(this)

            CoroutineScope(Dispatchers.IO).launch {
                val nuevoUsuario = Usuario(
                    name = nombre,
                    username = usuario,
                    correo = correo,
                    password = password,
                    fechaRegistro = System.currentTimeMillis()
                )
                db.usuarioDao().insert(nuevoUsuario)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RegisterActivity, "Cuenta creada", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegisterActivity, LogInActivity::class.java))
                    finish()
                }
            }
        }
    }
}