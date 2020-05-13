package au.com.addstar.whatis.eventhook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 12/05/2020.
 */
public class FileEventOutput extends File implements EventOutput{
    public FileEventOutput(String pathname) {
        super(pathname);
    }

    public FileEventOutput(String parent, String child) {
        super(parent, child);
    }

    public FileEventOutput(File parent, String child) {
        super(parent, child);
    }

    public FileEventOutput(URI uri) {
        super(uri);
    }

    @Override
    public String getDescription() {
        return getPath();
    }

    @Override
    public Writer getWriter() throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this)));
    }
}
