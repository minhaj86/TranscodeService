package com.video.transcoder.resource;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Files;
import java.util.Optional;

@Path("/file")
@Consumes(MediaType.APPLICATION_OCTET_STREAM)
public class FileResource {
    private final String UPLOADED_FILE_PATH = "/home/mrl/media/";

    @POST
    @Path("/upload/{id}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public Response uploadFile(@PathParam Integer id, @QueryParam("filename") Optional<String> fileName, InputStream attachmentInputStream) throws IOException {
        String fName = "";
        if (fileName.isPresent()) {
            if (!createDirectory(this.UPLOADED_FILE_PATH + id + "/")) {
                return Response.serverError().build();
            }
            fName = this.UPLOADED_FILE_PATH + id + "/" + fileName.get();
        } else {
            fName = this.UPLOADED_FILE_PATH + id + ".txt";
        }
        File destFile = new File(fName);
        saveFile(attachmentInputStream, destFile);
        System.out.println("=========FILE UPLOAD========");
        return Response.ok().build();
    }

    private boolean createDirectory(String path) {
        File dir = new File(path);
        if (dir.isDirectory()) {
            System.out.println("========= Directory exists");
            return true;
        }
        System.out.println("========= Directory does not exist");
        return dir.mkdir();
    }

    private void saveFile(InputStream attachmentInputStream, File dest) throws IOException {
        InputStream is = attachmentInputStream;
        OutputStream os = null;
        try {
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            os.close();
        }

    }

    private void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
        Files.copy(source.toPath(), dest.toPath());

    }
}

