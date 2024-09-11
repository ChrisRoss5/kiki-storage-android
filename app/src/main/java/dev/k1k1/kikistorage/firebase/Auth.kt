package dev.k1k1.kikistorage.firebase

import android.app.Activity
import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import dev.k1k1.kikistorage.SignInActivity
import dev.k1k1.kikistorage.framework.startActivity

object Auth {
    val auth = FirebaseAuth.getInstance()

    fun getUid(): String? = auth.uid

    fun signOut(context: Context) {
        AuthUI.getInstance().signOut(context).addOnCompleteListener {
            (context as? Activity)?.finish()
            context.startActivity<SignInActivity>()
        }
    }
}