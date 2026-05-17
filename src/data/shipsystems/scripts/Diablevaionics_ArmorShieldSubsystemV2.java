package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.drones.MagicDroneSubsystem;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Diablevaionics_ArmorShieldSubsystemV2 extends MagicDroneSubsystem {


    private ShipAPI fighter;
    private int DroneSpawned = 0;
    private static int MAX_DRONE_NUM = 1;
    private static int STRIFE_ARML_NUM = 2;
    LinkedList<ShipAPI> Drones =new LinkedList<ShipAPI>();

    public static Map sheild_id = new HashMap();
    static {
        sheild_id.put("default","diableavionics_warlust_drone");
        sheild_id.put("strife","diableavionics_strifeShield_wing");
    }


    public Diablevaionics_ArmorShieldSubsystemV2(@NotNull ShipAPI ship) {
        super(ship);
        fighter=ship;
        DroneSpawned = 0;
    }

    @Override
    public @NotNull String getDroneVariant() {
        if(fighter.getHullSpec().getHullId().contains("strife"))  return sheild_id.get("strife").toString();
        else  return sheild_id.get("default").toString();
    }



    @Override
    public int getMaxCharges() {
        return 1;
    }

    @Override
    public int getMaxDeployedDrones() {
        return 1;
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
    public boolean dronesExplodeWhenShipDies() {
        return true;
    }



    @Override
    public boolean shouldActivateAI(float amount) {
//        if(DroneSpawned<1 && AIUtils.canUseSystemThisFrame(ship) ){
//            return true;
//        } else return false;
        return canActivate();
    }

    @Override
    public @NotNull ShipAPI spawnDrone() {

        //Global.getCombatEngine().getFleetManager(ship.getOwner()).setSuppressDeploymentMessages(true);
        ShipAPI drone = CombatUtils.spawnShipOrWingDirectly(
                getDroneVariant(),
                FleetMemberType.FIGHTER_WING,
                FleetSide.values()[ship.getOwner()],
                0.9f,
                ship.getLocation(),
                ship.getFacing()
        );

        Drones.add(drone);

        for(WeaponSlotAPI weaponSlot:fighter.getHullSpec().getAllWeaponSlotsCopy()){

            if(weaponSlot.getId().contains("SHIELD")){
                fighter.setStation(true);
                drone.setParentStation(fighter);
                drone.setStationSlot(weaponSlot);
            }
        }

        return drone;

    }
    @Override
    public boolean canActivate() {
        return false;
    }


    @Override
    public float getBarFill() {
        float fill = 0f;
        if (charges < calcMaxCharges()) {
            fill = chargeInterval.getElapsed() / chargeInterval.getIntervalDuration();
        }
        return fill;
    }


    @Override
    public String getDisplayText() {
        return "";
    }

    @Override
    public void advanceInternal(float amount) {



            if(!Drones.isEmpty()) {

                ShipAPI ShieldDrone = Drones.getFirst();

                //set the Sheild on left arm and keep
                Vector2f handposition =new Vector2f(fighter.getLocation().getX()-14f,fighter.getLocation().getY()+4f);
                ShieldDrone.getLocation().set(handposition);
    //
    //            WeaponSlotAPI shieldslot = fighter.getStationSlot();


                float armFacing = fighter.getAllWeapons().get(STRIFE_ARML_NUM).getCurrAngle();
                ShieldDrone.setFacing(armFacing+10f);
    //          ShieldDrone.setFixedLocation(handposition);


                //if fighter is landing, order the shield to land and remove it
                if(fighter.isLanding()){
                    ShieldDrone.beginLandingAnimation(fighter);
                }
                if(ShieldDrone.isFinishedLanding()){
                    Global.getCombatEngine().removeEntity(ShieldDrone);
                }


            }

        super.advanceInternal(amount);
    }
}
