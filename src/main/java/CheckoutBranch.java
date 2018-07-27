import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;

import java.util.List;

public class CheckoutBranch {
    public static void checkout(Repository existingRepository, String branch) throws GitAPIException {
        //need to set to "no", or it will meet com.jcraft.jsch.JSchException: UnknownHostKey
        SshSessionFactory.setInstance(new JschConfigSessionFactory() {
            public void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
        });
        try (Git git = new Git(existingRepository)) {
            boolean createBranchInLocalOrNot = isCreateBranchInLocalOrNot(branch, git);
            //checkout or create(if is a new branch, need to create in local, then
            //will checkout it, need setCreateBranch(true)) remote branch in local first
            git.checkout().
                    setCreateBranch(createBranchInLocalOrNot).
                    setName(branch).
                    setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).
                    setStartPoint("origin/" + branch).
                    call();
        }
    }

    private static boolean isCreateBranchInLocalOrNot(String branch, Git git) throws GitAPIException {
        boolean createBranchInLocalOrNot = true;
        List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (Ref ref : refs) {
            if(ref.getName().contains(branch)){
                createBranchInLocalOrNot = false;
                break;
            }
        }
        return createBranchInLocalOrNot;
    }
}
