package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.util.Diableavionics_finder;
import java.util.*;


public class Diableavionics_mechaStormAI implements ShipSystemAIScript{

    private static int MINIMUN_FIGHTER_NUM = 6;
    public static float EFFECT_RANGE = 2000f;
    public boolean system_officer =false;
    IntervalUtil check_tick = new IntervalUtil(1.8f, 2.2f);
    public List<ShipAPI> fighters = new ArrayList<>();
    public float search_range=EFFECT_RANGE;
    private ShipSystemAPI sirocco_system;
    private ShipAPI sirocco_ship;


    public static Map elite_ship_dp = new HashMap();
    static {
        elite_ship_dp.put(ShipAPI.HullSize.FRIGATE, 12f);
        elite_ship_dp.put(ShipAPI.HullSize.DESTROYER, 20f);
        elite_ship_dp.put(ShipAPI.HullSize.CRUISER, 30f);
        elite_ship_dp.put(ShipAPI.HullSize.CAPITAL_SHIP, 50f);
    }



    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {

        if( ship.getOriginalCaptain().getStats().hasSkill("systems_expertise")){
            system_officer=true;
        }

        if(system_officer){
            search_range*=1.5f;
        }
        sirocco_system=system;
        sirocco_ship=ship;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {


        check_tick.advance(amount);




        if (check_tick.intervalElapsed()&&AIUtils.canUseSystemThisFrame(sirocco_ship)){

            if(null==target) return;


            float system_open = 0f;

            float fighter_score= 0f;
            int fighter_number= 0;

            fighters=Diableavionics_finder.siroco_getFighters(sirocco_ship,search_range);

            for(ShipAPI fighter:fighters){

                fighter_score += fighter.getWing().getSpec().getOpCost(fighter.getMutableStats());
                fighter_number++;
            }
            //consider the attack gruop size factor
            if(fighter_number>=MINIMUN_FIGHTER_NUM){

                system_open+= (float) (0.2*((float) fighter_number /MINIMUN_FIGHTER_NUM));

                if(system_open>=0.4f) system_open=0.4f;

            }else if (fighter_number<MINIMUN_FIGHTER_NUM){

                system_open-=0.1*(MINIMUN_FIGHTER_NUM-fighter_number);
            }
            //consider high value target factor
            if(target.getHullSize().equals(ShipAPI.HullSize.FIGHTER)){

                system_open=-100;

            }else{
                float a= (float) elite_ship_dp.get(target.getHullSize());
                float b=target.getMutableStats().getSuppliesPerMonth().getBaseValue();
                float elite_factor=0.3f*(b/a);

                system_open+=elite_factor;
            }

            //consider distance factor
            float distance = MathUtils.getDistance(sirocco_ship.getLocation(),target.getLocation());


            float distance_factor= (float) (0.05*Math.abs(1500-distance)/100f);

            //target is closer to 1.5k line
            if (distance<1500f){

                system_open+=0.1f;

            }else{

                //target is too far from sirocco, the fighters needs ship`s aid.
                system_open-=distance_factor;
            }


            float roll=MathUtils.getRandomNumberInRange(0f,1f);

            if(system_open>=roll){

                if(AIUtils.canUseSystemThisFrame(sirocco_ship)){

                    sirocco_ship.useSystem();
                }

            }

        }


    }




}
