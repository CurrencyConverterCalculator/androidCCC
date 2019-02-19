package mustafaozhan.github.com.mycurrencies.extensions

import android.annotation.SuppressLint
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import mustafaozhan.github.com.mycurrencies.R

/**
 * Created by Mustafa Ozhan on 2018-07-20.
 */
fun ImageView.setBackgroundByName(name: String) =
    setImageResource(
        resources.getIdentifier(
            name.toLowerCase(),
            "drawable",
            context.packageName
        )
    )

@SuppressLint("SetTextI18n")
fun TextView.addText(str: String) {
    text = text.toString() + str
}

fun AdView.loadAd(adId: Int) {
    MobileAds.initialize(context, resources.getString(adId))
    val adRequest = AdRequest.Builder().build()
    loadAd(adRequest)
}

fun WebView.fadeIO(isIn: Boolean) =
    startAnimation(AnimationUtils.loadAnimation(context, if (isIn) R.anim.fade_in else R.anim.fade_out))
