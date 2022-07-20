package onekey.rekallutils.utils.imagehelper.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import onekey.rekallutils.utils.imagehelper.drawable.URLDrawable
import java.io.File


interface ImageEngine {

    fun resumeLoadImage(context: Context?)


    fun pauseLoadImage(context: Context?)


    fun displayImageDrawable(
        textView: TextView?,
        drawable: URLDrawable?,
        url: String?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?,
        matchParentWidth: Boolean
    )


    fun displayImageDrawableRes(imageView: ImageView?, @DrawableRes imageResource: Int, placeHolder: Drawable?, errorPlaceHolder: Drawable?)


    fun displayImageFile(imageView: ImageView?, imageFile: File?, placeHolder: Drawable?, errorPlaceHolder: Drawable?)


    fun displayImageUrl(imageView: ImageView?, imageUrl: String?, placeHolder: Drawable? = null, errorPlaceHolder: Drawable? = null)


    fun displayImageUrl(imageView: ImageView?, uri: Uri?, placeHolder: Drawable?, errorPlaceHolder: Drawable?)


    fun displayImageUrlCircle(imageView: ImageView?, imageUrl: String?, placeHolder: Drawable? = null, errorPlaceHolder: Drawable? = null)



    fun displayImageBitmap(imageView: ImageView?, bitmap: Bitmap?, placeHolder: Drawable?, errorPlaceHolder: Drawable?)


    fun displayImageFileRound(imageView: ImageView?, file: File?, radius: Int, cornerType: CornerType?, placeHolder: Drawable?, errorPlaceHolder: Drawable?)


    fun displayImageBitmapBlur(
        imageView: ImageView?,
        imageUrl: Bitmap,
        radius: Int,
        placeHolder: Drawable? = null,
        errorPlaceHolder: Drawable? = null
    )


    fun displayImageUrlRound(
        imageView: ImageView?,
        imageUrl: String?,
        radius: Int,
        cornerType: CornerType? = CornerType.ALL,
        placeHolder: Drawable? = null,
        errorPlaceHolder: Drawable? = null
    )



    fun displayImageResRound(
        imageView: ImageView?,
        @DrawableRes imageResource: Int,
        radius: Int,
        cornerType: CornerType? = CornerType.ALL,
        placeHolder: Drawable? = null,
        errorPlaceHolder: Drawable? = null
    )


    fun displayImageUrlRoundFixed(
        imageView: ImageView?,
        imageUrl: String?,
        radius: Int,
        cornerType: CornerType?,
        placeHolder: Drawable?,
        errorPlaceHolder: Drawable?,
        width: Int,
        height: Int
    )


    fun displayImageUrlFixationSize(imageView: ImageView?, imageUrl: String?, placeHolder: Drawable?, errorPlaceHolder: Drawable?, width: Int, height: Int)


    fun downloadImage(context: Context?, imageUrl: String?, targetWidth: Int, targetHeight: Int): Bitmap?


    fun loadThumbnail(context: Context?, resize: Int, placeholder: Drawable?, imageView: ImageView?, uri: Uri?)

    /**
     * Load thumbnail of a gif image resource. You don't have to load an animated gif when it's only
     * a thumbnail tile.
     *
     * @param context     Context
     * @param resize      Desired size of the origin image
     * @param placeholder Placeholder drawable when image is not loaded yet
     * @param imageView   ImageView widget
     * @param uri         Uri of the loaded image
     */
    fun loadGifThumbnail(context: Context?, resize: Int, placeholder: Drawable?, imageView: ImageView?, uri: Uri?)

    /**
     * Load a static image resource.
     *
     * @param context   Context
     * @param resizeX   Desired x-size of the origin image
     * @param resizeY   Desired y-size of the origin image
     * @param imageView ImageView widget
     * @param uri       Uri of the loaded image
     */
    fun  loadImage(context: Context?, resizeX: Int, resizeY: Int, imageView: ImageView?, uri: Uri?)

    /**
     * Load a gif image resource.
     *
     * @param context   Context
     * @param resizeX   Desired x-size of the origin image
     * @param resizeY   Desired y-size of the origin image
     * @param imageView ImageView widget
     * @param uri       Uri of the loaded image
     */
    fun loadGifImage(context: Context?, resizeX: Int, resizeY: Int, imageView: ImageView?, uri: Uri?)


    enum class CornerType {
        ALL, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, TOP, BOTTOM, LEFT, RIGHT, OTHER_TOP_LEFT, OTHER_TOP_RIGHT, OTHER_BOTTOM_LEFT, OTHER_BOTTOM_RIGHT, DIAGONAL_FROM_TOP_LEFT, DIAGONAL_FROM_TOP_RIGHT
    }
}