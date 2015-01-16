package com.speed.traquer.app;

import android.os.Environment;
import java.io.File;

public final class BaseAlbum extends AlbumStorage {

    private static final String CAMERA_DIR = "/dcim/";

    @Override
    public File getAlbumStorage(String albumName) {
        return new File (
                Environment.getExternalStorageDirectory()
                        + CAMERA_DIR
                        + albumName
        );
    }

}