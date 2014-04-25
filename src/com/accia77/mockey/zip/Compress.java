package com.accia77.mockey.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


//Code entirely taken from:
//http://www.jondev.net/articles/Zipping_Files_with_Android_%28Programmatically%29

public class Compress {
	private static final int BUFFER = 2048;

	private String[] _files;
	private String _zipFile;

	// NOTE: both arguments need to be full paths to the files!
	public Compress(String[] files, String zipFile) {
		_files = files;
		_zipFile = zipFile;
	}

	public boolean zip() {
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(_zipFile);

			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));

			byte data[] = new byte[BUFFER];

			for (int i = 0; i < _files.length; i++) {
				//Log.v("Compress", "Adding: " + _files[i]);
				FileInputStream fi = new FileInputStream(_files[i]);
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(_files[i].substring(_files[i]
						.lastIndexOf(File.separator) + 1));
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}

			out.close();
			return true;
		} catch (Exception e) {
			//Log.e("Compress", "zip", e);
			return false;
		}

	}

}