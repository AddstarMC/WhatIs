package au.com.addstar.whatis.objects;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/06/2020.
 */
public abstract class ExplodingBall extends Ball {
    private int s;

    public ExplodingBall(int radius) {
        super(radius);
    }

    abstract void explode();
}
