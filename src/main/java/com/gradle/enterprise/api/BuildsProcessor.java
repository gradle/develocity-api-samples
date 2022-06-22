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
    private final int maxBuilds;
    private final int maxWaitSecs;

    public BuildsProcessor(
        final GradleEnterpriseApi api,
        final BuildProcessor buildProcessor,
        final int maxBuilds,
        final int maxWaitSecs
    ) {
        this.api = api;
        this.buildProcessor = buildProcessor;
        this.maxBuilds = maxBuilds;
        this.maxWaitSecs = maxWaitSecs;
    }

    public void process(final Instant since) throws ApiException {
        Consumer<BuildsQuery> sinceApplicator = buildsQuery -> buildsQuery.since(since.toEpochMilli());

        //noinspection InfiniteLoopStatement
        while (true) {
            final BuildsQuery query = new BuildsQuery();
            query.setMaxBuilds(maxBuilds);
            query.setMaxWaitSecs(maxWaitSecs);
            sinceApplicator.accept(query);

            final List<Build> builds = api.getBuilds(query);

            if (!builds.isEmpty()) {
                builds.forEach(buildProcessor::process);

                sinceApplicator = buildsQuery -> buildsQuery.sinceBuild(builds.get(builds.size() - 1).getId());
            }
        }
    }

}
