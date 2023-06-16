package com.dzboot.template.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import timber.log.Timber


/**
 * Singleton class to handle sign in with Google, facebook and with email and password
 */
class AccountManager private constructor(private val context: Context) {

   annotation class SignInFailReason {
      companion object {

         const val UNKNOWN = 0
         const val PROVIDER_COLLISION = 1
         const val WRONG_CREDENTIALS = 2
         const val EMAIL_NOT_VERIFIED = 3
      }
   }

   interface SignInCallbacks {

      fun onPreSignIn()
      fun onSignInSuccess()
      fun onSignInCanceled()
      fun onSignInFailed(@SignInFailReason reason: Int)
   }

   companion object {

      //TODO set web client id
      private const val WEB_CLIENT_ID = ""
      private const val GOOGLE_SIGN_IN_CODE = 6

      @Volatile
      private var INSTANCE: AccountManager? = null

      @Synchronized
      fun getInstance(context: Context) = INSTANCE
                                          ?: AccountManager(context)
                                                .also { INSTANCE = it }

      @SignInFailReason
      fun getReasonFromException(exp: Exception): Int {
         return when (exp) {
            is FirebaseAuthUserCollisionException -> SignInFailReason.PROVIDER_COLLISION
            is FirebaseAuthInvalidCredentialsException -> SignInFailReason.WRONG_CREDENTIALS
            is FirebaseAuthInvalidUserException -> SignInFailReason.WRONG_CREDENTIALS
            else -> SignInFailReason.UNKNOWN
         }
      }
   }

   private var signInCallbacks: SignInCallbacks? = null

   private val facebookCallbackManager by lazy { CallbackManager.Factory.create() }

   private val googleSignInClient by lazy {
      val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
      GoogleSignIn.getClient(context, gso)
   }

   fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

   fun googleSignIn(activity: Activity, signInCallbacks: SignInCallbacks) {
      this.signInCallbacks = signInCallbacks
      activity.startActivityForResult(
            googleSignInClient.signInIntent,
            GOOGLE_SIGN_IN_CODE
      )
   }

   fun facebookSignIn(activity: Activity, signInCallbacks: SignInCallbacks) {
      this.signInCallbacks = signInCallbacks
      LoginManager.getInstance()
            .registerCallback(facebookCallbackManager, object : FacebookCallback<LoginResult> {
               override fun onSuccess(result: LoginResult) {
                  handleFacebookSignInResult(result)
               }

               override fun onCancel() {
                  signInCallbacks.onSignInCanceled()
               }

               override fun onError(error: FacebookException?) {
                  error?.printStackTrace()
               }
            })

      LoginManager.getInstance().logInWithReadPermissions(activity, arrayListOf("email", "public_profile"))
   }

   fun signInWithEmailAndPassword(email: String, password: String, signInCallbacks: SignInCallbacks) {
      signInCallbacks.onPreSignIn()
      FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
               val user = it.user
               if (user == null) {
                  signInCallbacks.onSignInFailed(SignInFailReason.WRONG_CREDENTIALS)
                  return@addOnSuccessListener
               }

               if (!user.isEmailVerified) {
                  signInCallbacks.onSignInFailed(SignInFailReason.EMAIL_NOT_VERIFIED)
                  return@addOnSuccessListener
               }

               signInCallbacks.onSignInSuccess()
            }
            .addOnFailureListener {
               it.printStackTrace()
               signInCallbacks.onSignInFailed(
                     getReasonFromException(
                           it
                     )
               )
            }
   }

   /**
    * Call this in the Activity's onActivityResult()
    */
   fun handleSignInResult(requestCode: Int, resultCode: Int, data: Intent?) {
      facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
      if (requestCode == GOOGLE_SIGN_IN_CODE)
         try {
            handleGoogleSignInResult(
                  GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
            )
         } catch (e: ApiException) {
            Timber.e("Google sign in failed. Code=${e.statusCode}")
         }
   }

   private fun handleGoogleSignInResult(account: GoogleSignInAccount?) {
      if (signInCallbacks == null)
         throw RuntimeException("LoginCallbacks not set")

      signInCallbacks?.onPreSignIn()
      FirebaseAuth.getInstance()
            .signInWithCredential(GoogleAuthProvider.getCredential(account?.idToken, null))
            .addOnSuccessListener { signInCallbacks?.onSignInSuccess() }
            .addOnFailureListener { e ->
               signInCallbacks?.onSignInFailed(
                     getReasonFromException(
                           e
                     )
               )
               e.printStackTrace()
            }
   }

   private fun handleFacebookSignInResult(result: LoginResult?) {
      if (signInCallbacks == null)
         throw RuntimeException("LoginCallbacks not set")

      signInCallbacks?.onPreSignIn()
      result?.accessToken?.token?.let { FacebookAuthProvider.getCredential(it) }
            ?.let {
               FirebaseAuth.getInstance()
                     .signInWithCredential(it)
                     .addOnSuccessListener { signInCallbacks?.onSignInSuccess() }
                     .addOnFailureListener { e ->
                        signInCallbacks?.onSignInFailed(
                              getReasonFromException(e)
                        )
                        e.printStackTrace()
                     }
            }
   }

   fun logout() {
      FirebaseAuth.getInstance().signOut()
      LoginManager.getInstance().logOut()
      googleSignInClient.signOut()
   }
}