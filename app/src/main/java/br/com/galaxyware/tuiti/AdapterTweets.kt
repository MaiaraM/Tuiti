package br.com.galaxyware.tuiti

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.services.language.v1.CloudNaturalLanguage
import com.google.api.services.language.v1.CloudNaturalLanguageRequestInitializer
import com.google.api.services.language.v1.model.AnnotateTextRequest
import com.google.api.services.language.v1.model.AnnotateTextResponse
import com.google.api.services.language.v1.model.Document
import com.google.api.services.language.v1.model.Features
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.models.Tweet
import com.twitter.sdk.android.tweetui.Timeline
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter
import com.twitter.sdk.android.tweetui.TweetUtils
import io.fabric.sdk.android.services.concurrency.AsyncTask
import java.io.IOException
import kotlinx.android.synthetic.main.activity_list_tweets.*

class AdapterTweets(context: Context?, timeline: Timeline<Tweet>?) : TweetTimelineListAdapter(context, timeline) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var view = super.getView(position, convertView, parent)
        view.setBackgroundColor(Color.WHITE);

        var sentiment = 0.0F;
        if (view is ViewGroup) {
            disableViewAndSubViews(view);
        }

        view.setEnabled(true);
        view.setOnClickListener { it ->
            TweetUtils.loadTweet(getItemId(position), object : Callback<Tweet>() {
                override fun success(result: com.twitter.sdk.android.core.Result<Tweet>?) {

                    var build = CloudNaturalLanguage.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        AndroidJsonFactory(),
                        null
                    ).setCloudNaturalLanguageRequestInitializer(
                        CloudNaturalLanguageRequestInitializer(context.getString(br.com.galaxyware.tuiti.R.string.GOOGLE_KEY))
                    ).build()

                    val document = Document()
                    document.setType("PLAIN_TEXT");
                    document.language = "en-US"
                    document.content = result!!.data.text

                    val features = Features()
                    features.extractEntities = true
                    features.extractDocumentSentiment = true

                    val request = AnnotateTextRequest()
                    request.document = document
                    request.features = features

                    object : AsyncTask<Any, Void, AnnotateTextResponse>() {
                        override fun doInBackground(vararg params: Any): AnnotateTextResponse? {
                            var response: AnnotateTextResponse? = null
                            try {
                                response = build.documents().annotateText(request).execute()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }

                            return response
                        }

                        override fun onPostExecute(response: AnnotateTextResponse?) {
                            super.onPostExecute(response)
                            if (response != null) {
                                val sent = response.documentSentiment.score
                                if (sent > 0.0f) {
                                    view.setBackgroundColor(Color.YELLOW);
                                } else if (sent < 0.0f) {
                                    view.setBackgroundColor(Color.GRAY);
                                } else if (sent == 0.0F) {
                                    view.setBackgroundColor(0xFF99A9FF.toInt());
                                } else {
                                    view.setBackgroundColor(Color.WHITE);
                                }
                            }else{
                                view.setBackgroundColor(Color.WHITE);
                            }
                        }
                    }.execute()

                }

                override fun failure(exception: TwitterException) {
                    Toast.makeText(context, "Ops! Something went wrong", Toast.LENGTH_SHORT).show();
                }
            })
        };

        return view;
    }

    private fun disableViewAndSubViews(layout: ViewGroup) {
        layout.isEnabled = false
        for (i in 0 until layout.childCount) {
            val child = layout.getChildAt(i)
            if (child is ViewGroup) {
                disableViewAndSubViews(child)
            } else {
                child.isEnabled = false
                child.isClickable = false
                child.isLongClickable = false
            }
        }
    }


}