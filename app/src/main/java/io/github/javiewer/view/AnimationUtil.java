package io.github.javiewer.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;

public class AnimationUtil {

    public static final int ANIMATION_DURATION_MEDIUM = 300;

    public interface AnimationListener {
        boolean onAnimationStart(View view);
        boolean onAnimationEnd(View view);
        boolean onAnimationCancel(View view);
    }

    public static void reveal(final View view, final AnimationListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int cx = view.getWidth() / 2;
            int cy = view.getHeight() / 2;
            float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

            Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
            view.setVisibility(View.VISIBLE);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    listener.onAnimationStart(view);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    listener.onAnimationEnd(view);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    listener.onAnimationCancel(view);
                }
            });
            anim.start();
        } else {
            view.setVisibility(View.VISIBLE);
            listener.onAnimationEnd(view);
        }
    }

    public static void fadeInView(final View view, long duration, final AnimationListener listener) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        listener.onAnimationStart(view);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        listener.onAnimationEnd(view);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        listener.onAnimationCancel(view);
                    }
                })
                .start();
    }

    public static void fadeOutView(final View view, long duration, final AnimationListener listener) {
        view.setAlpha(1f);
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        listener.onAnimationStart(view);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        view.setVisibility(View.GONE);
                        listener.onAnimationEnd(view);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        listener.onAnimationCancel(view);
                    }
                })
                .start();
    }
}
