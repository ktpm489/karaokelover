package vn.com.frankle.karaokelover.exceptions;

/**
 * Created by duclm on 8/11/2016.
 */

public class InvalidSourceException extends Exception {

    public String message = "Invalid video source url";
    public int code = 101;

    public InvalidSourceException() {
    }

    public InvalidSourceException(String message) {
        super(message);
    }

}