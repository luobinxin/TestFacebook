package cn.com.startai.baseloginlibs.util;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * Created by Robin on 2019/2/28.
 * qq: 419109715 彬影
 */

public class ImageUtils {

    public static void loadImage(Context context, ImageView imageView, String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return;
        }
        Glide.with(context).load(imageUrl).into(imageView);

    }

}
