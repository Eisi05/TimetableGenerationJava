package de.maxkei.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Utility interface for working with ZIP files.
 */
public interface ZipHelper
{
    /**
     * Compresses a file or directory into a ZIP file.
     *
     * @param fileToZip   the file or directory to compress
     * @param parentFile  the parent directory of the zip files. This is normally set to null
     * @param renamedFile the name of the file or directory within the ZIP file
     * @param zos         the {@link ZipOutputStream} to write the compressed data to
     * @throws IOException if an I/O error occurs while compressing the file or directory
     */
    static void zipFile(@NotNull File fileToZip, @Nullable String parentFile, @NotNull String renamedFile,
                        @NotNull ZipOutputStream zos)
            throws IOException
    {
        if(!fileToZip.exists() || fileToZip.isHidden())
            return;

        String zipEntryName = fileToZip.getName();
        if(parentFile != null && !parentFile.isEmpty())
            zipEntryName = parentFile + "/" + fileToZip.getName();

        if(fileToZip.isDirectory())
        {
            if(parentFile == null)
                zipEntryName = renamedFile;

            File[] files = fileToZip.listFiles();
            if(files == null)
                return;

            for(File file : files)
                zipFile(file, zipEntryName, renamedFile, zos);
        }
        else
        {
            byte[] buffer = new byte[1024];
            FileInputStream fis = new FileInputStream(fileToZip);
            zos.putNextEntry(new ZipEntry(zipEntryName));
            int length;
            while((length = fis.read(buffer)) > 0)
                zos.write(buffer, 0, length);
            zos.closeEntry();
            fis.close();
        }
    }

    /**
     * Extracts a ZIP file into the specified output directory.
     *
     * @param zipFilePath the path to the ZIP file to extract
     * @param outputDir   the directory where the contents of the ZIP file will be extracted
     */
    static void extract(@NotNull String zipFilePath, @NotNull String outputDir)
    {
        String zipName = zipFilePath.split("/")[zipFilePath.split("/").length - 1];
        zipName = zipName.substring(0, zipName.lastIndexOf("."));

        try
        {
            File destDirFile = new File(outputDir);
            if(!destDirFile.exists())
                destDirFile.mkdirs();

            FileInputStream fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null)
            {
                File entryFile = new File(outputDir, zipName + entry.getName().substring(entry.getName().indexOf("/")));

                if(entry.isDirectory())
                {
                    if(!entryFile.exists())
                        entryFile.mkdirs();
                }
                else
                {
                    File entryParent = entryFile.getParentFile();
                    if(!entryParent.exists())
                        entryParent.mkdirs();

                    try(FileOutputStream fos = new FileOutputStream(entryFile);
                        BufferedOutputStream bos = new BufferedOutputStream(fos))
                    {

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while((bytesRead = zis.read(buffer)) != -1)
                            bos.write(buffer, 0, bytesRead);
                    }
                }
                zis.closeEntry();
            }

            zis.close();
            fis.close();
        } catch(IOException ignored)
        {
        }
    }
}
