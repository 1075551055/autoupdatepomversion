package core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class PushCommitToRemote {

    public static void main(String[] args) throws IOException, GitAPIException, URISyntaxException {
        Repository existingRepository = new FileRepositoryBuilder()
                .setGitDir(new File("C:/gitlab/iris4_dsh_demo/.git"))
                .build();
        String originGitUrl = "git@zha-ir4-ci1-w10:liwa/demo.git";
        String hkgGitUrl = "git@zha-ir4-ci1-w10:dsh/demo.git";
        String centralBranchForPush = "upstream/AutoUpdatePomRemote";
        pushChangesToRemote(existingRepository, originGitUrl, hkgGitUrl, centralBranchForPush);
        System.out.println(existingRepository.getBranch());
    }

    public static void pushChangesToRemote(Repository existingRepository, String gitUrlForOrigin, String gitUrlForCentral, String centralBranchForPush) throws GitAPIException, URISyntaxException, IOException {
        pushChangesToOrigin(existingRepository, gitUrlForOrigin);
        pushChangesToUpstream(existingRepository, gitUrlForCentral, centralBranchForPush);
    }

    private static void pushChangesToUpstream(Repository existingRepository, String gitUrlForCentral, String centralBranchForPush) throws GitAPIException, URISyntaxException, IOException {
        System.out.println("Starting push changes to HKG git server");
        try (Git git = new Git(existingRepository)) {
            // add remote repo:
            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setUri(new URIish(gitUrlForCentral));
            // you can add more settings here if needed
            remoteAddCommand.call();

            // push to remote:
            PushCommand pushCommand = git.push();
            //need to set remote, or it will push to default remote "origin"
            pushCommand.setRemote(centralBranchForPush.split("/")[0]);
            //RefSpec: is the mapping relationship between local branch and central branch
            pushCommand.setRefSpecs(new RefSpec(existingRepository.getBranch() + ":" + centralBranchForPush.split("/")[1]));
            //pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("username", "password"));
            // you can add more settings here if needed
            pushCommand.call();
        }
        System.out.println("End push changes to HKG git server...");
    }

    private static void pushChangesToOrigin(Repository existingRepository, String gitUrlForOrigin) throws GitAPIException, URISyntaxException, IOException {
        System.out.println("Starting push changes to origin...");
        try (Git git = new Git(existingRepository)) {
            // add remote repo:
            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setUri(new URIish(gitUrlForOrigin));
            remoteAddCommand.call();
            // push to remote:default is origin, can find in the source code (pushCommand.call())
            PushCommand pushCommand = git.push();
            //pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("username", "password"));
            // you can add more settings here if needed
            pushCommand.call();
        }
        System.out.println("End push changes to origin...");
    }
}
