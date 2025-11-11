package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

import java.awt.*;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.Diableavionics_finder;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;


public class Diableavionics_antiMissileLauncherSmallAI implements AutofireAIPlugin {

    private final IntervalUtil tick = new IntervalUtil(0.15f,0.25f);
    private final WeaponAPI weapon;
    private final ShipAPI ship;
    private CombatEntityAPI target = null;
    private boolean shouldfire=false;

    private MissileAPI targetmissile;
    private ShipAPI targetfighter = null;
    public Diableavionics_antiMissileLauncherSmallAI(WeaponAPI weapon){
            this.weapon=weapon;
            this.ship= weapon.getShip();
    }

    @Override
    public void advance(float amount) {

        if(weapon.isDisabled()) {return;}

        if (weapon.getAmmo() <= 0) {
            return;
        }
        if ((ship == null) || !ship.isAlive()) {
            return;
        }


        if(!Global.getCombatEngine().isPaused()){
            tick.advance(Global.getCombatEngine().getElapsedInLastFrame());
            if(tick.intervalElapsed()) {
                target = null;
                targetfighter = null;
                targetmissile = null;

                //对于较小的魔术盒导弹发射器 我们希望它瞄准距离较近的敌人 不瞄准船只
                //搜索范围较小,可以直接遍历。
                List<MissileAPI> missiles = AIUtils.getNearbyEnemyMissiles(ship, weapon.getRange());
                targetfighter = Diableavionics_finder.nearestEnemyFighterInRange(ship, weapon.getRange());
                if (missiles.isEmpty()&&targetfighter==null) {
                            shouldfire = false;
                } else {
                    if(!missiles.isEmpty()){ target=AIUtils.getNearestMissile(ship);}
                    else if(targetfighter!=null){ target=targetfighter; }
                }
            }

        }
    }

    @Override
    public boolean shouldFire() {
        return  shouldfire;
    }

    @Override
    public void forceOff() {
            shouldfire = false;
    }

    @Override
    public Vector2f getTarget() {
        return target.getLocation();
    }

    @Override
    public ShipAPI getTargetShip() {
        if (target instanceof ShipAPI) {
            return (ShipAPI) target;
        }
        return null;
    }

    @Override
    public WeaponAPI getWeapon() {
        return weapon;
    }

    @Override
    public MissileAPI getTargetMissile() {
        if (target instanceof MissileAPI) {
            return (MissileAPI) target;
        }
        return null;
    }
}
