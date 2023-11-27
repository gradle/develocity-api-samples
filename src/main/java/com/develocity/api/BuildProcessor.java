package com.develocity.api;

import com.gradle.enterprise.api.model.Build;

public interface BuildProcessor {

    void process(Build build);

}
