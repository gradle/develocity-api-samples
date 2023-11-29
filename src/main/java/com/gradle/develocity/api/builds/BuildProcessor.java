package com.gradle.develocity.api.builds;

import com.gradle.enterprise.api.model.Build;

interface BuildProcessor {

    void process(Build build);

}
