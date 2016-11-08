package com.yzq.android.musicplayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.ImageView;

/**
 * Created by YZQ on 2016/11/8.
 */

public class DrawCover {
    public DrawCover(){}

    public void setCover(Context context, ImageView cover, Uri uri) {
        MediaMetadataRetriever myRetriever = new MediaMetadataRetriever();
        myRetriever.setDataSource(context, uri);
        byte[] artwork;
        artwork = myRetriever.getEmbeddedPicture();
        if (artwork != null) {
            Bitmap bMap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
            cover.setImageBitmap(getRoundedShape(bMap));
        } else {
            cover.setImageResource(R.drawable.cover);
        }
    }

    private Bitmap getRoundedShape(Bitmap bMap) {
        int targetWidth = 1000;
        int targetHeight = 1000;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float)targetWidth-1)/2, ((float)targetHeight-1)/2, (Math.min(((float)targetWidth), ((float)targetHeight))/2), Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = bMap;
        canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight()), new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}
