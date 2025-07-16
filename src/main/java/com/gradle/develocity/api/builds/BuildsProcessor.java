package com.gradle.develocity.api.builds;

import com.gradle.enterprise.api.DevelocityApi;
import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.Build;
import com.gradle.enterprise.api.model.BuildsQuery;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

final class BuildsProcessor {

    private final DevelocityApi api;
    private final BuildProcessor buildProcessor;
    private final boolean reverse;
    private final int maxBuilds;
    private final int maxWaitSecs;

    BuildsProcessor(DevelocityApi api, BuildProcessor buildProcessor, boolean reverse, int maxBuilds, int maxWaitSecs) {
        this.api = api;
        this.buildProcessor = buildProcessor;
        this.reverse = reverse;
        this.maxBuilds = maxBuilds;
        this.maxWaitSecs = maxWaitSecs;
    }

    void process(Instant fromInstant) throws ApiException {
        Consumer<BuildsQuery> fromApplicator = buildsQuery -> buildsQuery.fromInstant(fromInstant.toEpochMilli());

        while (true) {
            BuildsQuery query = new BuildsQuery();
            query.setReverse(reverse);
            query.setMaxBuilds(maxBuilds);
            query.setMaxWaitSecs(maxWaitSecs);
            fromApplicator.accept(query);

            List<Build> builds = api.getBuilds(query);
            builds.forEach(buildProcessor::process);
            if (reverse) {
                break;
            } else if (!builds.isEmpty()) {
                fromApplicator = buildsQuery -> buildsQuery.fromBuild(builds.get(builds.size() - 1).getId());
            }
        }
    }

}
