package data.scripts.util;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import data.scripts.ai.WanzerMovementScript;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;

import java.util.HashMap;
import java.util.Map;


public class Diableavionics_wanzerAI {

    private final static Map<String, Boolean> WANZER_PDWEAPON_FILTER= new HashMap<String, Boolean>();

    {
        WANZER_PDWEAPON_FILTER.put("diableavionics_blizzaiaHead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_strifehead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_frosthead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_warlusthead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_warlustmissile",false);
    }




    public static float maxDistanceByweapon(ShipAPI ship){
        float maxDistance = Float.MAX_VALUE;
        float weaponRangeMultiplier=0.9f;
        if(ship.getHullSpec().getHullId().contains("warlust")){
            weaponRangeMultiplier=1.0f;
        }

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative()) {continue;} //对于万泽,遍历他的所有武器,寻找非pd武器射程最小的那个

            if(WANZER_PDWEAPON_FILTER.containsKey(weapon.getId())){continue;} //排除掉所有不希望参与到距离计算的pd武器

            if (weapon.getRange() <= maxDistance) {
                maxDistance = weapon.getRange() * weaponRangeMultiplier;
            }

        }

        return maxDistance;
    }

    public static boolean canRetreat(ShipAPI ship){

        float retreatJudge= 0f;

        //只有整备时间较长或者联队数量少的万泽参与检查
        if(ship.getWing().getSpec().getRefitTime()>=12f||ship.getWing().getSpec().getNumFighters()<=1){
            //生命值较低时检查撤退
            if (ship.getHullLevel()<=0.25){
                retreatJudge= (float) (0.5*(1-ship.getHullLevel())
                        +ship.getWing().getSpec().getRefitTime()/30.0*0.5);
            }

            if(MathUtils.getRandomNumberInRange(0f,1f)<retreatJudge){
                return true;
            }
        }
        return false;
    }

//    public static void setAttackAngle(ShipAPI wanzer,ShipAPI target){
//
//       float shipfacing=target.getFacing();
//
//       float angletowanzer= VectorUtils.getAngle(target.getLocation(),wanzer.getLocation());
//
//       float angledif=shipfacing-angletowanzer;
//
//       if(Math.abs(angledif)<180f&&Math.abs(angledif)>=110f){
//
//
//       }else if()
//
//
//    }

}
