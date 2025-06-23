package com.hoko.blur.plugin;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Deps implements Plugin<Project> {
    @Override
    public void apply(Project target) {

    }

    public static final int minSdkVersion = 21;
    public static final int compileSdkVersion = 36;
    public static final int targetSdkVersion = 36;
    public static final String buildToolsVersion = "36.0.0";
    public static final JavaVersion javaVersion = JavaVersion.VERSION_17;
    public static final String ndkVersion = "28.1.13356709";
    public static final String hokoBlurReleaseVersion = "1.5.4";

    public static final String appcompat = "androidx.appcompat:appcompat:1.4.0";
    public static final String junit = "junit:junit:4.13.2";
    public static final String androidTestJunit = "androidx.test.ext:junit:1.1.5";
    public static final String espressoCore = "androidx.test.espresso:espresso-core:3.5.1";
    public static final String androidAnnotation = "androidx.annotation:annotation:1.7.0";

}