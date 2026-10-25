package com.example.fotobitacora.ui.login

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fotobitacora.R
import com.example.fotobitacora.data.DatabaseHelper
import com.example.fotobitacora.ui.main.MainActivity
import com.example.fotobitacora.utils.SessionManager
import com.example.fotobitacora.utils.UiUtils
import java.util.Random

class LoginActivity : AppCompatActivity() {

    private lateinit var db: DatabaseHelper
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DatabaseHelper(this)
        session = SessionManager(this)

        // Si existe una sesión, abre la pantalla principal.
        if (session.userId > 0L) {
            openHome()
            return
        }

        // Primero se carga el XML.
        setContentView(R.layout.activity_login)

        // Después se aplica la textura sobre su fondo.
        aplicarTexturaFondo()

        UiUtils.setInsets(this, findViewById(R.id.rootLayout))

        configurarVisibilidadPassword()

        val email = findViewById<EditText>(R.id.editEmail)
        val password = findViewById<EditText>(R.id.editPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        btnLogin.setOnClickListener {
            val typedEmail = email.text.toString().trim()
            val typedPass = password.text.toString()

            if (typedEmail.isBlank() || typedPass.isBlank()) {
                Toast.makeText(
                    this,
                    R.string.login_missing_credentials,
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val userId = db.login(typedEmail, typedPass)

            if (userId == null) {
                Toast.makeText(
                    this,
                    R.string.login_invalid_credentials,
                    Toast.LENGTH_LONG
                ).show()
            } else {
                session.login(userId)
                openHome()
            }
        }

        btnRegister.setOnClickListener {
            startActivity(
                Intent(this, RegisterActivity::class.java)
            )
        }
    }

    private fun aplicarTexturaFondo() {
        val root = findViewById<View>(R.id.rootLayout)
        val fondoActual = root.background ?: return

        val medida = 128

        val bitmap = Bitmap.createBitmap(
            medida,
            medida,
            Bitmap.Config.ARGB_8888
        )

        val random = Random(42L)
        val pixeles = IntArray(medida * medida)

        for (indice in pixeles.indices) {
            val transparencia = random.nextInt(13)
            val tono = if (random.nextBoolean()) 255 else 0

            pixeles[indice] = Color.argb(
                transparencia,
                tono,
                tono,
                tono
            )
        }

        bitmap.setPixels(
            pixeles,
            0,
            medida,
            0,
            0,
            medida,
            medida
        )

        val textura = BitmapDrawable(resources, bitmap).apply {
            tileModeX = Shader.TileMode.REPEAT
            tileModeY = Shader.TileMode.REPEAT
        }

        root.background = LayerDrawable(
            arrayOf(fondoActual, textura)
        )
    }

    private fun configurarVisibilidadPassword() {
        val passwordField = findViewById<EditText>(
            R.id.editPassword
        )

        val toggleButton = findViewById<ImageButton>(
            R.id.btnTogglePassword
        )

        toggleButton.setOnClickListener {
            val mostrar =
                passwordField.transformationMethod is PasswordTransformationMethod

            val inicio = passwordField.selectionStart
            val fin = passwordField.selectionEnd

            passwordField.transformationMethod =
                if (mostrar) {
                    HideReturnsTransformationMethod.getInstance()
                } else {
                    PasswordTransformationMethod.getInstance()
                }

            // Conserva la posición del cursor y la selección.
            if (inicio >= 0 && fin >= 0) {
                passwordField.setSelection(
                    inicio.coerceAtMost(passwordField.length()),
                    fin.coerceAtMost(passwordField.length())
                )
            }

            toggleButton.setImageResource(
                if (mostrar) {
                    R.drawable.ic_visibility_off
                } else {
                    R.drawable.ic_visibility
                }
            )

            toggleButton.contentDescription = getString(
                if (mostrar) {
                    R.string.login_hide_password
                } else {
                    R.string.login_show_password
                }
            )
        }
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}