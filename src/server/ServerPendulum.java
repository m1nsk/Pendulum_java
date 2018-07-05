package server;

import fi.iki.elonen.NanoHTTPD;
import pendulum.ImgStorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        Map<String, String> files = new HashMap<>();
        try {
            session.parseBody(files);
        } catch (IOException | ResponseException e) {
            e.printStackTrace();
        }

        Map<String, File> fileMap = new HashMap<>();
        List<String> instructions = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            String key = i == 0 ? imgName : imgName + i;
            File file = new File(files.get(key));
            fileMap.put(file.getName(), file);
            instructions.add(file.getName());
        }

        try {
            imgStorage.loadData(fileMap, instructions);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getLoadPage();
    }
}
