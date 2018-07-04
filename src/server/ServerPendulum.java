package server;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import pendulum.ImgStorage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//       instead of the above import use the following:
// import org.nanohttpd.NanoHTTPD;

public class ServerPendulum extends NanoHTTPD {
    private ImgStorage imgStorage;
    private final String imgName = "img";

    public ServerPendulum(ImgStorage imgStorage) throws IOException {
        super(8080);
        this.imgStorage = imgStorage;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }


    @Override
    public Response serve(IHTTPSession session) {
        switch (session.getMethod()) {
            case GET:
                return get(session);
            case POST:
                return post(session);
        }
        return getLoadPage();
    }

    private Response get(IHTTPSession session) {
        return getLoadPage();
    }

    private Response getLoadPage() {
        String msg = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "\n"
                + "<form action=\"\" method=\"post\" enctype=\"multipart/form-data\">\n"
                + "    Select image to upload:\n"
                + "    <input type=\"file\" name=\"" + imgName + "\" id=\"fileToUpload\" multiple>\n"
                + "    <input type=\"submit\" value=\"Upload Image\" name=\"submit\">\n"
                + "</form>\n"
                + "\n"
                + "</body>\n"
                + "</html>";
        return newFixedLengthResponse(msg);
    }

    private Response post(IHTTPSession session) {
        Map<String, String> files = new HashMap<String, String>();
        try {
            session.parseBody(files);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ResponseException e1) {
            e1.printStackTrace();
        }
        Map<String, File> fileMap = new HashMap<>();
        for (int i = 0; i < files.size(); i++) {
            String key = i == 0 ? imgName : imgName + i;
            fileMap.put(key, new File(files.get(key)));
        }
        try {
            imgStorage.setImgMapBuffer(fileMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getLoadPage();
    }
}
