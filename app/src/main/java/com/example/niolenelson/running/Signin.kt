package com.example.niolenelson.running

import com.example.niolenelson.running.utilities.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import android.content.Intent
import android.widget.Toast
import com.example.niolenelson.running.utilities.LocationPermissionHandler
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import java.util.logging.Logger


/**
 * Created by niolenelson on 10/7/18.
 */
class SigninActivity : AppCompatActivity(), View.OnClickListener {

    val logger: Logger = logger()

    val RC_SIGN_IN = 0

    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.signing_activity)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        setSigninButtonClick()
    }

    override fun onStart() {
        super.onStart()

        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            // launch init routes view
           onSignInSuccess(account)
        }
    }

    private fun onSignInSuccess(account: GoogleSignInAccount) {
        logger.info("Successful signin for account: $account")

        LocationPermissionHandler.getLocationPermission(this, {
            val intent = Intent(this, InitRoutesActivity::class.java)
            logger.info("Granted location permission")
            startActivity(intent)
        })
    }

    override fun onClick(view: View) {
        val viewId = view.id
        if (viewId == R.id.sign_in_button) {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun setSigninButtonClick() {
        val button = findViewById<SignInButton>(R.id.sign_in_button)
        button.setOnClickListener(this)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        } else {
            logger.warning("Unhandled google signin code returned on attempted auth. code: $requestCode")
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java!!)
            if (account != null) {
                onSignInSuccess(account)
            } else {
                logger.info("No account associated with username and password")
                Toast.makeText(this, "There is no account associated with this username and passsword", Toast.LENGTH_LONG)
            }
        } catch (e: ApiException) {
            logger.warning("Unable to complete google signin flow. error: $e")
        }

    }

}
