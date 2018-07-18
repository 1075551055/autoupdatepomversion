import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PullRemoteCommits {
    public static void main(String[] args) throws GitAPIException, IOException {
        try (Repository existingRepository = new FileRepositoryBuilder()
                .setGitDir(new File("C:/gitlab/demo/.git"))
                .build()) {
            pull(existingRepository, "AutoUpdatePomTest");
        }
    }

    public static void pull(Repository existingRepository, String remoteBranch) throws IOException, GitAPIException {
        System.out.println("Starting pull...");
        //need to set to "no", or it will meet com.jcraft.jsch.JSchException: UnknownHostKey
        SshSessionFactory.setInstance(new JschConfigSessionFactory() {
            public void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
        });
        try (Git git = new Git(existingRepository)) {
            boolean createBranchInLocalOrNot = isCreateBranchInLocalOrNot(remoteBranch, git);
            //checkout or create(if is a new branch, need to create in local, then
            //will checkout it, need setCreateBranch(true)) remote branch in local first
            git.checkout().
                    setCreateBranch(createBranchInLocalOrNot).
                    setName(remoteBranch).
                    setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                    setStartPoint("origin/" + remoteBranch).
                    call();
            PullCommand pullCommand = git.pull();
            pullCommand.call();
            System.out.println("End pull...");
        }
    }

    private static boolean isCreateBranchInLocalOrNot(String remoteBranch, Git git) throws GitAPIException {
        boolean createBranchInLocalOrNot = true;
        List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref ref : refs) {
            if(ref.getName().contains(remoteBranch)){
                createBranchInLocalOrNot = false;
                break;
            }
        }
        return createBranchInLocalOrNot;
    }
}
