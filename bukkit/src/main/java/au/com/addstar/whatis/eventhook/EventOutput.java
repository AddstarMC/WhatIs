package au.com.addstar.whatis.eventhook;

import java.io.IOException;
import java.io.Writer;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 12/05/2020.
 */
public interface EventOutput {
    String getDescription();
    Writer getWriter() throws IOException;
}
