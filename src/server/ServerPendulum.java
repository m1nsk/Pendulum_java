package server;

import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.util.HashMap;
import java.util.List;
// NOTE: If you're using NanoHTTPD >= 3.0.0 the namespace is different,
//       instead of the above import use the following:
// import org.nanohttpd.NanoHTTPD;

public class ServerPendulum extends NanoHTTPD {

    public ServerPendulum() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        try {
            new ServerPendulum();
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<body>\n"
                + "\n"
                + "<form action=\"\" method=\"post\" enctype=\"multipart/form-data\">\n"
                + "    Select image to upload:\n"
                + "    <input type=\"file\" name=\"img\" id=\"fileToUpload\">\n"
                + "    <input type=\"submit\" value=\"Upload Image\" name=\"submit\">\n"
                + "</form>\n"
                + "\n"
                + "</body>\n"
                + "</html>";
        Map<String, List<String>> parms = session.getParameters();

        if (session.getMethod() == Method.POST) {
            Map<String, String> files = new HashMap<String, String>();
            try {
                session.parseBody(files);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ResponseException e1) {
                e1.printStackTrace();
            }
            File file = new File(files.get("img"));
        }
        return newFixedLengthResponse(msg);
    }
}
