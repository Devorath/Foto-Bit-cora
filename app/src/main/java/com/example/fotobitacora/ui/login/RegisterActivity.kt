package com.example.fotobitacora.ui.login

import android.database.sqlite.SQLiteConstraintException
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fotobitacora.R
import com.example.fotobitacora.utils.UiUtils
import com.example.fotobitacora.data.DatabaseHelper

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        UiUtils.setInsets(this, findViewById(R.id.rootLayout))

        val name = findViewById<EditText>(R.id.editName)
        val lastName = findViewById<EditText>(R.id.editLastName)
        val email = findViewById<EditText>(R.id.editEmail)
        val password = findViewById<EditText>(R.id.editPassword)
        val confirm = findViewById<EditText>(R.id.editConfirm)

        findViewById<Button>(R.id.btnSaveUser).setOnClickListener {
            val n = name.text.toString().trim()
            val l = lastName.text.toString().trim()
            val e = email.text.toString().trim()
            val p = password.text.toString()
            val c = confirm.text.toString()
            when {
                n.isBlank() -> name.error = "El nombre es obligatorio"
                l.isBlank() -> lastName.error = "Los apellidos son obligatorios"
                !Patterns.EMAIL_ADDRESS.matcher(e).matches() -> email.error = "Correo no válido"
                p.length < 8 -> password.error = "Usa al menos 8 caracteres"
                p != c -> confirm.error = "Las contraseñas no coinciden"
                else -> {
                    try {
                        DatabaseHelper(this)
                            .use { db -> db.createUser(n, l, e, p) }
                        Toast.makeText(this, "Cuenta registrada. Inicia sesión.", Toast.LENGTH_LONG).show()
                        finish()
                    } catch (_: SQLiteConstraintException) {
                        email.error = "Este correo ya está registrado"
                    } catch (_: Exception) {
                        Toast.makeText(this, "No fue posible registrar al usuario", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
        findViewById<Button>(R.id.btnGoBack).setOnClickListener { finish() }
    }
}
