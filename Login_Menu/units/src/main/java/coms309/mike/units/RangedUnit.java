package coms309.mike.units;

/**
 * Created by mike on 6/10/2017.
 *
 */

public interface RangedUnit{

    int getMinAttackRange();

    int getMaxAttackRange();

    boolean needsLineOfSight();
}
