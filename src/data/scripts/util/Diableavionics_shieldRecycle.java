package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class Diableavionics_shieldRecycle implements HullDamageAboutToBeTakenListener {


    private static final float DAMAGE_REDUCE = 0.25f;
    private ShipAPI fighter;
    private ShipAPI drone;


    public Diableavionics_shieldRecycle(ShipAPI wanzer, ShipAPI shield) {
        super();
        fighter=wanzer;
        drone=shield;

    }



    @Override
    public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {

        boolean damage_deny=false;


        //持有护盾的万泽，在特定方向上有伤害豁免

        float shield_angle= VectorUtils.getAngle(fighter.getLocation(),drone.getLocation());

        float damage_angle= VectorUtils.getAngle(fighter.getLocation(),point);

        if(Math.abs(shield_angle-damage_angle)<=85){

            damage_deny=true;
            Global.getCombatEngine().applyDamage(fighter,point,
                    damageAmount*DAMAGE_REDUCE,DamageType.ENERGY,0,
                    false,false,null,false
                    );

        }



        float hull = ship.getHitpoints();

        if (damageAmount >= hull) {
            Vector2f damageFrom = drone.getLocation();
            damageFrom = Misc.getPointWithinRadius(damageFrom, 20f);
            Global.getCombatEngine().applyDamage(drone, damageFrom, 1000000f, DamageType.ENERGY, 0f, true, false, drone, false);
        }


//       if(Global.getCombatEngine().isMission()){
//
//
//       }


        return damage_deny;
    }
}
