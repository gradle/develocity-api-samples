package com.gradle.develocity.api.builds;

import com.gradle.develocity.api.model.Build;

interface BuildProcessor {

    void process(Build build);

}
