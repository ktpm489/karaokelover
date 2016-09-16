package vn.com.frankle.karaokelover.util;

import java.io.File;

/**
 * Created by duclm on 9/16/2016.
 */

public class FileCompare implements Comparable {

    public long modifiedDate;
    public File file;

    public FileCompare(File file) {
        this.file = file;
        modifiedDate = file.lastModified();
    }

    @Override
    public int compareTo(Object object) {
        long comparedDate = ((FileCompare) object).modifiedDate;
        return modifiedDate > comparedDate ? -1 : modifiedDate == comparedDate ? 0 : 1;
    }
}
