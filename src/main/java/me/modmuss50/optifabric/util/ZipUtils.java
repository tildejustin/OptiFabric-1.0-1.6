package me.modmuss50.optifabric.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class ZipUtils {
	public interface ZipTranformer {
		boolean keep(ZipFile zip, ZipEntry entry) throws IOException;
	}

	/**
	 * Visit the contents of the given zip file as per {@link ZipFile#entries()}
	 *
	 * @param zip The zip file to visit the contents of
	 * @param visitor A visitor to receive the contents, returning {@code false} will immediately end visiting
	 */
	public static void iterateContents(File zip, ZipTranformer visitor) {
		try (ZipFile origin = new ZipFile(zip)) {
			for (Enumeration<? extends ZipEntry> it = origin.entries(); it.hasMoreElements();) {
				if (!visitor.keep(origin, it.nextElement())) {
					break;
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Error iterating " + zip, e);
		}
	}

	/**
	 * Extract the given zip file into the given root directory
	 *
	 * @param zip The zip file to be extracted fully
	 * @param to The directory to extract the zip into
	 */
	public static void extract(File zip, File to) {
		iterateContents(zip, (zipFile, entry) -> {
			String name = entry.getName();
			File extract = new File(to, name);

			if (name.indexOf("..") >= 0 && !extract.getCanonicalPath().startsWith(to.getCanonicalPath())) {
				throw new SecurityException("The file \"" + name + "\" (in " + zip + ") tried to leave the output directory: " + to);
			}

			if (entry.isDirectory()) {
				FileUtils.forceMkdir(extract);
			} else {
				FileUtils.forceMkdir(extract.getParentFile());
				FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), extract);
			}

			return true;
		});
	}

	/**
	 * Filter the given zip based on the given filter
	 *
	 * @param zip The zip file to filter the contents of
	 * @param filter A filter of the contents, returning {@code false} will remove the given entry
	 */
	public static void transformInPlace(File zip, ZipTranformer filter) {
		File tempZip = null;
		try {
			tempZip = File.createTempFile("optifabric", ".zip");

			transform(zip, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE, filter, tempZip);

			FileUtils.moveFile(tempZip, zip);
		} catch (IOException e) {
			throw new UncheckedIOException("Error modifying " + zip, e);
		} finally {
			FileUtils.deleteQuietly(tempZip);
		}
	}

	/**
	 * Filter the given zip based on the given filter to produce a new zip
	 *
	 * @param zipOrigin The zip file to filter the contents of
	 * @param filter A filter of the contents, returning {@code false} will remove the given entry
	 * @param zipDestination The location of the filtered zip file
	 */
	public static void transform(File zipOrigin, ZipTranformer filter, File zipDestination) {
		try {
			transform(zipOrigin, ZipFile.OPEN_READ, filter, zipDestination);
		} catch (IOException e) {
			throw new UncheckedIOException("Error transforming " + zipOrigin, e);
		}
	}

	private static void transform(File zipOrigin, int originFlags, ZipTranformer filter, File zipDestination) throws IOException {
		try (ZipFile origin = new ZipFile(zipOrigin, originFlags); ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipDestination)))) {
			for (Enumeration<? extends ZipEntry> it = origin.entries(); it.hasMoreElements();) {
				ZipEntry entry = it.nextElement();

				if (filter.keep(origin, entry)) {
					out.putNextEntry(new ZipEntry(entry));
					IOUtils.copy(origin.getInputStream(entry), out);
				}
			}
		}
	}
}