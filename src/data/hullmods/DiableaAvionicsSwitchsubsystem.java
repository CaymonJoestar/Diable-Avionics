package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.shipsystems.scripts.Diableavionics_AWACSsubsystem;
import data.shipsystems.scripts.Diableavionics_dampdashSubsystem;
import data.shipsystems.scripts.Diableavionics_arcemitter;
import org.lwjgl.input.Keyboard;
import org.magiclib.subsystems.MagicSubsystemsManager;

import java.awt.*;

import static data.scripts.util.Diableavionics_stringsManager.txt;

public class DiableaAvionicsSwitchsubsystem extends BaseHullMod {

    private boolean hullmodswitched = true;

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        return null;
    }



    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {


        final Color green = new Color(55,245,65,255);
        final Color flavor = new Color(110,110,110,255);
        final float pad = 10f;
        final float padQuote = 10f;
        final float padSig = 2f;
        final float padS = 0f;


        if(Keyboard.isKeyDown(Keyboard.getKeyIndex("LEFT"))){
            hullmodswitched=!hullmodswitched;
        }

        tooltip.addSectionHeading(txt("hm_tooltip_title_specific"), Alignment.MID, pad);
        tooltip.addPara(
                txt("hm_switchsub_cf01")
                        + "\n",
                pad);

        if(hullmodswitched)
        {
            tooltip.addPara(
                    txt("hm_switchsub_cf02"),pad,Misc.getHighlightColor(),txt("hm_switchsub_cf02A_hl")
            );
            tooltip.addPara(txt("hm_switchsub_cf03A01")
                    +txt("hm_switchsub_cf03A02"),pad,Misc.getHighlightColor(),"600","300","150");


        }else{

            tooltip.addPara(
                    txt("hm_switchsub_cf02"),pad,Misc.getHighlightColor(),txt("hm_switchsub_cf02B_hl")
            );


            tooltip.addPara(
                    txt("hm_switchsub_cf03B01")+"\n"+
                            txt("hm_switchsub_cf03B02")+txt("hm_switchsub_cf03B03"),pad,Misc.getHighlightColor(),"1250","25%","40%"
            );
        }
        tooltip.addPara(txt("hm_pressleft"), Misc.getGrayColor(), pad);

    }


    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {


        if (hullmodswitched){
            MagicSubsystemsManager.addSubsystemToShip(ship,new Diableavionics_arcemitter(ship));
        }else{
            MagicSubsystemsManager.addSubsystemToShip(ship, new Diableavionics_AWACSsubsystem(ship));
        }


    }



    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return (ship.getHullSpec().getHullId().startsWith("diableavionics_"));
    }
}
