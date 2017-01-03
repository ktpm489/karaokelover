package vn.com.frankle.karaokelover.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by duclm on 9/7/2016.
 */

public class RecordingFileFilter implements FileFilter {
    /**
     * Files formats currently supported by Library
     */
    public enum SupportedFileFormat {
        THREE_GP("3gp"),
        WAV("wav");

        private String filesuffix;

        SupportedFileFormat(String filesuffix) {
            this.filesuffix = filesuffix;
        }

        public String getFilesuffix() {
            return filesuffix;
        }
    }


    @Override
    public boolean accept(File file) {
        if (file.isHidden() || !file.canRead()) {
            return false;
        }
        return !file.isDirectory() && filterFileByExtension(file);
    }

    /**
     * Check for file extension
     *
     * @param file : filte to be checked
     * @return true if file extension is "wav" or "3gp"
     */
    private boolean filterFileByExtension(File file) {
        String fileExt = getFileExtension(file.getName());
        if (fileExt == null) {
            return false;
        }
        if (fileExt.equals("3gp") || fileExt.equals("wav")) {
            return true;
        }
        return false;
    }

    public String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        } else
            return null;
    }
}
