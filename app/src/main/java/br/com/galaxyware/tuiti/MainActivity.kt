package br.com.galaxyware.tuiti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.TwitterAuthProvider
import com.twitter.sdk.android.core.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.Result

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    lateinit var user: FirebaseUser;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = TwitterConfig.Builder(this)
            .twitterAuthConfig(
                TwitterAuthConfig(
                    getString(R.string.CONSUMER_KEY_TWITTER),
                    getString(R.string.CONSUMER_SECRET_TWITTER)
                )
            ).build()

        Twitter.initialize(config)
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance()
        animation();
        buttonTwitterOpen.setOnClickListener(this)



        buttonTwitterLogin?.callback = object : Callback<TwitterSession>() {
            override fun success(result: com.twitter.sdk.android.core.Result<TwitterSession>?) {
                Log.d(TAG, "twitterLogin:success$result")
                handleTwitterSession(result!!.data)            }

            override fun failure(exception: TwitterException) {
                Log.w(TAG, "twitterLogin:failure", exception)
                updateUI(null)
            }
        }
        buttonSearch.setOnClickListener {
            val intent = Intent(this@MainActivity, ListTweetsActivity::class.java)
            if (textEditUserName.text.toString() != "") {
                intent.putExtra("userName", textEditUserName.text.toString())
                startActivity(intent)
            } else {
                Toast.makeText(baseContext, "Type a user name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun animation() {
        val animationEmotion = AnimationUtils.loadAnimation(this, R.anim.move_emotion)
        val animationEmotion1 = AnimationUtils.loadAnimation(this, R.anim.move_emotion_sad)
        emotioHappy.startAnimation(animationEmotion)
        emotionSad.startAnimation(animationEmotion1)
    }


    public override fun onStart() {
        super.onStart()
        if(auth.currentUser != null) {
            user = auth.currentUser!!
            updateUI(user)
        }else{
            updateUI(null)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        buttonTwitterLogin!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleTwitterSession(session: TwitterSession) {
        Log.d(TAG, "handleTwitterSession:$session")

        val credential = TwitterAuthProvider.getCredential(
            session.authToken.token,
            session.authToken.secret
        )

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    user = auth.currentUser!!
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }
    }

    private fun open() {
        if (user != null) {
            val intent = Intent(this@MainActivity, ListTweetsActivity::class.java)
            intent.putExtra("userName", user.displayName)
            startActivity(intent)
        } else {
            Toast.makeText(
                baseContext, "Authentication failed.",
                Toast.LENGTH_SHORT
            ).show()
        }

//
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            buttonTwitterLogin.visibility = View.GONE
            buttonTwitterOpen.visibility = View.VISIBLE
            buttonTwitterSigOut.visibility = View.VISIBLE
        } else {
            buttonTwitterLogin.visibility = View.VISIBLE
            buttonTwitterOpen.visibility = View.GONE
            buttonTwitterSigOut.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.buttonTwitterOpen) {
            open()
        }else if (i == R.id.buttonTwitterSigOut){
            sigout();
        }
    }

    private fun sigout() {
        auth.signOut()
        TwitterCore.getInstance().sessionManager.clearActiveSession()
        updateUI(null)
    }

    companion object {
        private const val TAG = "TwitterLogin"
    }


}
