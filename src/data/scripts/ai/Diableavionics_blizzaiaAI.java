package data.scripts.ai;



import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import data.scripts.util.Diableavionics_wanzerAI;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class Diableavionics_blizzaiaAI implements AdvanceableListener {

    private final static Map<ShipAPI.HullSize, Float> SHIP_DISTANCE_MODIFER= new HashMap<ShipAPI.HullSize,Float>();


    {
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CAPITAL_SHIP,0.7f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CRUISER,0.6f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DESTROYER,0.5f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FRIGATE,0.5f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DEFAULT,1f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FIGHTER,1f);
    }

    enum wanzerState{
        engaging,retreat
    }
    wanzerState state= wanzerState.engaging;

    private ShipAPI ship = null;
    private float maxDistance;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.2f, 0.3f);   //判定减速的间隔

    private boolean finishDecelerate = true;

    private IntervalUtil stateInterval = new IntervalUtil(4f, 6f);   //判定交战状态的间隔
    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);  //判定左右游荡的间隔
    private IntervalUtil cicadaReloadInterval = new IntervalUtil(0.5f, 1f);  //判定左右游荡的间隔


    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    private boolean blizzaia_reloading =false;
    private boolean runOnce = false;

    float CicadaRange = 350f;
    float CicadaCooldown = 7.4f;
    private ShipAIPlugin defaulAI;
    public Diableavionics_blizzaiaAI(ShipAPI ship) {
        this.ship = ship;
        maxDistance=Float.MAX_VALUE;
    }


    @Override
    public void advance(float amount) {

        if(!runOnce){
            defaulAI=ship.getShipAI();
            maxDistance= Diableavionics_wanzerAI.maxDistanceByweapon(ship);
            CicadaRange=maxDistance;
            runOnce=true;
        }

        //Different Wanzer logic judgement here:


        //Wanzer state check here:
        stateInterval.advance(amount);

        if(stateInterval.intervalElapsed()){

            if(state== wanzerState.retreat&&!Diableavionics_wanzerAI.canRetreat(ship)){
                state= wanzerState.engaging;
            }

            if(Diableavionics_wanzerAI.canRetreat(ship)){
                state=wanzerState.retreat;
            }
        }



        //distance check:
        //暴雪:金蝉榴弹处于装弹装填下 拉远距离
        cicadaReloadInterval.advance(amount);
        if(cicadaReloadInterval.intervalElapsed()){

            if(ship.getHullSpec().getBaseHullId().contains("blizzaia")){

                for (WeaponAPI weapon : ship.getAllWeapons()) {

                    if (weapon.getOriginalSpec().getWeaponId().equals("diableavionics_blizzaiaCicada")) {

                        float progress=weapon.getAmmoTracker().getReloadProgress();
                        if(progress==0){
                            blizzaia_reloading=false;
                        }else{
                            blizzaia_reloading=true;
                        }
                        if(blizzaia_reloading){
                            maxDistance = 600f; //令暴雪后撤至较远的距离
                        } else {
                            maxDistance = CicadaRange; //暴雪的武器有效射程大概只有350点左右
                        }
                        break;
                    }
                }
            }

        }



        switch(state){

            case retreat:{
                for(FighterWingAPI wanzerWing:ship.getWing().getSourceShip().getAllWings()){
                    wanzerWing.orderReturn(ship);
                }

            }break;

            case engaging:{

                //推进时钟
                strafeInterval.advance(amount);
                decelerateInterval.advance(amount);

                //顺逆时针运动
                if (strafeInterval.intervalElapsed()) {
                    strafeState = randomStrafeDir();
                }

                //——————运动与后撤部分
                ShipAPI target = ship.getShipTarget();
                if (target != null) {
                    float distance = MathUtils.getDistance(ship,target);

                    // 交战中 选择一个方向侧滑
                    if (distance <= maxDistance) {
                        ship.giveCommand(strafeState, null, 0);
                    }



                    if (decelerateInterval.intervalElapsed()) {

                        if (distance <= maxDistance * SHIP_DISTANCE_MODIFER.get(target.getHullSize())
                                && finishDecelerate) {
                            ship.setShipAI(new Diableavionics_WanzerDrawbackAI(ship,maxDistance));
                            finishDecelerate=false;
                        }

                        if(distance>maxDistance*(SHIP_DISTANCE_MODIFER.get(target.getHullSize())+0.1f)
                                && !finishDecelerate){
                            ship.setShipAI(defaulAI);
                            finishDecelerate=true;
                        }
                    }


                }

            }break;
        }

    }


    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }


}

