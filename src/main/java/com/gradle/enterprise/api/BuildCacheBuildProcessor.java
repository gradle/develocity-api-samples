package com.gradle.enterprise.api;

import com.gradle.enterprise.api.client.ApiException;
import com.gradle.enterprise.api.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.time.Instant;
import java.util.Set;

public final class BuildCacheBuildProcessor implements BuildProcessor {

    private static final Set<GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcomeEnum> GRADLE_CACHE_HIT_TYPES = Set.of(
        GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcomeEnum.AVOIDED_FROM_LOCAL_CACHE,
        GradleBuildCachePerformanceTaskExecutionEntry.AvoidanceOutcomeEnum.AVOIDED_FROM_REMOTE_CACHE
    );

    private static final Set<MavenBuildCachePerformanceGoalExecutionEntry.AvoidanceOutcomeEnum> MAVEN_CACHE_HIT_TYPES = Set.of(
        MavenBuildCachePerformanceGoalExecutionEntry.AvoidanceOutcomeEnum.AVOIDED_FROM_LOCAL_CACHE,
        MavenBuildCachePerformanceGoalExecutionEntry.AvoidanceOutcomeEnum.AVOIDED_FROM_REMOTE_CACHE
    );

    private final GradleEnterpriseApi api;
    private final String serverUrl;
    private final String projectName;

    BuildCacheBuildProcessor(GradleEnterpriseApi api, String serverUrl, String projectName) {
        this.api = api;
        this.serverUrl = serverUrl;
        this.projectName = projectName;
    }

    @Override
    public void process(Build build) {
        try {
            if (build.getBuildToolType().equals("gradle")) {
                processGradleBuild(build);
            } else if (build.getBuildToolType().equals("maven")) {
                processMavenBuild(build);
            }
        } catch (ApiException e) {
            reportError(build, e);
        }
    }

    private void processMavenBuild(Build build) throws ApiException {
        var attributes = api.getMavenAttributes(build.getId(), new BuildQuery());
        if (projectName == null || projectName.equals(attributes.getTopLevelProjectName())) {
            var model = api.getMavenBuildCachePerformance(build.getId(), new BuildQuery());
            reportBuild(
                build,
                computeCacheHitPercentage(model),
                computeAvoidanceSavingsRatioPercentage(model),
                attributes.getTopLevelProjectName(),
                attributes.getBuildDuration(),
                attributes.getEnvironment().getUsername()
            );
        }
    }

    private void processGradleBuild(Build build) throws ApiException {
        var attributes = api.getGradleAttributes(build.getId(), new BuildQuery());
        if (projectName == null || projectName.equals(attributes.getRootProjectName())) {
            var model = api.getGradleBuildCachePerformance(build.getId(), new BuildQuery());
            reportBuild(
                build,
                computeCacheHitPercentage(model),
                computeAvoidanceSavingsRatioPercentage(model),
                attributes.getRootProjectName(),
                attributes.getBuildDuration(),
                attributes.getEnvironment().getUsername()
            );
        }
    }

    private void reportBuild(Build build, BigDecimal cacheHitPercentage, BigDecimal avoidanceSavingsRatioPercentage, String rootProjectName, Long buildDuration, String username) {
        System.out.printf("Build Scan | %s | Project: %s | 🗓  %s | ⏱  %s ms\t| 👤 %s%n - \tCache hit percentage: %s%%%n - \tAvoidance savings ratio: %s%%%n%n",
            buildScanUrl(build),
            rootProjectName,
            Instant.ofEpochMilli(build.getAvailableAt()).toString(),
            buildDuration,
            username,
            cacheHitPercentage,
            avoidanceSavingsRatioPercentage
        );
    }

    private void reportError(Build build, ApiException e) {
        System.err.printf("API Error %s for Build Scan ID %s%n%s%n", e.getCode(), build.getId(), e.getResponseBody());
        ApiProblemParser.maybeParse(e).ifPresent(apiProblem -> {
            // Types of API problems can be checked as following
            if (apiProblem.getType().equals("urn:gradle:enterprise:api:problems:build-deleted")) {
                // Handle the case when the Build Scan is deleted.
                System.err.println(apiProblem.getDetail());
            }
        });
    }

    private URI buildScanUrl(Build build) {
        return URI.create(serverUrl + "/s/" + build.getId());
    }

    private static BigDecimal computeAvoidanceSavingsRatioPercentage(GradleBuildCachePerformance gradleBuildCachePerformanceModel) {
        return toPercentage(gradleBuildCachePerformanceModel.getAvoidanceSavingsSummary().getRatio());
    }

    private static BigDecimal computeAvoidanceSavingsRatioPercentage(MavenBuildCachePerformance mavenBuildCachePerformanceModel) {
        return toPercentage(mavenBuildCachePerformanceModel.getAvoidanceSavingsSummary().getRatio());
    }

    private static BigDecimal computeCacheHitPercentage(GradleBuildCachePerformance model) {
        int numTasks = model.getTaskExecution().size();
        long numAvoidedTasks = model.getTaskExecution().stream()
            .filter(task -> GRADLE_CACHE_HIT_TYPES.contains(task.getAvoidanceOutcome()))
            .count();

        return toPercentage(numTasks, numAvoidedTasks);
    }

    private static BigDecimal computeCacheHitPercentage(MavenBuildCachePerformance model) {
        int numGoals = model.getGoalExecution().size();
        long numAvoidedGoals = model.getGoalExecution().stream()
            .filter(goal -> MAVEN_CACHE_HIT_TYPES.contains(goal.getAvoidanceOutcome()))
            .count();

        return toPercentage(numGoals, numAvoidedGoals);
    }

    private static BigDecimal toPercentage(Double ratio) {
        return toPercentage(BigDecimal.valueOf(ratio));
    }

    private static BigDecimal toPercentage(BigDecimal ratio) {
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal toPercentage(long total, long portion) {
        if (total == 0) {
            return BigDecimal.ZERO;
        } else {
            return toPercentage(new BigDecimal(portion).divide(new BigDecimal(total), 2, RoundingMode.HALF_UP));
        }
    }

}
