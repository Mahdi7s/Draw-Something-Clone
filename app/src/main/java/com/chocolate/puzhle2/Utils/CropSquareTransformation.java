package com.chocolate.puzhle2.Utils;

/**
 * Copyright (C) 2015 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

public class CropSquareTransformation implements Transformation {
    private int mWidth;
    private int mHeight;

    public CropSquareTransformation(int w, int h){
        this.mWidth = w;
        this.mHeight = h;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, mWidth, mHeight);
        if (bitmap != source) {
            source.recycle();
        }

        return bitmap;
    }

    @Override
    public String key() {
        return "CropSquareTransformation(width=" + mWidth + ", height=" + mHeight + ")";
    }
}