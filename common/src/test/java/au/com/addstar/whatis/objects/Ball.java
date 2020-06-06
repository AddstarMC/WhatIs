package au.com.addstar.whatis.objects;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/06/2020.
 */
public class Ball extends Object {

    private int radius;

    public Ball(int radius) {
        this.radius = radius;
    }

    public int getRadius() {
        return radius;
    }
}
