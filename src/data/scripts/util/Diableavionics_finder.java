package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

public class Diableavionics_finder {



    public static final Map<String, Float> DIABLE_WANZER_SCORE_MAP=new HashMap<>();
    static {
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_frost",1f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_strife",2f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_hoar",1.5f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_warlust",2f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_avalanche",3f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_blizzaia",3f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_valiant",2f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_raven",4f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_zephyr",4f);
        DIABLE_WANZER_SCORE_MAP.put("diableavionics_versant",6f);
        DIABLE_WANZER_SCORE_MAP.put("virtuous",20f);
    }

    public static final Map<String, Float> WANZER_RANGE_MULT =new HashMap<>();
    static {
        WANZER_RANGE_MULT.put("diableavionics_frost",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_strife",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_hoar",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_avalanche",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_blizzaia",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_valiant",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_raven",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_zephyr",0.9f);
        WANZER_RANGE_MULT.put("diableavionics_warlust",1.0f);
    }


    public static ShipAPI nearestEnemyFighterInRange(CombatEntityAPI entity, float range) {
        ShipAPI closest = null;
        float closestDistanceSquared = Float.MAX_VALUE;

        for (ShipAPI ship : AIUtils.getNearbyEnemies(entity, range)) {

            if (!ship.isFighter())
                continue;  float distanceSquared = MathUtils.getDistanceSquared(ship.getLocation(), entity.getLocation());


            if (distanceSquared < closestDistanceSquared) {
                closest = ship;
                closestDistanceSquared = distanceSquared;
            }
        }

        return closest;
    }


    public static  List<ShipAPI> nearbyWanzerInRange(CombatEntityAPI entity, float range){

        List<ShipAPI> wanzerlist = new ArrayList<>();

        for(ShipAPI s:AIUtils.getNearbyAllies(entity,range)){

            if(!s.isFighter()){
                continue;
            }else if(DIABLE_WANZER_SCORE_MAP.containsKey(s.getHullSpec().getHullId())){
                wanzerlist.add(s);
            }
        }

        return wanzerlist;
    }
}
