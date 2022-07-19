package net.rekall.utils.imagehelper.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import net.rekall.utils.imagehelper.EngineHelper
import net.rekall.utils.imagehelper.drawable.URLDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import jp.wasabeef.glide.transformations.BlurTransformation
import jp.wasabeef.glide.transformations.RoundedCornersTransformation
import java.io.File
import java.util.concurrent.ExecutionException


/**
 * <pre>
 * author :
 * time   : 2019/03/22
 * desc   : Glide
 * </pre>
 */
class GlideImageEngine : ImageEngine {
    override fun resumeLoadImage(context: Context?) {
        if (context != null) {
            Glide.with(context).resumeRequests()
        }
    }

    override fun pauseLoadImage(context: Context?) {
        if (context != null) {
            Glide.with(context).pauseRequests()
        }
    }

    /**
     *
     *
     * @param url
     * @param placeHolder
     * @param errorPlaceHolder
     * @param drawable
     * @param matchParentWidth
     * @param textView
     */
    override fun displayImageDrawable(
        textView: TextView?,
        drawable: URLDrawable?,
        url: String?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?,
        matchParentWidth: Boolean
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        Glide.with(textView!!)
            .load(url)
            .apply(options)
            .into(object : CustomTarget<Drawable?>() {
                override fun onLoadCleared(placeholder: Drawable?) {}
                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable?>?
                ) {
                    val width: Int
                    val height: Int
                    if (matchParentWidth) {
                        val viewWidth = textView.width
                        val scale = viewWidth.toFloat() / resource.intrinsicWidth
                        width = (scale * resource.intrinsicWidth).toInt()
                        height = (scale * resource.intrinsicHeight).toInt()
                    } else {
                        width = resource.intrinsicWidth
                        height = resource.intrinsicHeight
                    }
                    resource.setBounds(0, 0, width, height)
                    drawable!!.setBounds(0, 0, width, height)
                    drawable.setDrawable(resource)
                    textView.text = textView.text
                }
            })
    }

    override fun displayImageDrawableRes(
        imageView: ImageView?,
        imageResource: Int,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        displayImageResource(
            imageView,
            imageResource,
            getRequestOptions(placeHolder, errorPlaceHolder)
        )
    }

    override fun displayImageFile(
        imageView: ImageView?,
        imageFile: File?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        displayImageResource(imageView, imageFile, getRequestOptions(placeHolder, errorPlaceHolder))
    }

    override fun displayImageUrl(
        imageView: ImageView?,
        imageUrl: String?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        displayImageResource(imageView, imageUrl, getRequestOptions(placeHolder, errorPlaceHolder))
    }

    override fun displayImageUrl(
        imageView: ImageView?,
        uri: Uri?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        displayImageResource(imageView, uri, options)
    }

    override fun displayImageUrlCircle(
        imageView: ImageView?,
        imageUrl: String?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        options.circleCrop()
        displayImageResource(imageView, imageUrl, options)
    }

    override fun displayImageBitmap(
        imageView: ImageView?,
        bitmap: Bitmap?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        displayImageResource(imageView, bitmap, options)
    }

    override fun displayImageFileRound(
        imageView: ImageView?,
        file: File?,
        radius: Int,
        cornerType: ImageEngine.CornerType?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        options.transform(
            CenterCrop(),
            RoundedCornersTransformation(
                radius,
                0,
                EngineHelper.transitionGlideCornerType(cornerType)
            )
        )
        displayImageResource(imageView, file, options)
    }

    override fun displayImageBitmapBlur(imageView: ImageView?, imageUrl: Bitmap, radius: Int, placeHolder: Drawable?, errorPlaceHolder: Drawable?) {
        Glide.with(imageView!!).load(imageUrl).transform(BlurTransformation(radius)).into(imageView)
    }

    override fun displayImageUrlRound(
        imageView: ImageView?,
        imageUrl: String?,
        radius: Int,
        cornerType: ImageEngine.CornerType?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        options.transform(
            CenterCrop(),
            RoundedCornersTransformation(
                radius,
                0,
                EngineHelper.transitionGlideCornerType(cornerType)
            )
        )
        displayImageResource(imageView, imageUrl, options)
    }

    override fun displayImageResRound(
        imageView: ImageView?,
        imageResource: Int,
        radius: Int,
        cornerType: ImageEngine.CornerType?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        options.transform(
            CenterCrop(),
            RoundedCornersTransformation(
                radius,
                0,
                EngineHelper.transitionGlideCornerType(cornerType)
            )
        )
        displayImageResource(imageView, imageResource, options)
    }

    override fun displayImageUrlRoundFixed(
        imageView: ImageView?,
        imageUrl: String?,
        radius: Int,
        cornerType: ImageEngine.CornerType?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?,
        width: Int,
        height: Int
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        options.transform(
            CenterCrop(),
            RoundedCornersTransformation(
                radius,
                0,
                EngineHelper.transitionGlideCornerType(cornerType)
            )
        )
        displayImageResource(imageView, imageUrl, options, width, height)
    }

    override fun displayImageUrlFixationSize(
        imageView: ImageView?,
        imageUrl: String?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?,
        width: Int,
        height: Int
    ) {
        val options = getRequestOptions(placeHolder, errorPlaceHolder)
        displayImageResource(imageView, imageUrl, options, width, height)
    }

    override fun downloadImage(
        context: Context?,
        imageUrl: String?,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        try {
            val builder = Glide
                .with(context!!)
                .asBitmap()
                .load(imageUrl)
            val futureTarget: FutureTarget<Bitmap>
            futureTarget = if (targetHeight > 0 && targetWidth > 0) {
                builder.submit(targetWidth, targetHeight)
            } else {
                builder.submit()
            }
            return futureTarget.get()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return null
    }



    override fun loadThumbnail(
        context: Context?,
        resize: Int,
        placeholder: Drawable?,
        imageView: ImageView?,
        uri: Uri?
    ) {
        displayImageResource(
            imageView, uri, RequestOptions()
                .override(resize, resize)
                .priority(Priority.HIGH)
                .placeholder(placeholder)
                .fitCenter()
        )
    }


    override fun loadGifThumbnail(
        context: Context?, resize: Int, placeholder: Drawable?, imageView: ImageView?,
        uri: Uri?
    ) {
        displayGifResource(
            imageView, uri, RequestOptions()
                .override(resize, resize)
                .placeholder(placeholder)
                .priority(Priority.HIGH)
                .fitCenter()
        )
    }


    override fun loadImage(
        context: Context?,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView?,
        uri: Uri?
    ) {
        displayImageResource(
            imageView, uri, RequestOptions()
                .override(resizeX, resizeY)
                .priority(Priority.HIGH)
                .fitCenter()
        )
    }

    @SuppressLint("CheckResult")
    override fun loadGifImage(
        context: Context?,
        resizeX: Int,
        resizeY: Int,
        imageView: ImageView?,
        uri: Uri?
    ) {
        val options = RequestOptions()
            .priority(Priority.HIGH)
            .fitCenter()
        if (resizeX != -1 && resizeY != -1) {
            options.override(resizeX, resizeY)
        }
        displayGifResource(imageView, uri, options)
    }



    private fun displayImageResource(
        imageView: ImageView?,
        resource: Any?,
        requestOptions: RequestOptions,
        width: Int,
        height: Int
    ) {
        Glide.with(imageView!!)
            .load(resource)
            .apply(requestOptions)
            .override(width, height)
            .into(imageView)
    }


    private fun displayImageResource(
        imageView: ImageView?,
        resource: Any?,
        requestOptions: RequestOptions
    ) {
        Glide.with(imageView!!)
            .asBitmap()
            .load(resource)
            .apply(requestOptions)
            .into(imageView)
    }


    private fun displayGifResource(
        imageView: ImageView?,
        resource: Any?,
        requestOptions: RequestOptions
    ) {
        Glide.with(imageView!!)
            .asGif()
            .load(resource)
            .apply(requestOptions)
            .into(imageView)
    }


    private fun getRequestOptions(
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?
    ): RequestOptions {
        val options = RequestOptions()
        options.placeholder(placeHolder)
            .error(errorPlaceHolder)
            .sizeMultiplier(1f)
        return options
    }
}