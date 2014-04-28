/*
 * Copyright (C) 2014 fraben 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

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