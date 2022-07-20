package net.rekall.utils.imagehelper

import net.rekall.utils.imagehelper.engine.ImageEngine
import jp.wasabeef.glide.transformations.RoundedCornersTransformation


object EngineHelper {

    fun transitionGlideCornerType(cornerType: ImageEngine.CornerType?): RoundedCornersTransformation.CornerType {
        if (cornerType == null) {
            return RoundedCornersTransformation.CornerType.ALL
        }
        return when (cornerType) {
            ImageEngine.CornerType.ALL -> RoundedCornersTransformation.CornerType.ALL
            ImageEngine.CornerType.TOP -> RoundedCornersTransformation.CornerType.TOP
            ImageEngine.CornerType.TOP_LEFT -> RoundedCornersTransformation.CornerType.TOP_LEFT
            ImageEngine.CornerType.TOP_RIGHT -> RoundedCornersTransformation.CornerType.TOP_RIGHT
            ImageEngine.CornerType.BOTTOM -> RoundedCornersTransformation.CornerType.BOTTOM
            ImageEngine.CornerType.LEFT -> RoundedCornersTransformation.CornerType.LEFT
            ImageEngine.CornerType.RIGHT -> RoundedCornersTransformation.CornerType.RIGHT
            ImageEngine.CornerType.BOTTOM_LEFT -> RoundedCornersTransformation.CornerType.BOTTOM_LEFT
            ImageEngine.CornerType.BOTTOM_RIGHT -> RoundedCornersTransformation.CornerType.BOTTOM_RIGHT
            ImageEngine.CornerType.OTHER_TOP_LEFT -> RoundedCornersTransformation.CornerType.OTHER_TOP_LEFT
            ImageEngine.CornerType.OTHER_TOP_RIGHT -> RoundedCornersTransformation.CornerType.OTHER_TOP_RIGHT
            ImageEngine.CornerType.OTHER_BOTTOM_LEFT -> RoundedCornersTransformation.CornerType.OTHER_BOTTOM_LEFT
            ImageEngine.CornerType.OTHER_BOTTOM_RIGHT -> RoundedCornersTransformation.CornerType.OTHER_BOTTOM_RIGHT
            ImageEngine.CornerType.DIAGONAL_FROM_TOP_LEFT -> RoundedCornersTransformation.CornerType.DIAGONAL_FROM_TOP_LEFT
            ImageEngine.CornerType.DIAGONAL_FROM_TOP_RIGHT -> RoundedCornersTransformation.CornerType.DIAGONAL_FROM_TOP_RIGHT
        }
    }
}