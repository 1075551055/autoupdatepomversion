package pojo;

import java.util.Date;

public class GitParameter {
    private String projectRootPath;
    private String remoteBranch;
    private String originGitUrl;
    private String hkgGitUrl;
    private String hkgRemoteRepository;
    private Date since;
    private Date until;

    public GitParameter(String projectRootPath, String remoteBranch, String originGitUrl, String hkgGitUrl, String hkgRemoteRepository, Date since, Date until) {
        this.projectRootPath = projectRootPath;
        this.remoteBranch = remoteBranch;
        this.originGitUrl = originGitUrl;
        this.hkgGitUrl = hkgGitUrl;
        this.hkgRemoteRepository = hkgRemoteRepository;
        this.since = since;
        this.until = until;
    }

    public String getProjectRootPath() {
        return projectRootPath;
    }

    public String getRemoteBranch() {
        return remoteBranch;
    }

    public String getOriginGitUrl() {
        return originGitUrl;
    }

    public String getHkgGitUrl() {
        return hkgGitUrl;
    }

    public String getHkgRemoteRepository() {
        return hkgRemoteRepository;
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
