package com.gradle.enterprise.api;

import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.Build;
import com.gradle.enterprise.api.model.BuildsQuery;

import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;

public final class BuildsProcessor {

    private final GradleEnterpriseApi api;
    private final BuildProcessor buildProcessor;

    public BuildsProcessor(GradleEnterpriseApi api, BuildProcessor buildProcessor) {
        this.api = api;
        this.buildProcessor = buildProcessor;
    }

    public void process(Instant since) throws ApiException {
        Consumer<BuildsQuery> sinceApplicator = q -> q.since(since.toEpochMilli());

        //noinspection InfiniteLoopStatement
        while (true) {
            var query = new BuildsQuery();
            sinceApplicator.accept(query);
            List<Build> builds = api.getBuilds(query);

            if (!builds.isEmpty()) {
                builds.forEach(buildProcessor::process);
                sinceApplicator = q -> q.sinceBuild(builds.get(builds.size() - 1).getId());
            }
        }
    }

}
