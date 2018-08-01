package job;

import core.UpdatePomVersionByDiff;
import pojo.GitParameter;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleJob {
    public static void main(String[] args) {
        //java -jar autoupdatepom.jar C:/gitlab/demo AutoUpdatePomTest2 upstream git@zha-ir4-ci1-w10:liwa/demo.git git@zha-ir4-ci1-w10:test/demo.git
        if (args.length < 5) {
            throw new RuntimeException("Arguments are not enough, need 5 arguments~");
        }
        String projectRootPath = args[0];
        projectRootPath = projectRootPath.replace("\\\\", "/");
        if (!projectRootPath.endsWith("/")) {
            projectRootPath = projectRootPath + "/";
        }
        String remoteBranch = args[1];
        String hkgRemoteRepository = args[2];
        String originGitUrl = args[3];
        String hkgGitUrl = args[4];

        startNoonSchedule(new GitParameter(projectRootPath, remoteBranch, originGitUrl, hkgGitUrl, hkgRemoteRepository, null, null));
        startAfternoonSchedule(new GitParameter(projectRootPath, remoteBranch, originGitUrl, hkgGitUrl, hkgRemoteRepository, null, null));

    }

    private static void startNoonSchedule(final GitParameter gitParameter) {
        Timer timerForNoon = new Timer();
        Date noonScheduleDateTime = new Date();
        Calendar calendarForNoon = Calendar.getInstance();
        calendarForNoon.setTime(noonScheduleDateTime);
        calendarForNoon.set(Calendar.HOUR_OF_DAY, 11);
        calendarForNoon.set(Calendar.MINUTE, 30);
        noonScheduleDateTime = calendarForNoon.getTime();
        timerForNoon.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Begin noon job at " + new Date());
                try {
                    Date previousAfternoonCommitDateTime = new Date();
                    Calendar calendarForPreviousAfternoon = Calendar.getInstance();
                    calendarForPreviousAfternoon.setTime(previousAfternoonCommitDateTime);
                    calendarForPreviousAfternoon.set(Calendar.DATE, calendarForPreviousAfternoon.get(Calendar.DATE) - 1);
                    calendarForPreviousAfternoon.set(Calendar.HOUR_OF_DAY, 17);
                    calendarForPreviousAfternoon.set(Calendar.MINUTE, 40);
                    previousAfternoonCommitDateTime = calendarForPreviousAfternoon.getTime();

                    Date noonCommitDateTime = new Date();
                    Calendar calendarForNoon = Calendar.getInstance();
                    calendarForNoon.setTime(noonCommitDateTime);
                    calendarForNoon.set(Calendar.HOUR_OF_DAY, 11);
                    calendarForNoon.set(Calendar.MINUTE, 30);
                    noonCommitDateTime = calendarForNoon.getTime();
                    gitParameter.setSince(previousAfternoonCommitDateTime);
                    gitParameter.setUntil(noonCommitDateTime);
                    UpdatePomVersionByDiff.update(gitParameter);
                    System.out.println("End noon job..." + new Date());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, noonScheduleDateTime, 1000 * 60 * 60 * 24);// 这里设定将延时每天固定执行
    }

    private static void startAfternoonSchedule(final GitParameter gitParameter) {
        Timer timerForAfternoon = new Timer();
        Date afternoonScheduleDateTime = new Date();
        Calendar calendarForPreviousAfternoon = Calendar.getInstance();
        calendarForPreviousAfternoon.setTime(afternoonScheduleDateTime);
        calendarForPreviousAfternoon.set(Calendar.HOUR_OF_DAY, 17);
        calendarForPreviousAfternoon.set(Calendar.MINUTE, 40);
        afternoonScheduleDateTime = calendarForPreviousAfternoon.getTime();
        timerForAfternoon.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                System.out.println("Begin afternoon job at " + new Date());
                try {
                    Date afternoonCommitDateTime = new Date();
                    Calendar calendarForPreviousAfternoon = Calendar.getInstance();
                    calendarForPreviousAfternoon.setTime(afternoonCommitDateTime);
                    calendarForPreviousAfternoon.set(Calendar.HOUR_OF_DAY, 17);
                    calendarForPreviousAfternoon.set(Calendar.MINUTE, 40);
                    afternoonCommitDateTime = calendarForPreviousAfternoon.getTime();

                    Date noonCommitDateTime = new Date();
                    Calendar calendarForNoon = Calendar.getInstance();
                    calendarForNoon.setTime(noonCommitDateTime);
                    calendarForNoon.set(Calendar.HOUR_OF_DAY, 11);
                    calendarForNoon.set(Calendar.MINUTE, 30);
                    noonCommitDateTime = calendarForNoon.getTime();
                    gitParameter.setSince(noonCommitDateTime);
                    gitParameter.setUntil(afternoonCommitDateTime);
                    UpdatePomVersionByDiff.update(gitParameter);
                    System.out.println("End afternoon job at " + new Date());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, afternoonScheduleDateTime, 1000 * 60 * 60 * 24);// 这里设定将延时每天固定执行
    }

}
