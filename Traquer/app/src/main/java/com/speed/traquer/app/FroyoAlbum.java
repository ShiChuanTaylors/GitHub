package com.speed.traquer.app;

import android.os.Environment;
import java.io.File;

public final class FroyoAlbum extends AlbumStorage {

    @Override
    public File getAlbumStorage(String albumName) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), albumName);

    }

}
