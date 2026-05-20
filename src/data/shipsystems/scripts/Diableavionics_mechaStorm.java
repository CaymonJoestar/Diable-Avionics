package data.shipsystems.scripts;

import java.awt.Color;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.loading.WingRole;
import org.lazywizard.lazylib.MathUtils;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

public class Diableavionics_mechaStorm extends BaseShipSystemScript {

    private final float MANEUVERBUFF_PERCENT=150f;
    private static final float SYSTEM_RANGE=4000f;

    private float beep = 0;
    private int beep_times = 0;
    private float alarm_beep = 0;
    private int alarm_beep_times = 0;
    private float sign_timer=0;
    List<ShipAPI> diamond_fighters = new ArrayList<ShipAPI>();
    public static final Object KEY_JITTER = new Object();
    public static final Color JITTER_COLOR = new Color(100,165,255,155);

    private boolean beep_finished = false;
    private boolean firstTime = false;
    private boolean teleport_finished = false;
    private boolean alarm_finished = false;
    private boolean point_checked=false;
    private ShipAPI target;
    private float teleport_radius = 600f;
    private final String section = "diableavionics";

    float[] lockedring_angle =new float[3];
    {
        lockedring_angle[0]=0f;
        lockedring_angle[1]=120f;
        lockedring_angle[2]=240f;
    }
    List<Vector2f> teleport_points= new ArrayList<Vector2f>();

    public Diableavionics_mechaStorm() {
        super();

    }

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {




        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }




//        float teleport_radius= target.getCollisionRadius()+300f;
//        if(teleport_radius<=200f) teleport_radius=300f;



        if (effectLevel > 0) {

            float jitterLevel = effectLevel;
            if(!firstTime){
                firstTime = true;
                //locked the target
                if(ship.getShipTarget()!=null)
                {
                    target = ship.getShipTarget();
                }else{
                    return;
                }


                //reset
                beep_times=0;
                alarm_beep_times=0;

                beep_finished =false;
                teleport_finished=false;
                point_checked=false;
                alarm_finished=false;
                if(!diamond_fighters.isEmpty())
                    diamond_fighters.clear();
                //caculate the jump stuff



                teleport_points = new ArrayList<>();
                if(target.getCollisionRadius()>=600f) teleport_radius=target.getCollisionRadius()+200f;


                //add a targeting ring  (From virtuous)
                MagicRender.objectspace(
                        Global.getSettings().getSprite("diableavionics", "RING"),
                        ship, //anchor
                        new Vector2f(), //offset
                        new Vector2f(), //velocity
                        new Vector2f(64, 64), //size
                        new Vector2f(2500, 2500), //growth
                        MathUtils.getRandomNumberInRange(-180, 180), //angle
                        0, //spin
                        false, //parented
                        new Color(0, 200, 255, 128),
                        true, //additive
                        0, 0, //jitter
                        0.5f, 1f, 0.2f, //flicker
                        0.05f, 0.45f, 1f, //timing
                        true,
                        CombatEngineLayers.UNDER_SHIPS_LAYER
                );

            }



            //create fighter_list

            final String fightersKey = ship.getId() + "_MechaStorm_target";
            List<ShipAPI> fighters = null;
            if (!Global.getCombatEngine().getCustomData().containsKey(fightersKey)) {
                fighters = getFighters(ship);
                Global.getCombatEngine().getCustomData().put(fightersKey, fighters);

            } else {
                fighters = (List<ShipAPI>) Global.getCombatEngine().getCustomData().get(fightersKey);
            }
            if (fighters == null) { // shouldn't be possible, but still
                fighters = new ArrayList<ShipAPI>();
            }




            // move the timer & refresh fighterslist
            beep-=Global.getCombatEngine().getElapsedInLastFrame();
            alarm_beep-=Global.getCombatEngine().getElapsedInLastFrame();
            sign_timer-=Global.getCombatEngine().getElapsedInLastFrame();


            if(!teleport_finished){

                ListIterator<ShipAPI> iterator = fighters.listIterator();

                while(iterator.hasNext()){

                    ShipAPI checkfighter=iterator.next();
                    if (!checkfighter.isAlive()) iterator.remove();
                }

            }

            //make some noise and target diamond for locked on
            if (!beep_finished && beep<=0){
                beep=0.075f;
                //locked on sound
                Global.getSoundPlayer().playSound("diableavionics_virtuousTarget_beep",
                        1, 1, ship.getLocation(), ship.getVelocity());


                //fighterlist is non null and the beep and dianmond didnt finished
                if(!fighters.isEmpty()&&!beep_finished){
                    ListIterator<ShipAPI> iterator = fighters.listIterator();

                        while(iterator.hasNext()){

                            ShipAPI fighter= iterator.next();
                            if(diamond_fighters.contains(fighter)){
                            //JUST DO NOTHING
                            }else{
                                diamond_fighters.add(fighter);
                                diamondLockAWACS(fighter);
                                beep_times++;
                                break;
                            }
                        }
                }

                if (beep_times>=fighters.size()){
                    beep_finished =true;
                }


            }

            if (!alarm_finished && alarm_beep<=0 && effectLevel>=0.1f){

                alarm_beep = 1.2f;
                //locked on sound
                Global.getSoundPlayer().playSound("diableavionics_mechastorm_alarm",
                        1.0f, 0.7f, ship.getLocation(), ship.getVelocity());

                alarm_beep_times++;
                if (alarm_beep_times>=3){
                    alarm_finished=true;
                }

                //locked ship ring (from ttf2! hell yeah!)
                Vector2f lockedring_point =
                        MathUtils.getPoint(target.getLocation(), target.getCollisionRadius()*0.6f, 120f * alarm_beep_times);
                Vector2f offset=new Vector2f(target.getLocation().getX()-lockedring_point.getX(),
                        target.getLocation().getY()-lockedring_point.getY());

                MagicRender.objectspace(
                        Global.getSettings().getSprite("diableavionics", "TONE_LOCKED_RING"),
                        target, //anchor
                        offset, //offset
                        new Vector2f(), //velocity
                        new Vector2f(64+target.getCollisionRadius()*0.7f, 64+target.getCollisionRadius()*0.7f), //size
                        new Vector2f(0,0), //growth
                        lockedring_angle[alarm_beep_times-1], //angle
                        0, //spin
                        false, //parented
                        Color.white,
                        true, //additive
                        0, 0, //jitter
                        0f, 0f, 0f, //flicker
                        0.2f, 0.3f+0.3f+1.2f*(3-alarm_beep_times), 0f, //timing
                        true,
                        CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER
                );


            }



            if(effectLevel>=0.8f&&!point_checked){


                 if (!fighters.isEmpty()){

                     for (ShipAPI fighter : fighters){
                         float teleport_range =  MathUtils.getRandomNumberInRange(teleport_radius-50f,teleport_radius+50f);
                         float teleport_angle = MathUtils.getRandomNumberInRange(0f,360f);
                         teleport_points.add(MathUtils.getPoint(target.getLocation(),teleport_range,teleport_angle));
                     }
                     point_checked=true;
                 }


                }



                if(effectLevel>=0.8f&&sign_timer<=0&&!teleport_finished){


                    // incase if list is empty
                    //if(teleport_points.isEmpty()) return;

                    for(Vector2f point:teleport_points)
                    {
                        MagicRender.battlespace(
                                Global.getSettings().getSprite(section,"JUMPALARM"),
                                point,
                                new Vector2f(),
                                new Vector2f(96,96),
                                new Vector2f(),
                                0,
                                0,
                                Color.yellow,
                                false,
                                0.05f,
                                0.2f,
                                0.05f
                                );
//                        MagicRender.singleframe(
//                                Global.getSettings().getSprite(section,"jumpalarm"),
//                                point,
//                                new Vector2f(64,64),
//                                0,
//                                Color.RED,
//                                false,
//                                CombatEngineLayers.ABOVE_PARTICLES
//                        );
                            sign_timer=0.3f;
                    }

                    Global.getSoundPlayer().playSound("diableavionics_mechastorm_jumpbeep",
                            1.0f, 0.9f, ship.getLocation(), ship.getVelocity());
                }

                if(!teleport_finished){


                    for (ShipAPI fighter : fighters) {
                        if (fighter.isHulk()) continue;

                        float maxRangeBonus = fighter.getCollisionRadius() * 1f;
                        float jitterRangeBonus = 5f + jitterLevel * maxRangeBonus;



                        fighter.setJitter(KEY_JITTER, JITTER_COLOR, jitterLevel, 10, 0f, jitterRangeBonus);

                        if (state == State.IN) {
                            float alpha = 1f - effectLevel * 0.5f;
                            fighter.setExtraAlphaMult(alpha);
                        }
                    }


                }

            if (effectLevel>=0.95&&!teleport_finished){
               for(ShipAPI fighter: fighters){
                   Global.getSoundPlayer().playSound("system_phase_skimmer", 1f, 0.75f, fighter.getLocation(), fighter.getVelocity());
               }
            }





            //jump & buff
            if (effectLevel == 1 &&!teleport_finished) {
                // 充能完毕之后 传送写到这里

                if(ship.isPullBackFighters()){
                    ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS,null,0);
                }

                int index=0;
                for (ShipAPI fighter : fighters){
                    // incase if list is empty
                    if(teleport_points.isEmpty()) continue;


                    fighter.getMutableStats().getAcceleration().modifyPercent(this.getClass().getName(),MANEUVERBUFF_PERCENT);
                    fighter.getMutableStats().getTurnAcceleration().modifyPercent(this.getClass().getName(),MANEUVERBUFF_PERCENT);

                    fighter.getWing().getSourceShip().getMutableStats().getFighterWingRange().modifyMult(this.getClass().getName(),100f);
                    fighter.getLocation().set(teleport_points.get(index));
                    fighter.setFacing(VectorUtils.getAngle(fighter.getLocation(),target.getLocation()));
                    fighter.setExtraAlphaMult(1);
                    fighter.getShipAI().setDoNotFireDelay(1f);

                    for(int i=0;i<4;i++){

                        Vector2f thermalVel=new Vector2f();
                        thermalVel.set(fighter.getVelocity().getX()*0.10f,fighter.getVelocity().getY()*0.10f);
                        Global.getCombatEngine().spawnProjectile(
                                fighter,
                                null,
                                "flarelauncher3",
                                fighter.getLocation(),
                                MathUtils.getRandomNumberInRange(0,360),
                                thermalVel
                        );
                    }
                    index++;
                }

                   teleport_finished=true;
            }


        }
    }



    @Override
    public StatusData getStatusData(int index, State state, float effectLevel) {
        return super.getStatusData(index, state, effectLevel);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {

        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }


        firstTime=false;

        for (ShipAPI fighter : getFighters(ship)) {

            fighter.getMutableStats().getAcceleration().unmodify(this.getClass().getName());
            fighter.getMutableStats().getTurnAcceleration().unmodify(this.getClass().getName());
            fighter.getWing().getSourceShip().getMutableStats().getFighterWingRange().unmodify(this.getClass().getName());

        }

        final String fightersKey = ship.getId() + "_MechaStorm_target";
        Global.getCombatEngine().getCustomData().remove(fightersKey);
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {


            if (system.isOutOfAmmo()) return null;
            if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;

            ShipAPI target = ship.getShipTarget();
            if (target != null  && !target.isFighter() && !target.isHulk() && !target.isDrone() && target.isAlive()) {
                if(MathUtils.getDistance(target,ship)>SYSTEM_RANGE){
                    return "OUT OF RANGE";
                }
                return "READY";
            }

            return "NO TARGET";


    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {

        ShipAPI target = ship.getShipTarget();
        if (target != null  && !target.isFighter() && !target.isHulk() && !target.isDrone() && target.isAlive()) {
            float range=SYSTEM_RANGE;
            if(ship.getOriginalCaptain().getStats().hasSkill("systems_expertise")){
                range*=1.5f;
            }

            if(MathUtils.getDistance(target,ship)<=range){
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public float getActiveOverride(ShipAPI ship) {
        return super.getActiveOverride(ship);
    }

    @Override
    public float getInOverride(ShipAPI ship) {
        return super.getInOverride(ship);
    }

    @Override
    public float getOutOverride(ShipAPI ship) {
        return super.getOutOverride(ship);
    }



    @Override
    public int getUsesOverride(ShipAPI ship) {
        return super.getUsesOverride(ship);
    }


    @Override
    public String getDisplayNameOverride(State state, float effectLevel) {
        return super.getDisplayNameOverride(state, effectLevel);
    }


    public static List<ShipAPI> getFighters(ShipAPI carrier) {
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

    private void diamondLockAWACS(ShipAPI wanzer) {


        if (Global.getCombatEngine().isUIShowingHUD()) {
            //add a targeting diamond
            MagicRender.objectspace(
                    Global.getSettings().getSprite("diableavionics", "DIAMONDAWACS"),
                    wanzer, //anchor
                    new Vector2f(), //offset
                    new Vector2f(), //velocity
                    new Vector2f(64, 64), //size
                    new Vector2f(0, 0), //growth
                    -90, //angle
                    0, //spin
                    false, //parented
                    Color.green,
                    false, //additive
                    0, 0, //jitter
                    0, 0, 0, //flicker
                    0.5f, 5f, 0.5f, //timing
                    true,
                    CombatEngineLayers.BELOW_INDICATORS_LAYER
            );
            //exclude the swirly one if it is too far off screen
            if (MagicRender.screenCheck(0.2f, wanzer.getLocation())) {
                MagicRender.objectspace(
                        Global.getSettings().getSprite("diableavionics", "DIAMONDAWACS"),
                        wanzer, //anchor
                        new Vector2f(), //offset
                        new Vector2f(), //velocity
                        new Vector2f(192, 192), //size
                        new Vector2f(-256, -256), //growth
                        -90, //angle
                        360, //spin
                        false, //parented
                        Color.green,
                        false, //additive
                        0, 0, //jitter
                        0, 0, 0, //flicker
                        0.35f, 0.05f, 0.1f, //timing
                        true,
                        CombatEngineLayers.BELOW_INDICATORS_LAYER
                );
            }

        }
    }




}
