package com.surfacesoft.yaj.sascancharger;

import android.os.Bundle;
import androidx.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro;

/**
 * Created by yulio on 26/09/2015.
 */
public class Intro extends AppIntro {

    // Please DO NOT override onCreate. Use init
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Add your slide's fragments here
        // AppIntro will automatically generate the dots indicator and buttons.
        addSlide(FragmentSlides.newInstance(R.layout.fragment_first_slide));
        addSlide(FragmentSlides.newInstance(R.layout.fragment_second_slide));
        addSlide(FragmentSlides.newInstance(R.layout.fragment_third_slide));
        addSlide(FragmentSlides.newInstance(R.layout.fragment_fourth_slide));

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest
//        addSlide(AppIntroFragment.newInstance(title, description, image, background_colour));

        // OPTIONAL METHODS
        // Override bar/separator color
//        setBarColor(Color.parseColor("#3F51B5"));
//        setSeparatorColor(Color.parseColor("#2196F3"));

        // Hide Skip/Done button
//        showSkipButton(true);
        showDoneButton(true);

        // Turn vibration on and set intensity
        // NOTE: you will probably need to ask VIBRATE permesssion in Manifest
        setVibrate(false);
//        setVibrateIntensity(30);

        //  set animation of slides
        setFlowAnimation();
    }

//    @Override
//    public void onSkipPressed() {
//        // Do something when users tap on Skip button.
//
//        this.finish();
//    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        this.finish();
    }

    @Override
    public void onSkipPressed() {
        this.finish();
    }
}