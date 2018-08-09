package pojo;

import java.util.Date;

public class GitParameter {
    private String projectRootPath;
    private String originBranchForUpdatePomVersion;
    private String gitUrlForOrigin;
    private String gitUrlForCentral;
    private String centralBranchForPush;
    private Date since;
    private Date until;

    public GitParameter(String projectRootPath, String originBranchForUpdatePomVersion, String gitUrlForOrigin, String gitUrlForCentral, String centralBranchForPush, Date since, Date until) {
        this.projectRootPath = projectRootPath;
        this.originBranchForUpdatePomVersion = originBranchForUpdatePomVersion;
        this.gitUrlForOrigin = gitUrlForOrigin;
        this.gitUrlForCentral = gitUrlForCentral;
        this.centralBranchForPush = centralBranchForPush;
        this.since = since;
        this.until = until;
    }

    public String getProjectRootPath() {
        projectRootPath = projectRootPath.replace("\\\\", "/");
        if (!projectRootPath.endsWith("/")) {
            projectRootPath = projectRootPath + "/";
        }
        return projectRootPath;
    }

    public String getOriginBranchForUpdatePomVersion() {
        return originBranchForUpdatePomVersion;
    }

    public String getGitUrlForOrigin() {
        return gitUrlForOrigin;
    }

    public String getGitUrlForCentral() {
        return gitUrlForCentral;
    }

    public String getCentralBranchForPush() {
        return centralBranchForPush;
    }

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getUntil() {
        return until;
    }

    public void setUntil(Date until) {
        this.until = until;
    }

    public void setProjectRootPath(String projectRootPath) {
        this.projectRootPath = projectRootPath;
    }
}
