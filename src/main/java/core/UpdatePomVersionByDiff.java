package core;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import pojo.GitParameter;
import util.UpdatePomVersionUtil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public class UpdatePomVersionByDiff {
    private static final List<String> PRS_POM_FOR_UPDATE = Arrays.asList(
            "WLS_PRS_DSH/pom_deploy.xml",
            "WLS_PRS_DSH/war_file/pom.xml",
            "WLS_PRS_DSH/ear_file/pom.xml");
    private static final List<String> DOM_POM_FOR_UPDATE = Arrays.asList(
            "WLS_DOM_DSH/pom_deploy.xml",
            "WLS_DOM_DSH/pom_deploy_BE.xml",
            "WLS_DOM_DSH/pom_deploy_CA.xml",
            "WLS_DOM_DSH/war_file/pom.xml",
            "WLS_DOM_DSH/ear_file/pom.xml",
            "WLS_DOM_DSHBE/pom.xml",
            "WLS_DOM_DSHBE/ear_file/pom.xml",
            "WLS_DOM_DSHCA/pom.xml",
            "WLS_DOM_DSHCA/ear_file/pom.xml");
    private static final String DSH_COMMON_POM_FOR_UPDATE = "DSH_Common/pom.xml";
    private static final String DSH_JAXB_POM_FOR_UPDATE = "DSH_JAXB/pom.xml";
    private static final String PROJECT_ROOT_POM = "pom.xml";

    public static void main(String[] args) throws IOException, GitAPIException, URISyntaxException {
        Date since = new Date();
        Calendar calendarForSince = Calendar.getInstance();
        calendarForSince.setTime(since);
//        calendarForSince.set(Calendar.DATE, calendarForSince.get(Calendar.DATE) - 1);
        calendarForSince.set(Calendar.HOUR_OF_DAY, 15);
        calendarForSince.set(Calendar.MINUTE, 17);
        since = calendarForSince.getTime();

        Date until = new Date();
        Calendar calendarForUtil = Calendar.getInstance();
        calendarForUtil.setTime(until);
        calendarForUtil.set(Calendar.HOUR, 15);
        calendarForUtil.set(Calendar.MINUTE, 40);
        until = calendarForUtil.getTime();

        String hkgGitUrl = "git@zha-ir4-ci1-w10:dsh/demo.git";
        String originGitUrl = "git@zha-ir4-ci1-w10:liwa/demo.git";
        String hkgRemoteRepository = "upstream";
        update(new GitParameter("C:/gitlab/iris4_dsh_deploy", "IRIS4_R24.1_POST_Aug05", hkgRemoteRepository, originGitUrl, hkgGitUrl, since, until));
    }

    public static void update(GitParameter gitParameter) throws IOException, GitAPIException, URISyntaxException {
        gitParameter.setProjectRootPath(gitParameter.getProjectRootPath().replace("\\\\", "/"));
        if (!gitParameter.getProjectRootPath().endsWith("/")) {
            gitParameter.setProjectRootPath(gitParameter.getProjectRootPath() + "/");
        }
        try (Repository existingRepository = new FileRepositoryBuilder()
                .setGitDir(new File(gitParameter.getProjectRootPath() + ".git"))
                .build()) {
            //it will checkout remoteBranch first and then pull changes
            PullRemoteCommits.pull(existingRepository, gitParameter.getRemoteBranch());
            List<String> pomFilesForPush = updatePomVersion(existingRepository, gitParameter.getProjectRootPath(), gitParameter.getSince(), gitParameter.getUntil());
            CommitFileChanges.commit(existingRepository, pomFilesForPush);
            PushCommitToRemote.pushChangesToRemote(existingRepository, gitParameter.getOriginGitUrl(), gitParameter.getHkgGitUrl(), gitParameter.getHkgRemoteRepository());
        }
    }

    private static List<String> updatePomVersion(Repository existingRepository, String projectRootPath, Date since, Date until) throws IOException {
        System.out.println("Starting update pom version...");
        List<String> pomFilesForPush = new ArrayList<>();
        List<DiffEntry> commitChanges = getCommitChanges(existingRepository, since, until);
        boolean needUpdatePrsPom = false;
        boolean needUpdateDomPom = false;
        boolean needUpdateDSHCommonPom = false;
        boolean needUpdateDSHJaxbPom = false;
        for (DiffEntry diff : commitChanges) {
            //todo:filter update pom commit
            if (diff.getNewPath().contains("WLS_PRS_DSH/war_file") && !diff.getNewPath().contains("WLS_PRS_DSH/war_file/src/test")
                    && !matchPomFile(diff)) {
                needUpdatePrsPom = true;
                System.out.println(diff.getNewPath());
            }
            if (diff.getNewPath().contains("WLS_DOM_DSH/war_file") && !diff.getNewPath().contains("WLS_DOM_DSH/war_file/src/test")
                    && !diff.getNewPath().contains("WLS_DOM_DSH/war_file/src/it") && !diff.getNewPath().contains("WLS_DOM_DSH/war_file/src/support")
                    && !matchPomFile(diff)) {
                needUpdateDomPom = true;
                System.out.println(diff.getNewPath());
            }
            if (diff.getNewPath().contains("DSH_Common") && !matchPomFile(diff) && !diff.getNewPath().contains("DSH_Common/src/test")) {
                needUpdateDSHCommonPom = true;
                System.out.println(diff.getNewPath());
            }
            if (diff.getNewPath().contains("DSH_JAXB") && !matchPomFile(diff)) {
                needUpdateDSHJaxbPom = true;
                System.out.println(diff.getNewPath());
            }
        }
        if (needUpdatePrsPom) {
            System.out.println("Update PRS pom version...");
            UpdatePomVersionUtil.updateNormalPomVersion(projectRootPath, PRS_POM_FOR_UPDATE);
            pomFilesForPush.addAll(PRS_POM_FOR_UPDATE);
        }
        if (needUpdateDomPom) {
            System.out.println("Update DOM pom version...");
            UpdatePomVersionUtil.updateNormalPomVersion(projectRootPath, DOM_POM_FOR_UPDATE);
            pomFilesForPush.addAll(DOM_POM_FOR_UPDATE);
        }
        if (needUpdateDSHCommonPom) {
            System.out.println("Update DSH_Common pom version...");
            UpdatePomVersionUtil.updateDSHCommonOrJaxbPomVersion(projectRootPath, DSH_COMMON_POM_FOR_UPDATE);
            pomFilesForPush.add(DSH_COMMON_POM_FOR_UPDATE);
            pomFilesForPush.add(PROJECT_ROOT_POM);
        }
        if (needUpdateDSHJaxbPom) {
            System.out.println("Update DSH_Jaxb pom version...");
            UpdatePomVersionUtil.updateDSHCommonOrJaxbPomVersion(projectRootPath, DSH_JAXB_POM_FOR_UPDATE);
            pomFilesForPush.add(DSH_JAXB_POM_FOR_UPDATE);
            pomFilesForPush.add(PROJECT_ROOT_POM);
        }
        System.out.println("End update pom version...");
        return pomFilesForPush;
    }

    private static List<DiffEntry> getCommitChanges(Repository existingRepository, Date since, Date until) throws IOException {
        //        Ref head = existingRepo.exactRef("refs/heads/water_test");
        // a RevWalk allows to walk over commits based on some filtering that is defined
        try (RevWalk walk = new RevWalk(existingRepository)) {
            ObjectId head = existingRepository.resolve(Constants.HEAD);
            RevCommit headCommit = walk.parseCommit(head);
            walk.markStart(headCommit);
            walk.sort(RevSort.REVERSE);

            walk.setRevFilter(CommitTimeRevFilter.between(since, until));
            RevCommit earlierCommit = null;
            for (RevCommit commit : walk) {
                //because walk sorted by RevSort.REVERSE, so the first commit is the earlier commit of the filter
                earlierCommit = commit;
                break;
            }
            if (earlierCommit != null) {
                boolean onlyOneCommit = earlierCommit.getTree().equals(headCommit.getTree());
                if (onlyOneCommit) {
                    walk.reset();
                    walk.markStart(headCommit);
                    walk.sort(RevSort.COMMIT_TIME_DESC);
                    //reset the filter, or it will use the filter that was set before(here is RevSort.REVERSE)
                    walk.setRevFilter(RevFilter.ALL);
                    //it will return start commit(here is headCommit, because walk.markStart(headCommit))
                    walk.next();
                    //get next commit before head commit
                    RevCommit nextCommit = walk.next();
                    DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                    df.setRepository(existingRepository);
                    df.setDiffComparator(RawTextComparator.DEFAULT);
                    df.setDetectRenames(true);
                    List<DiffEntry> diffEntries = df.scan(nextCommit.getTree(), headCommit.getTree());
//                    for (DiffEntry diff : diffEntries) {
//                        System.out.println(MessageFormat.format("({0} {1} {2}", diff.getChangeType().name(), diff.getNewMode().getBits(), diff.getNewPath()));
//                    }
                    return diffEntries;

                }
                //why parse, not getTree directly?
//                RevCommit parent = walk.parseCommit(headCommit.getParent(0).getId());
                DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
                df.setRepository(existingRepository);
                df.setDiffComparator(RawTextComparator.DEFAULT);
                df.setDetectRenames(true);
                List<DiffEntry> diffs = df.scan(earlierCommit.getTree(), headCommit.getTree());
//                for (DiffEntry diff : diffs) {
//                    System.out.println(MessageFormat.format("({0} {1} {2}", diff.getChangeType().name(), diff.getNewMode().getBits(), diff.getNewPath()));
//                }
                walk.dispose();
                return diffs;
            }
            return new ArrayList<>();
        }
    }

    private static boolean matchPomFile(DiffEntry diffEntry) {
        return Pattern.compile("pom(.*)\\.xml").matcher(diffEntry.getNewPath()).find();
    }
}
