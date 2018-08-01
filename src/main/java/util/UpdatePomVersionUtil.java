package util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdatePomVersionUtil {

    private static String versionStartTag = "<version>";
    private static String versionEndTag = "</version>";

    public static void updateDSHCommonOrJaxbPomVersion(String projectRootPath, String childModulePomPath) throws IOException {
        if (childModulePomPath.contains("DSH_Common")) {
            writeDSHCommonOrJaxbPomVersion(projectRootPath, childModulePomPath, "<artifactId>DSH_Common</artifactId>");
        }
        if (childModulePomPath.contains("DSH_JAXB")) {
            writeDSHCommonOrJaxbPomVersion(projectRootPath, childModulePomPath, "<artifactId>DSH_JAXB</artifactId>");
        }
    }

    private static void writeDSHCommonOrJaxbPomVersion(String projectRootPath, String childModulePomPath, String childModulePomConfig) throws IOException {
        projectRootPath = projectRootPath.replace("\\\\", "/");
        String projectRootPomPath = projectRootPath + "pom.xml";
        if (!projectRootPath.endsWith("/")) {
            projectRootPomPath = projectRootPath + "/" + "pom.xml";
        }
        //first update DSH_Common or DSH_Jaxb pom.xml version
        List<String> dshCommonOrJaxbPomVersionListAfterUpdated = updateNormalPomVersion(projectRootPath, Collections.singletonList(childModulePomPath));
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(projectRootPomPath, "rw")) {
            boolean existDSHCommonOrJaxb = false;
            String line;
            long lastPoint = 0;
            long offset;
            while ((line = randomAccessFile.readLine()) != null) {
                offset = lastPoint;
                lastPoint = randomAccessFile.getFilePointer();
                if (!existDSHCommonOrJaxb) {
                    existDSHCommonOrJaxb = line.contains(childModulePomConfig);
                }
                if (existDSHCommonOrJaxb && line.contains(versionStartTag)) {
                    //update project root pom.xml version
                    writeUpdatedPomVersion(randomAccessFile, line, lastPoint, offset, dshCommonOrJaxbPomVersionListAfterUpdated.get(0));
                    break;
                }
            }
        }
    }

    public static List<String> updateNormalPomVersion(String projectRootPath, List<String> pomPaths) throws IOException {
        projectRootPath = projectRootPath.replace("\\\\", "/");
        List<String> pomVersionListAfterUpdated = new ArrayList<>();
        for (String pomPath : pomPaths) {
            String fullPomPath = projectRootPath + pomPath;
            if (!projectRootPath.endsWith("/")) {
                fullPomPath = projectRootPath + "/" + pomPath;
            }
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(fullPomPath, "rw")) {
                String line;
                boolean hasParent = false;
                int versionCountWasFound = 0;
                long lastPoint = 0;
                long offset = 0;
                while ((line = randomAccessFile.readLine()) != null) {
                    offset = lastPoint;
                    lastPoint = randomAccessFile.getFilePointer();
                    if (!hasParent) {
                        hasParent = line.contains("<parent>");
                    }
                    if (!hasParent && line.contains(versionStartTag)) {
                        pomVersionListAfterUpdated = writeUpdatedPomVersion(randomAccessFile, line, lastPoint, offset, null);
                        break;
                    }
                    if (hasParent && line.contains(versionStartTag) && versionCountWasFound == 0) {
//                        System.out.println("parent version:" + line);
                        versionCountWasFound++;
                        continue;
                    }
                    if (line.contains(versionStartTag)) {
                        pomVersionListAfterUpdated = writeUpdatedPomVersion(randomAccessFile, line, lastPoint, offset, null);
                        break;
                    }
                }
            }
        }
        return pomVersionListAfterUpdated;
    }

    private static List<String> writeUpdatedPomVersion(RandomAccessFile randomAccessFile, String line, long lastPoint, long offset, String commonChildModuleVersionLine) throws IOException {
        long length = lastPoint - offset;
        byte[] buffer = new byte[1024 * 1024];
        int endRead;
        int maxLength;
        String versionConfig = "";
        List<String> pomVersionListAfterUpdated = new ArrayList<>();
        while ((endRead = randomAccessFile.read(buffer)) != -1) {
            randomAccessFile.seek(randomAccessFile.getFilePointer() - length - endRead);
            if (commonChildModuleVersionLine != null) {
                versionConfig = line.replaceAll("<version>(\\d+\\.\\d+\\.\\d+\\.\\d+\\.\\d+)</version>", commonChildModuleVersionLine);
            } else {
                String pomVersionAfterUpdated = plusOneToPomVersion(line, versionStartTag, versionEndTag);
                pomVersionListAfterUpdated.add(pomVersionAfterUpdated);
                versionConfig = line.replaceAll("<version>(\\d+\\.\\d+\\.\\d+\\.\\d+\\.\\d+)</version>", pomVersionAfterUpdated);
            }
            randomAccessFile.writeBytes(versionConfig);
            randomAccessFile.write(buffer, 0, endRead);
            maxLength = length > versionConfig.length() ? (int) length : versionConfig.length();
            randomAccessFile.seek(randomAccessFile.getFilePointer() + maxLength);
        }
        if (versionConfig.length() < length) {
            randomAccessFile.setLength(randomAccessFile.getFilePointer() - (length - versionConfig.length()));
        }
        return pomVersionListAfterUpdated;
    }

    private static String plusOneToPomVersion(String line, String versionStartTag, String versionEndTag) {
        String versionNumber = line.replace(versionStartTag, "").replace(versionEndTag, "");
        String[] versionNumberSegment = versionNumber.split("\\.");
        if (versionNumberSegment.length == 5) {
            versionNumberSegment[0] = versionNumberSegment[0].trim();
            String updatedVersion = versionNumberSegment[4].trim();
            versionNumberSegment[4] = String.format("%03d", Integer.parseInt(String.valueOf((Integer.parseInt(updatedVersion) + 1))));
        }
        String resultVersionNumber = String.join(".", versionNumberSegment);
        return versionStartTag + resultVersionNumber + versionEndTag + System.getProperty("line.separator");
    }

}
