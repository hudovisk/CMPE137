package com.assignment.sjsu.hudoassenco.cmpe137;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;

public class Utils {

    public static class ValidationResult {
        public boolean mValid;
        public int mMessageRes;

        public ValidationResult(boolean valid, int messageRes) {
            mValid = valid;
            mMessageRes = messageRes;
        }
    }

    public static ValidationResult isPasswordValid(String password) {
        //TODO: Proper password validation logic.
        if(password.isEmpty()) {
            return new ValidationResult(false, R.string.error_invalid_password);
        } else {
            return new ValidationResult(true, 0);
        }
    }

    public static ValidationResult isEmailValid(String email) {
        //TODO: Proper email validation logic.
        if(email.isEmpty()) {
            return new ValidationResult(false, R.string.error_invalid_email);
        } else {
            return new ValidationResult(true, 0);
        }
    }

    public static ValidationResult isNameValid(String name) {
        //TODO: Proper mName validation logic.
        if(name.isEmpty()) {
            return new ValidationResult(false, R.string.error_invalid_password);
        } else {
            return new ValidationResult(true, 0);
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static int getGalleryImageRotation(Context context, Uri photoUri) {
    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    public static Bitmap rotateBitmap(final Bitmap bitmap, float degree) {
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, true);
    }

}
