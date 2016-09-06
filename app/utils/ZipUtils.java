package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
	public static File unpackToOutputFolder(final File aFile) throws IOException {

		final byte[] buffer = new byte[1024];
		ZipInputStream zipInputStream = null;
		ZipEntry zipEntry;
		File tempXML = null;
		final String exePattern = "([^\\s]+(\\.(?i)(exe))$)";

		zipInputStream = new ZipInputStream(new FileInputStream(aFile));
		zipEntry = zipInputStream.getNextEntry();
		while (zipEntry != null) {
			if (zipEntry.getName().matches(exePattern)) {
				zipInputStream.closeEntry();
				zipInputStream.close();
				return null;
			}
			zipEntry = zipInputStream.getNextEntry();
		}
		zipInputStream.closeEntry();
		zipInputStream.close();

		zipInputStream = new ZipInputStream(new FileInputStream(aFile));
		zipEntry = zipInputStream.getNextEntry();

		while ((zipEntry != null) && !zipEntry.getName().equals(CONTENT_FILE + ".xml")) {
			zipEntry = zipInputStream.getNextEntry();
		}

		tempXML = File.createTempFile(CONTENT_FILE, ".xml");
		tempXML.deleteOnExit();

		final FileOutputStream fos = new FileOutputStream(tempXML);

		int len;
		while ((len = zipInputStream.read(buffer)) > 0) {
			fos.write(buffer, 0, len);
		}

		fos.close();
		zipInputStream.closeEntry();
		zipInputStream.close();

		return tempXML;
	}

	private static String CONTENT_FILE = "content";
}
