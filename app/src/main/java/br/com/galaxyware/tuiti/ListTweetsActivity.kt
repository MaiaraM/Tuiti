package br.com.galaxyware.tuiti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.twitter.sdk.android.tweetui.UserTimeline
import kotlinx.android.synthetic.main.activity_list_tweets.*


class ListTweetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_tweets);

        if(intent.getStringExtra("userName") != null) {
            find(intent.getStringExtra("userName"));
        }
    }

    private fun find(userName: String) {
        try {
            val userTimeline = UserTimeline.Builder()
                .screenName(userName).build()
            val adapter: AdapterTweets = AdapterTweets(this, userTimeline);

            listTweets.adapter = adapter;
            listTweets.emptyView = linearLayout

        } catch (e: Exception) {
            Log.d("ERROTWITER", e.message)
            Toast.makeText(
                baseContext, "OPS! ",
                Toast.LENGTH_SHORT
            ).show()
        }


    }
}
