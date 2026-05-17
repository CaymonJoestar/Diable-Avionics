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

import com.fs.starfarer.api.loading.WingRole;
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

    public static List<ShipAPI> siroco_getFighters(ShipAPI carrier,float SYSTEM_RANGE) {
        List<ShipAPI> result = new ArrayList<ShipAPI>();

//        for (ShipAPI ship: CombatUtils.getShipsWithinRange(carrier.getLocation(),2000)){
//            if (!ship.isFighter()) continue;
//            if (ship.getWing() == null) continue;
//
//            if (ship.getOwner() == carrier.getOwner()){
//                result.add(ship);
//            }
//
//
//        }
        float range=SYSTEM_RANGE;
        if(carrier.getOriginalCaptain().getStats().hasSkill("systems_expertise")){
            range*=1.5f;
        }
        for(ShipAPI ship: AIUtils.getNearbyAllies(carrier,range)){

            if (!ship.isFighter()) continue;
            if (!ship.isAlive()) continue;
            if (ship.isDrone()) continue;
            //for  exclude spawned Shipapi  such as strife`s sheild  排除类似于冲突护盾这样的 被生成的对象
            //诚挚建议:以后在做跟null做条件判断时应该把null放在条件前面，把变量放在后边
            if(null == ship.getWing()) continue;
            if(null == ship.getWing().getSourceShip())continue;
            if (ship.isHulk()||ship.isLanding()|| ship.isPhased())continue;

            //对非万泽舰载机 排除非万泽的轰炸机和支援机
            if(!ship.getWing().getSpec().hasTag("wanzer")){

                if(ship.getWing().getRole().equals(WingRole.BOMBER)||ship.getWing().getRole().equals(WingRole.SUPPORT)) continue;

            }
            //对于万泽 排除战意狙击手和夜莺
            if(ship.getWing().getSpec().hasTag("wanzer")){

                if(ship.getHullSpec().getHullId().contains("warlust")){
                    if(ship.getWing().getSourceShip().getVariant().getHullMods().contains("diableavionics_sniperkit"))
                        continue;
                }

                if(ship.getHullSpec().getHullId().contains("nightingale"))continue;
            }

            if (ship.getOwner() == carrier.getOwner()){

                result.add(ship);
            }

        }

//        for (CombatFleetManagerAPI.AssignmentInfo order: Global.getCombatEngine().getFleetManager(1).getTaskManager(true).getAllAssignments()){
//
//            if(order.getType()==CombatAssignmentType.HEAVY_ESCORT||order.getType()==CombatAssignmentType.LIGHT_ESCORT||order.getType()==CombatAssignmentType.MEDIUM_ESCORT){
//
//
//            }
//        }




//        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
//            if (!ship.isFighter()) continue;
//            if (ship.getWing() == null) continue;
//            if (ship.getWing().getSourceShip() == carrier) {
//                result.add(ship);
//            }
//        }

        return result;
    }

}
