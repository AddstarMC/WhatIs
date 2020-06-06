package au.com.addstar.whatis.objects;

/**
 * Created for use for the Add5tar MC Minecraft server
 * Created by benjamincharlton on 6/06/2020.
 */
public class Fireball extends ExplodingBall {

    public int getSpeed() {
        return speed;
    }

    private int speed;
    public Fireball(int radius,int speed) {
        super(radius);
        this.speed = speed;
    }

    @Override
    void explode() {

    }
}
