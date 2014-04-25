package com.accia77.mockey.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


//Code entirely taken from:
//http://jondev.net/articles/Unzipping_Files_with_Android_(Programmatically)

public class Decompress {
	private String _zipFile;
	private String _location;

	/**
	 * Decompress zipFile in location
	 * @param zipFile Full path of the file to be unzipped
	 * @param location Full path where to unzip the file
	 */
	public Decompress(String zipFile, String location) {
		_zipFile = zipFile;
		_location = location;

		_dirChecker("");
	}

	public boolean unzip() {
		try {
			FileInputStream fin = new FileInputStream(_zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			while ((ze = zin.getNextEntry()) != null) {
				//Log.v("Decompress", "Unzipping " + ze.getName());

				if (ze.isDirectory()) {
					_dirChecker(ze.getName());
				} else {
					FileOutputStream fout = new FileOutputStream(_location
							+ ze.getName());
					for (int c = zin.read(); c != -1; c = zin.read()) {
						fout.write(c);
					}

					zin.closeEntry();
					fout.close();
				}

			}
			zin.close();
			return true;
		} catch (Exception e) {
			//Log.e("Decompress", "unzip", e);
			return false;
		}

	}

	private void _dirChecker(String dir) {
		File f = new File(_location + dir);

		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}
}