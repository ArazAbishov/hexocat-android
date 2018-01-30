package com.abishov.hexocat.common.espresso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.annotation.IdRes;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.TextView;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

public final class DrawableMatcher {

  private DrawableMatcher() {
    // no instances
  }

  public static Matcher<View> withCompoundDrawable(@IdRes final int resourceId) {
    return new BoundedMatcher<View, TextView>(TextView.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("has compound drawable resource " + resourceId);
      }

      @Override
      public boolean matchesSafely(TextView textView) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
          if (sameBitmap(textView.getContext(), drawable, resourceId)) {
            return true;
          }
        }
        return false;
      }
    };
  }

  private static boolean sameBitmap(Context context, Drawable drawable, int resourceId) {
    return sameBitmap(drawable, context.getResources().getDrawable(resourceId));
  }

  private static boolean sameBitmap(Drawable first, Drawable second) {
    if (first == null || second == null) {
      return false;
    }
    if (first instanceof StateListDrawable && second instanceof StateListDrawable) {
      first = first.getCurrent();
      second = second.getCurrent();
    }
    if (first instanceof BitmapDrawable) {
      Bitmap bitmap = ((BitmapDrawable) first).getBitmap();
      Bitmap otherBitmap = ((BitmapDrawable) second).getBitmap();
      return bitmap.sameAs(otherBitmap);
    }
    return false;
  }
}
