import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class PushCommitToRemote {

    public static void pushChangesToRemote(Repository existingRepository, String originGitUrl, String hkgGitUrl, String remoteRepository) throws GitAPIException, URISyntaxException, IOException {
        pushChangesToOrigin(existingRepository, originGitUrl);
        pushChangesToUpstream(existingRepository, hkgGitUrl, remoteRepository);
    }

    private static void pushChangesToUpstream(Repository existingRepository, String hkgGitUrl, String hkgRemoteRepository) throws GitAPIException, URISyntaxException {
        System.out.println("Starting push changes to HKG git server");
        try (Git git = new Git(existingRepository)) {
            // add remote repo:
            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setUri(new URIish(hkgGitUrl));
            // you can add more settings here if needed
            remoteAddCommand.call();

            // push to remote:
            PushCommand pushCommand = git.push();
            //need to set remote, or it will push to default remote "origin"
            pushCommand.setRemote(hkgRemoteRepository);
            //pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("username", "password"));
            // you can add more settings here if needed
            pushCommand.call();
        }
        System.out.println("End push changes to HKG git server...");
    }

    private static void pushChangesToOrigin(Repository existingRepository, String originGitUrl) throws GitAPIException, URISyntaxException, IOException {
        System.out.println("Starting push changes to origin...");
        try (Git git = new Git(existingRepository)) {
            // add remote repo:
            RemoteAddCommand remoteAddCommand = git.remoteAdd();
            remoteAddCommand.setUri(new URIish(originGitUrl));
            remoteAddCommand.call();
            // push to remote:default is origin
            PushCommand pushCommand = git.push();
            //pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider("username", "password"));
            // you can add more settings here if needed
            pushCommand.call();
        }
        System.out.println("End push changes to origin...");
    }
}
