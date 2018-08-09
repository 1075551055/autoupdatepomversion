package core;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import util.EmailUtil;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommitFileChanges {
    public static void commit(Repository existingRepository, List<String> pomFilesForCommit) throws GitAPIException, IOException {
        //todo:change to aspect for log
        System.out.println("Starting commit changes...");
        try (Git git = new Git(existingRepository)) {
            for (String pomFile : pomFilesForCommit) {
                //Stage all files in the existingRepository including new files, like GitExtensions "Stage" operation
                //when file pattern is ".", it means all file changes; pomFile is PathFilter parameter, current path
                //is project root folder, so no need to add the absolute path to file pattern, like only add "WLS_DOM_DSH/pom_deploy.xml" path
                git.add().addFilepattern(pomFile).call();
            }
            if (pomFilesForCommit.size() > 0) {
                // and then commit the changes.
                git.commit()
                        .setMessage("auto update pom version")
                        .call();
                constructEmailContentAndSend(existingRepository, pomFilesForCommit);
            }
        }
        System.out.println("\r\nEnd commit changes to local...");
    }

    private static void constructEmailContentAndSend(Repository existingRepository, List<String> pomFilesForCommit) throws IOException {
        String commitMsg = "Committed followings files' changes " + " to repository at " + existingRepository.getDirectory()
                + " for remote branch <font style='background-color:yellow'>" + existingRepository.getBranch() + "</font></br></br>";
        System.out.println(commitMsg);
        String emailContent = commitMsg;
        for (String pomFile : pomFilesForCommit) {
            System.out.println(pomFile);
            emailContent += pomFile + "</br>";
        }
        if (pomFilesForCommit.size() > 0) {
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            final String finalEmailContent = emailContent;
            executorService.execute(() -> {
                try {
                    EmailUtil.sendEmail(finalEmailContent);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            });
            executorService.shutdown();
        }
    }
}
