package core;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.SshSessionFactory;

import java.io.File;
import java.io.IOException;

public class PullRemoteCommits {
    public static void main(String[] args) throws GitAPIException, IOException {
        try (Repository existingRepository = new FileRepositoryBuilder()
                .setGitDir(new File("C:/gitlab/demo/.git"))
                .build()) {
            pull(existingRepository, "AutoUpdatePomTest");
        }
    }

    public static void pull(Repository existingRepository, String branch) throws GitAPIException {
        System.out.println("Starting pull...");
        //need to set to "no", or it will meet com.jcraft.jsch.JSchException: UnknownHostKey
        SshSessionFactory.setInstance(new JschConfigSessionFactory() {
            public void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }
        });
        try (Git git = new Git(existingRepository)) {
            CheckoutBranch.checkout(existingRepository, branch);
            PullCommand pullCommand = git.pull();
            pullCommand.call();
            System.out.println("End pull...");
        }
    }

}
