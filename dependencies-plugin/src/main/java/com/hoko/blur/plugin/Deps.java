package com.hoko.blur.plugin;

import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Deps implements Plugin<Project> {
    @Override
    public void apply(Project target) {

    }

    public static final int minSdkVersion = 21;
    public static final int compileSdkVersion = 34;
    public static final int targetSdkVersion = 34;
    public static final String buildToolsVersion = "34.0.0";
    public static final int renderscriptTargetApi = 34;
    public static final boolean renderscriptEnabled = true;
    public static final boolean renderscriptSupportModeEnabled = true;
    public static final JavaVersion javaVersion = JavaVersion.VERSION_17;
    public static final String ndkVersion = "26.1.10909125";

    public static final String appcompat = "androidx.appcompat:appcompat:1.4.0";
    public static final String junit = "junit:junit:4.13.2";
    public static final String androidTestJunit = "androidx.test.ext:junit:1.1.5";
    public static final String espressoCore = "androidx.test.espresso:espresso-core:3.5.1";
    public static final String androidAnnotation = "androidx.annotation:annotation:1.7.0";

    public static final String androidGradlePlugin = "com.android.tools.build:gradle:8.1.2";

}