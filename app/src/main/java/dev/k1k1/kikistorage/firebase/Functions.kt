package dev.k1k1.kikistorage.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableResult
import dev.k1k1.kikistorage.BuildConfig

object Functions {
    private val functions = FirebaseFunctions.getInstance()

    init {
        if (BuildConfig.DEBUG) {

            // https://developer.android.com/studio/run/emulator-networking
            // 10.0.2.2	Special alias to your host loopback interface (127.0.0.1 on your development machine)
            functions.useEmulator("10.0.2.2", 5001)
        }
    }

    fun deleteAccount(): Task<HttpsCallableResult> {
        return functions.getHttpsCallable("deleteAccount").call()
    }
}