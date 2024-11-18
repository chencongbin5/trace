package com.akuchen.trace.service.util;

/**
 * @description:
 * @author: gaoweiwei_v
 * @time: 2019/6/20 4:28 PM
 */

import com.akuchen.trace.service.common.TraceConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RepositoryCache;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.FS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Slf4j
public class GitHandler {

    @Autowired
    private TraceConfig traceConfig;

    public Git syncRepository(String gitUrl, String codePath,String branch)  {
        try {
            File repoDir = new File(codePath);
            Git git;

            // Step 1: Check if the repository exists locally
            if (!repoDir.exists()) {
                // Step 2: Clone the repository
                git = Git.cloneRepository()
                        .setURI(gitUrl)
                        .setCredentialsProvider(new UsernamePasswordCredentialsProvider(traceConfig.getGitlabUsername(), traceConfig.getGitlabPassword()))
                        .setDirectory(repoDir)
                        .setBranch("refs/heads/"+branch)
                        .call();
            } else {

                git = Git.open(repoDir);
                git.reset().setMode(ResetCommand.ResetType.HARD).call();
                git.checkout().setName("refs/remotes/origin/"+branch).setForce(true).call();
                git.fetch().setCredentialsProvider(new UsernamePasswordCredentialsProvider(traceConfig.getGitlabUsername(), traceConfig.getGitlabPassword()))
                        .setCheckFetchedObjects(true).call();

                MergeResult result = git.merge().include(git.getRepository().exactRef("refs/remotes/origin/" + branch)).call();

                if (result.getMergeStatus().equals(MergeResult.MergeStatus.CONFLICTING)) {
                    git.checkout().setStage(CheckoutCommand.Stage.THEIRS).addPath(".").call();
                }

                git.clean().setCleanDirectories(true).call();

            }
            return git;
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }
        return null;
    }


    public Git cloneRepository(String gitUrl, String codePath, String commitId) throws GitAPIException {
        Git git = Git.cloneRepository()
                .setURI(gitUrl)
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(traceConfig.getGitlabUsername(), traceConfig.getGitlabPassword()))
                .setDirectory(new File(codePath))
                .setBranch(commitId)
                .call();
        // 切换到指定commitId
        checkoutBranch(git, commitId);
        return git;
    }
    public  CredentialsProvider findCredentialsProvider(){
        return new UsernamePasswordCredentialsProvider(traceConfig.getGitlabUsername(), traceConfig.getGitlabPassword());
    }

    private static Ref checkoutBranch(Git git, String branch) {
        try {
            return git.checkout()
                    .setName(branch)
                    .call();
        } catch (GitAPIException e) {
            throw new IllegalStateException(e);
        }
    }

    public static boolean isValidGitRepository(String codePath) {
        Path folder = Paths.get(codePath);
        if (Files.exists(folder) && Files.isDirectory(folder)) {
            // If it has been at least initialized
            if (RepositoryCache.FileKey.isGitRepository(folder.toFile(), FS.DETECTED)) {
                // we are assuming that the clone worked at that time, caller should call hasAtLeastOneReference
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }


}