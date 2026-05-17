package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.scripts.ai.Diableavionics_WanzerDrawbackAI;
import data.scripts.util.Diableavionics_shieldRecycle;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.MagicSubsystem;

import java.awt.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.lazywizard.lazylib.MathUtils.getPoint;


public class Diableavionics_ArmorShieldSubsystem extends MagicSubsystem{

    private static float X_OFFSET = 1;
    private static float Y_OFFSET = 2;


    private ShipAPI fighter;
    private int DroneSpawned = 0;
    private static int MAX_DRONE_NUM = 1;
    private static int STRIFE_ARML_NUM = 2;
    LinkedList<ShipAPI> Drones =new LinkedList<ShipAPI>();
    IntervalUtil checktick = new IntervalUtil(1f, 1.25f);
    private boolean runOnce=false;



    public static Map sheild_id = new HashMap();
    static {
        sheild_id.put("default","diableavionics_warlust_drone");
        sheild_id.put("strife","diableavionics_strifeShield_wing");
    }

    public Diableavionics_ArmorShieldSubsystem(ShipAPI ship) {
        super(ship);
        fighter=ship;
        Global.getCombatEngine().getCustomData().put(ship.getId(),ship);
        DroneSpawned = 0;
        runOnce=true;
    }


    @Override
    public void advanceInternal(float amount) {

        super.advanceInternal(amount);
        checktick.advance(amount);

        if(Drones.isEmpty()){
            return;
        }

        ShipAPI ShieldDrone = Drones.getFirst();
        boolean alive = fighter.isAlive() && !fighter.isHulk() && fighter.getOwner() != 100;


        if(checktick.intervalElapsed()){


            if(!alive){
                Vector2f damageFrom = ShieldDrone.getLocation();
                damageFrom = Misc.getPointWithinRadius(damageFrom, 20f);
                Global.getCombatEngine().applyDamage(ShieldDrone, damageFrom, 1000000f, DamageType.ENERGY, 0f, true, false, ShieldDrone, false);
                Drones.removeFirst();
            }

        }



        if(ShieldDrone!=null) {

            //set the Sheild on left arm and keep
            Vector2f handposition;
            //Vector2f handposition =new Vector2f(fighter.getLocation().getX()-14f,fighter.getLocation().getY()+4f);


            float armFacing = fighter.getAllWeapons().get(STRIFE_ARML_NUM).getCurrAngle();

            //armFacing+10f


            float animationfactor = ship.getSystem().getEffectLevel();

//            if(ship.getSystem().isOn()){
//            }else{
//                handposition = getPoint(ship.getLocation(),14.2f, ship.getFacing()+60f);
//                ShieldDrone.setFacing(ship.getFacing());
//            }

            handposition = getPoint(ship.getLocation(),20f-2.2f*animationfactor, ship.getFacing()+50f-30f*animationfactor);
            ShieldDrone.setFacing(ship.getFacing()-30f*animationfactor);


            ShieldDrone.getLocation().set(handposition);

//            ShieldDrone.setFixedLocation(handposition);
//            Global.getCombatEngine().addFloatingText(ship.getLocation(),"Arm:"+armFacing+" Wanzer="+ship.getFacing()+" Shield:"+ShieldDrone.getFacing()
//                    ,10, Color.ORANGE,ship,1,1);
            //if fighter destroyed, killed the shield

            //if fighter is landing, order the shield to land and remove it
            if(fighter.isLanding()){
                ShieldDrone.beginLandingAnimation(fighter);
            }
            if(ShieldDrone.isFinishedLanding()){
                Global.getCombatEngine().removeEntity(ShieldDrone);
            }
        }

    }

    @Override
    public boolean requiresTarget() {
        return super.requiresTarget();
    }

    @Override
    public float getBaseActiveDuration() {
        return 0;
    }

    @Override
    public float getBaseCooldownDuration() {
        return 0;
    }

    @Override
    public boolean shouldActivateAI(float amount) {

        if(DroneSpawned==0 && AIUtils.canUseSystemThisFrame(ship) ){
            return true;
        } else return false;

    }

    @Override
    public void onActivate() {


        if(DroneSpawned<MAX_DRONE_NUM){
            ShipAPI newdrone = spawnDrone(fighter);
            DroneSpawned++;
            Drones.add(newdrone);
            fighter.addListener(new Diableavionics_shieldRecycle(fighter,newdrone));
        }



    }

    @Override
    public String getDisplayText() {
        return "";
    }


    public ShipAPI spawnDrone(ShipAPI ship){

        //Global.getCombatEngine().getFleetManager(ship.getOwner()).setSuppressDeploymentMessages(true);

//        ShipAPI drone = CombatUtils.spawnShipOrWingDirectly(
//                getDroneVariant(),
//                FleetMemberType.FIGHTER_WING,
//                FleetSide.values()[ship.getOwner()],
//                0.9f,
//                ship.getLocation(),
//                ship.getFacing()
//        );

        //ShipHullSpecAPI spec = Global.getSettings().getHullSpec(getDroneVariant());
        //ShipVariantAPI v = Global.getSettings().createEmptyVariant(getDroneVariant(), spec);

        Global.getCombatEngine().getFleetManager(ship.getOwner()).setSuppressDeploymentMessages(true);
//        ShipAPI drone = Global.getCombatEngine().getFleetManager(ship.getOwner())
//                .spawnShipOrWing("diableavionics_strifeShield_Standard",ship.getLocation(), ship.getFacing());
        ShipAPI drone =Global.getCombatEngine().createFXDrone(Global.getSettings().getVariant("diableavionics_strifeShield_Standard"));
        drone.getLocation().set(ship.getLocation());
        Global.getCombatEngine().addEntity(drone);

//      Global.getCombatEngine().getFleetManager(ship.getOwner()).removeDeployed(drone,false);
        drone.setLayer(CombatEngineLayers.FIGHTERS_LAYER);
        drone.setCollisionClass(CollisionClass.FIGHTER);
        drone.setDrone(true);
        drone.setExplosionScale(0.3f);
        drone.setCRAtDeployment(0.9f);
        drone.setCurrentCR(0.9f);
        drone.setOwner(ship.getOwner());
//        drone.getShipAI().forceCircumstanceEvaluation();
        Global.getCombatEngine().getFleetManager(ship.getOwner()).setSuppressDeploymentMessages(false);

//        for(WeaponSlotAPI weaponSlot:fighter.getHullSpec().getAllWeaponSlotsCopy()){
//
//            if(weaponSlot.getId().contains("SHIELD")){
//                fighter.setStation(true);
//                drone.setParentStation(fighter);
//                drone.setStationSlot(weaponSlot);
//            }
//        }
        return drone;
    }


    private String getDroneVariant() {
        if(fighter.getHullSpec().getHullId().contains("strife"))  return sheild_id.get("strife").toString();
        else  return sheild_id.get("default").toString();
    }
}
