package com.utility.files;

import androidx.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by Phong on 1/15/2018.
 */

public class FileUtilsResult implements Serializable {

    private String message;
    private boolean success;

    public FileUtilsResult() {
    }

    public FileUtilsResult(boolean success, @Nullable String message) {
        this.success = success;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
